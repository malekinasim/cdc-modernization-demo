# Domain model and design decisions

The demo has one application with CQRS. The application writes to a normalized model; a separate read database
holds a model with a different shape, built for the screens. CDC and a Kafka Streams middleware connect the two.

The middleware is only justified if the two models are really different, so this document starts there.

## 1. Domain

Projects with tasks and monthly progress. Each project has a planned progress curve per task, and every month
each task gets a snapshot of its actual progress. The question the users care about is
**"which projects are behind plan, and by how much?"**

## 2. Write model (`project-service`)

Normalized, designed for correct writes, with a few habits of an older system: code columns and dates as strings.

```
EMPLOYEE          (EMP_ID, FIRST_NM, LAST_NM, DEPT_CD)
PROJECT           (PRJ_ID, PRJ_CD 'PRJ-2026-014', PRJ_TITLE, STATUS_CD 'A'|'H'|'C', OWNER_ID → EMPLOYEE,
                   START_DT '20261201', END_DT '20270331', LAST_UPD_TS)
TASK              (TASK_ID, PRJ_ID → PROJECT, TASK_TITLE, WEIGHT, LAST_UPD_TS)
TASK_ASSIGNEE     (TASK_ID, EMP_ID)                          -- one or more people per task
TASK_PLAN         (TASK_ID, PERIOD '202612', PLAN_PCT)       -- planned curve per task
PROGRESS_SNAPSHOT (TASK_ID, PERIOD, ACTUAL_PCT, REPORTED_BY, LAST_UPD_TS)
                                                              -- one per task and month, can be corrected
```

The application only does what a command side does: create projects and tasks, assign people, set the plan,
report and correct monthly progress. It has no Kafka code and publishes no events.

## 3. Read model (`query-service`)

Denormalized, one table per screen, ready to return without joins.

```
project_dashboard
  project_code, title, status 'ACTIVE'|'ON_HOLD'|'CLOSED', owner_name, department,
  start_date DATE, end_date DATE,
  period, planned_pct, actual_pct, variance, is_delayed,
  task_count, tasks_missing_report, last_reported_at

my_tasks
  employee_id, employee_name, project_code, project_title, task_title,
  period, planned_pct, actual_pct, report_missing

project_progress_history
  project_code, period, planned_pct, actual_pct            -- the S-curve
```

## 4. How different the two models are

| Write model | Read model | Who closes the gap |
|---|---|---|
| `STATUS_CD 'A'`, `'20261201'`, `'202612'` | `'ACTIVE'`, `DATE`, `YearMonth` | translation |
| Owner is an ID in `PROJECT` | Owner name and department in the row | join with `EMPLOYEE` |
| Plan and actual are in two tables | Planned, actual and variance side by side | join `TASK_PLAN` with `PROGRESS_SNAPSHOT` |
| Progress per task | Progress per project, weighted by `TASK.WEIGHT` | aggregation |
| No concept of "delayed" or "missing report" | `is_delayed`, `report_missing` | derived values |
| One row per assignment | One row per person and task, with project data | fan-out |

One snapshot written by one user changes several read rows: the project's dashboard row, its history row, and a
`my_tasks` row for every assignee of that task.

## 5. Why the middleware (Kafka Streams)

- **Why not let `project-service` update the read database itself?** Then every write needs a second write in
  another database (the dual-write problem), and the command side would have to know the shape of every screen.
  With CDC the command side stays simple, and new read models can be added without touching it.
- **Why not a plain Kafka consumer?** Debezium gives one topic per table. Building the read model needs state:
  KTables of projects, tasks, plans and employees for the joins, and a running aggregate per project and period.
  That is what Kafka Streams is for.

What the middleware does:

1. Unwraps the Debezium envelope and translates codes, dates and periods.
2. Keeps KTables of employees, projects, tasks, assignees and plans.
3. Joins each snapshot with its task, plan and project.
4. Aggregates task progress into project progress per period, weighted, and derives variance and the delayed flag.
5. Publishes the read model rows to output topics (Avro, Schema Registry), keyed by the read model's primary key.
6. Sends records it cannot process (unknown task, `ACTUAL_PCT = 130`, period `'202613'`) to a dead-letter topic
   and keeps going.

`query-service` consumes the output topics and upserts the rows into the read database.

## 6. Design decisions

- **Upsert, keyed by the read model's primary key.** Every output record is the full new state of a read row.
  Applying it twice gives the same result, so redelivered messages are harmless (idempotency without an inbox table).
- **Corrections just work.** A corrected snapshot is an update in the write database, so CDC emits it, the aggregate
  is recomputed and the read rows are replaced.
- **The read model can be rebuilt.** Reset the middleware, empty the read database, and replay the CDC topics from
  the beginning. Adding a new screen later works the same way.
- **Eventual consistency is accepted.** The read side is a few seconds behind the write side; consumer lag is
  measured and shown in Grafana.

## 7. In the real system (for the interview)

In the real project the CDC pipeline kept a legacy system and new services in sync in **both** directions.
That adds problems this demo deliberately leaves out, but which the README will explain:
- **Loops.** A change synced into system B is captured by CDC in B and sent back to A. A `source` header is not
  enough, because CDC events do not carry Kafka headers from earlier hops; the origin has to be stored in the data
  (for example `LAST_UPD_BY = 'SYNC'`) and filtered in the middleware.
- **Inbox and outbox** in services that consume and publish events themselves.

## 8. Possible extensions (not planned)

Plans that group projects, with progress rolled up recursively; linking projects to strategic goals;
two-way sync with a second system. Each would show one more technique, but none is needed for the core story.

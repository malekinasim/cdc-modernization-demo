# Domain model and design decisions

This document describes what the demo models and, more importantly, **why each part exists**.
The domain is based on a real action-management system (IAMS) that I worked on, simplified on purpose.
Every part that is built must answer a concrete design question; everything else is listed as out of scope.

## 1. The domain in one page

An **action** (اقدام) is a corrective or improvement measure. Every action comes from an **origin system**
(audit, suggestions, risk management, inspection) and keeps the origin's own reference code.

Lifecycle:

```
Registered      title, description, origin + origin reference, proposer
    │           (no execution aspect yet: no tasks, no executors, no plan)
    ▼
Planning        a process with steps, all inside legacy:
    │             1. define the tasks
    │             2. schedule the tasks and the whole project (planned progress per month)
    │             3. assign executors to each task (one or more per task)
    ▼
Project         the action is now executed through its tasks
    │
    ▼
Monitoring      every month a snapshot per task: planned % vs actual %
                progress is reported back to the origin system
```

The plan can change after planning, but only through a **plan change request approved by a manager**.

Action types:

| Type | Code | Has tasks? | Progress comes from |
|---|---|---|---|
| Executable | `E` | Yes, once it becomes a project | Its tasks (weighted) |
| Plan (طرح) | `T` | Never. A plan is not executed itself | Its sub-actions (weighted, recursive) |
| Follower (پیرو) | `F` | Never. It is fulfilled when its leader is executed | Its leader action |

Rules:
- Only actions that have gone into execution (projects), plans or followers can be attached as sub-actions of a plan.
  So every node in a plan tree has a plan and a progress value.
- A sub-action of a plan can itself be a plan or a follower, so plans form a tree of any depth.
- A follower can follow a plan or another follower. The structure must never contain a cycle (validated in legacy).
- Actions are linked to **strategic goals**. Goal achievement is the weighted progress of the linked actions.

## 2. Migration story (Strangler Fig)

The legacy monolith owns everything today. The first capability moved to the new service is **monitoring**
(monthly progress snapshots), because:
- it is the most used part: every executor reports every month;
- origin systems want progress close to real time, not from a nightly batch;
- it gets a new capability that legacy does not have: snapshot approval.

During the transition:

| Owned by legacy | Owned by the new service (`action-service`) |
|---|---|
| Registration, planning, plan change requests, plan/follower structure, goal links | Progress snapshots and their approval |

Some departments still report progress in legacy during the transition, so **snapshots are synced in both directions**.
This is what makes loop prevention a real problem in this demo, not a theoretical one.

## 3. Legacy data model

Deliberately legacy-style: wide tables, code columns, dates as strings, self-references, a technical audit column.

```
ORIGIN          (ORG_CD 'AUD'|'SUG'|'RSK'|'INS', ORG_NM)
EMPLOYEE        (EMP_ID, FIRST_NM, LAST_NM, EMAIL, DEPT_CD)
STRATEGIC_GOAL  (GOAL_ID, GOAL_CD, GOAL_TITLE)

ACTION
  ACT_ID            NUMBER PK
  ACT_CD            'AUD-2026-0012'
  ORG_CD            → ORIGIN
  ORG_REF_CD        reference code in the origin system
  ACT_TITLE, ACT_DESC
  PROPOSER_ID       → EMPLOYEE
  ACT_TYPE_CD       'E' | 'T' | 'F'
  STATUS_CD         'R' registered | 'L' planning | 'E' executing | 'C' closed | 'X' cancelled
  PARENT_ACT_ID     → ACTION   (sub-action of a plan)
  WEIGHT_IN_PARENT  weight in the parent plan's progress
  LEADER_ACT_ID     → ACTION   (only for followers)
  EXEC_ID           → EMPLOYEE (project executor)
  PLAN_START_DT     '20261201'
  PLAN_END_DT       '20270331'
  PLAN_VER          current approved plan version
  LAST_UPD_BY, LAST_UPD_TS

ACTION_GOAL     (ACT_ID, GOAL_ID, CONTRIB_WEIGHT)

ACT_TASK        (TASK_ID, ACT_ID, TASK_TITLE, WEIGHT, START_DT, END_DT, LAST_UPD_BY, LAST_UPD_TS)
TASK_EXECUTOR   (TASK_ID, EMP_ID, ROLE_CD)

ACT_PLAN        (ACT_ID, PLAN_VER, PERIOD '202612', PLAN_PCT)    -- planned curve of the project
TASK_PLAN       (TASK_ID, PLAN_VER, PERIOD, PLAN_PCT)            -- planned curve of each task

PLAN_CHANGE_REQ (REQ_ID, ACT_ID, NEW_PLAN_VER, REQUESTED_BY, APPROVED_BY, STATUS_CD, REASON)

ACT_PROGRESS_SNAPSHOT
  SNAP_ID, TASK_ID, PERIOD, PLAN_PCT, ACTUAL_PCT, REMARK, REPORTED_BY,
  LAST_UPD_BY, LAST_UPD_TS
```

Design decisions:
- **The plan value is frozen in the snapshot.** When a snapshot is taken, `PLAN_PCT` is copied from the plan that is
  valid at that moment. A later plan change does not rewrite history: last month's variance stays what it was.
- **Plans are versioned** (`PLAN_VER`). An approved change request creates a new version; old versions are kept.
- **`LAST_UPD_BY`** exists in the legacy tables anyway. The sync process writes `SYNC` there, which is the basis of
  loop prevention (section 6).

## 4. New service model (`action-service`, monitoring context)

```
ActionProject   (id UUID, legacyId, code, title, originCode, executor, startDate, endDate)   read-only copy
Task            (id UUID, legacyTaskId, actionId, title, weight, executors[], plan[period → pct])  read-only copy
ProgressSnapshot
  id UUID, taskId, period YearMonth, plannedPct (frozen), actualPct, remark,
  status DRAFT | SUBMITTED | APPROVED,
  reportedBy, approvedBy, createdAt, updatedAt
Inbox  (eventId)                        idempotent consumption
Outbox (id, aggregateId, type, payload) reliable publishing
```

- The service receives projects, tasks and plans from legacy; it never changes them.
- **New capability: approval.** An executor submits a snapshot, the project executor approves it.
  Only `APPROVED` snapshots are published to legacy and to origin systems. Legacy has no such concept,
  so this is a routing rule and a lossy mapping at the same time.

## 5. Canonical events (Avro, Schema Registry)

| Event | Produced from | Consumed by |
|---|---|---|
| `ActionRegistered` | new row in `ACTION` | query-service |
| `ActionPlanned` | `STATUS_CD` changes from `L` to `E` (planning finished) | action-service, query-service |
| `ActionStructureChanged` | parent, leader or weight of an action changed | middleware (state), query-service |
| `PlanRebaselined` | change request approved, `PLAN_VER` increased | action-service, middleware, query-service |
| `TaskProgressSnapshotted` | insert **or update** in `ACT_PROGRESS_SNAPSHOT` (legacy has no approval, so every change is final), or an approved snapshot in the outbox | the other side, middleware, query-service |
| `ActionProgressUpdated` | **computed by the middleware** | origin system of the action, query-service |

Every event carries `eventId`, `occurredAt` and a `source` header (`LEGACY` or `ACTION_SERVICE`).

`TaskProgressSnapshotted` is an **upsert keyed by `(taskId, period)`**, not an append: a correction is a new event
with the same key and a later `occurredAt`. The middleware's KTables, the action-service copy and the read models keep
the latest value per key, so a corrected percentage replaces the original one everywhere and the aggregates are
recomputed. Each event still has its own `eventId`, so the inbox skips redeliveries without skipping corrections.

`ActionProgressUpdated` carries `period`, `plannedPct`, `actualPct`, `variance` and
`derivedFrom` (`TASKS` | `CHILDREN` | `LEADER`), so the origin system knows whether a value is measured or derived.

## 6. Why the middleware exists

Without the middleware, each side would need to know the other side's model. With it, both sides only know
the canonical events. When legacy is switched off, only the legacy adapter in the middleware is removed.

Concretely, the middleware (Kafka Streams) does work that neither side can do alone:

1. **Turns row changes into business events.** CDC only says "a row changed". Planning is a process with many
   intermediate row changes (tasks created before they are scheduled, executors added later). The middleware ignores
   them and emits `ActionPlanned` once, when the status moves from planning to executing, built from its state stores.
2. **Translates the legacy model.** Status codes, `'20261130'` strings to dates, `PERIOD` strings to `YearMonth`,
   numeric IDs to UUIDs.
3. **Stateful joins.** A snapshot only has a `TASK_ID`. To know its action, plan and origin system, it is joined with
   KTables built from the task, plan and action topics.
4. **Stateful, recursive aggregation.** Task snapshots become action progress (planned vs actual, weighted).
   Action progress flows up the plan tree and over to followers through a **feedback topic**: every
   `ActionProgressUpdated` is read again, the parent and the followers are recomputed and published.
   This loop is intentional and always terminates: the structure has no cycles, and nothing is published when a value
   does not change.
5. **Content-based routing.** Progress is routed by origin code to `origin.<code>.progress`, so each origin system
   receives only its own actions, including plans and followers.
6. **Loop prevention for the sync.** A `source` header only works for messages that start in Kafka. When the sync
   writes a snapshot into legacy, Debezium sees an ordinary insert without headers. So the origin is stored in the
   data: the sync writes `LAST_UPD_BY = 'SYNC'`, and the middleware drops CDC events with that value.
7. **Validation and dead letters.** `ACTUAL_PCT = 130`, period `'202613'` or an unknown task go to `actions.dlq`
   with the error in the headers; processing continues.

## 7. Read side (CQRS, `query-service`)

During the transition the plan lives in legacy and the actuals live in the new service. **No single write database
has the data to answer "which actions are behind schedule?"**. Only the read model, built from the events of both
sides, can answer it. That is the reason for CQRS in this demo, more than read performance.

| Read model | Question it answers |
|---|---|
| `action_progress_view` | Where is each action? Planned vs actual, variance, delayed flag, origin, executor |
| `action_tree_view` | The tree of a plan with progress per node and the followers attached |
| `progress_history` | Planned vs actual curve (S-curve) per action and task, from the snapshots |
| `my_tasks` | Per executor: tasks, plan for this month, last actual, "snapshot missing this month" |
| `goal_achievement` | Planned vs actual achievement per strategic goal, and the actions holding it back |

Goal achievement is computed only here: no external system needs it in real time.

## 8. Build in layers

The schema is complete from the start (adding a column costs nothing); behaviour is added in layers.

| Layer | What | Interview question it answers | When |
|---|---|---|---|
| 1. Core | Executable projects, tasks, plans, snapshots with planned vs actual, two-way snapshot sync, routing to origin systems | How does CDC work? Duplicates? Loops? Poison messages? | Sprints 1-2 |
| 2. Structure | Plans and followers with recursive aggregation through the feedback topic; plan rebaselining | Why Kafka Streams and not a plain consumer? How did you handle state? | Sprint 3 |
| 3. Goals | Goal links and goal achievement in the read model | What problem did CQRS actually solve? | Sprint 3 |

Out of scope, on purpose: hierarchy of strategic goals, execution team, execution plan documents,
approval workflow of the planning process itself, authentication. In the real system these existed;
they add size but no new design question.

## 9. Open points

- Snapshot frequency is monthly, one snapshot per task and period.
- Corrections: in legacy a snapshot can be corrected at any time and every update is published (see section 5).
  In action-service a snapshot can be corrected until it is approved; corrections after approval are out of scope.
- The snapshots of a task are reported in one system only (the department either still uses legacy or has moved),
  so concurrent corrections of the same snapshot on both sides are not expected. If they happen, the latest
  `occurredAt` wins.
- Deleting a snapshot in legacy is out of scope; the legacy app only inserts and updates.
- Origin systems are simulated by a small `origin-stub` that consumes `origin.*.progress` and logs what it receives.

# What is this project? (plain-language overview)

This document is for readers with no background. It starts with an everyday example and explains each technical
term where it is first needed.

## The example: a small web shop

Imagine a small web shop with three parts:

1. **The order desk** (`order-service`): customers open an order, add items, then place or cancel it.
2. **The warehouse** (`shipping-service`): staff pack the parcel, ship it and confirm delivery.
3. **The information board** (`query-service`): customers and managers see where each order is, what it costs,
   its tracking number, and how much was sold each day.

The order desk and the warehouse each keep **their own ledger** (their own database) and use their own words.
The order desk writes "status: P"; the warehouse writes "status: SHIPPED". Neither knows the other exists.

The question is: **how does information move between these three parts without tying them together?**

## The solution in one picture

```
 order desk ──► order ledger ─────┐
                                  │   (someone who reads the ledgers)
 warehouse ───► warehouse ledger ─┤          Debezium
                                  ▼
                       Kafka (the mailbox)
                                  │
                                  ▼
                     middleware (the coordinator)
                    │             │              │
                    ▼             ▼              ▼
             to warehouse   to order desk   information board
```

## What each part does, in plain words

**Database:** the ledger where each part writes its information.

**CDC (Change Data Capture):** "recording the ledger's changes". Instead of the order desk telling everyone after
every action, someone keeps watching the ledger and reports every line that is added or changed.
The tool that does this is **Debezium**.

*Why this way?* If the order desk had to write in its ledger *and* send a message to everyone else, one could succeed
and the other fail (say, the power goes out in between). Then the parts would disagree. This is called the
**dual-write problem**. With CDC the order desk only writes in its own ledger; everything else follows automatically.

**Kafka:** a large, reliable mailbox. Every change report stays there, in order, and anyone who needs it can read it,
later, or even from the very beginning.

**Middleware (the coordinator):** the only part that understands everyone's language. It does three things:

1. **Translates:** "P" becomes "placed", "20261113" becomes a date, "14900 öre" becomes "149 SEK".
2. **Builds the board's information:** puts the order together with the customer's name, the total and the
   warehouse's tracking number.
3. **Passes news between the order desk and the warehouse:** order placed → tell the warehouse to create a parcel;
   parcel shipped → tell the order desk.

It is built with **Kafka Streams** because it must **remember** things (all customers' names, all items of each order)
to combine information. A simple message reader has no memory.

**CQRS:** "keep the place you write separate from the place you read". The order desk and the warehouse are designed
for **correct writing**. The information board is designed for **fast reading** and has its own ledger where everything
is ready and in one place. The main reason here: an order's information is spread over **two** ledgers (the total in
the order ledger, the tracking number in the warehouse ledger). Neither can fully answer "where is my order?" on its
own. The board can.

## The story of one order

1. Sara opens an order at the order desk and adds two books. Debezium reports each change, and the board updates
   the total.
2. Sara **places** the order (status changes from O to P). The middleware notices and creates a parcel in the
   warehouse with **both** books. (Only once, and only when the order is complete; not once per book.)
3. The warehouse ships the parcel and records a tracking number. The middleware tells the order desk: status
   "shipped". The board now shows the tracking number too.

## The loop problem, and its solution

In step 3 the middleware wrote "shipped" in the order desk's ledger. But Debezium reports this like any other change!
Without care, the middleware thinks the order desk changed something and tells the warehouse again, the warehouse
changes again, and the cycle never ends.

**The solution:** whenever the middleware writes in a ledger, it signs the line: "written by: SYNC".
When Debezium reports that change, the middleware sees the signature and does **not** send it back. (It still updates
the board, because the board must show the correct status.)

Why must the signature be **inside the ledger itself**? Because Debezium only reads the ledger; any note we attached
to a message outside the ledger never reaches it.

## What if something goes wrong?

- **A message arrives twice:** each message carries the **full** new state of a board row, not "add one". Writing the
  same value twice changes nothing. Creating a parcel for an order that already has one is skipped too.
  This property is called **idempotency**.
- **A broken message arrives** (a negative quantity, the 13th month of the year): it goes to a separate mailbox called
  the **DLQ** (dead-letter queue) for later inspection, and the rest of the work continues.
- **The board breaks:** all changes are still in Kafka, so the board is emptied and rebuilt from the beginning
  (**replay**).
- **Delay:** the board and the warehouse are a few seconds behind the order desk. This is accepted
  (**eventual consistency**), and the delay is shown on a **Grafana** dashboard.

## Short glossary

| Term | Plain meaning |
|---|---|
| CDC | Automatic report of every change in a database |
| Debezium | The tool that does CDC |
| Kafka | A reliable mailbox that keeps messages in order |
| Kafka Streams | A library for processing messages with memory (combining and summing) |
| CQRS | Separating the model you write to from the model you read from |
| Read model | A separate ledger, ready for reading (the information board) |
| Loop | A change bouncing between two systems forever |
| Idempotency | Repeating a message does not change the result |
| DLQ | The mailbox for messages that could not be processed |
| Eventual consistency | Data becomes correct everywhere, a few seconds later |

For the technical details: [domain-model.md](domain-model.md)

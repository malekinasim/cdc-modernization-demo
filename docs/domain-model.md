# Domain model and design decisions

A small web shop. Two services own their own data, a read side answers the queries, and a Kafka Streams middleware
connects everything through CDC. The demo shows two mechanisms:

1. **CQRS with a CDC-fed read side.** The read model has a different shape from the write model.
2. **Two-way sync between two systems without loops.** Both systems change the same order.

The middleware is only justified if the models are really different, so the models come first.

## 1. order-service (command side)

Normalized, with a few habits of an older system: code columns, dates as strings, prices in öre.

```
CUSTOMER    (CUST_ID, FIRST_NM, LAST_NM, EMAIL, CITY)
PRODUCT     (PROD_ID, SKU, PROD_NM, UNIT_PRICE_ORE)
ORDERS      (ORD_ID, ORD_NO 'ORD-000123', CUST_ID → CUSTOMER,
             STATUS_CD 'O' open | 'P' placed | 'X' cancelled | 'S' shipped | 'D' delivered,
             ORD_DT '20261113', LAST_UPD_BY, LAST_UPD_TS)
ORDER_LINE  (LINE_ID, ORD_ID → ORDERS, PROD_ID → PRODUCT, QTY, UNIT_PRICE_ORE)
```

The customer builds an order while it is `O` (lines are added one by one), then places it (`P`) or cancels it (`X`).
`order-service` has no Kafka code and publishes no events.

## 2. shipping-service (a second system)

Owned by "another team", with its own model and its own words:

```
shipment       (id UUID, order_ref 'ORD-000123', recipient_name, city,
                status PENDING | PICKING | SHIPPED | DELIVERED | CANCELLED,
                carrier, tracking_no, shipped_at TIMESTAMPTZ, delivered_at TIMESTAMPTZ,
                updated_by, updated_at)
shipment_item  (shipment_id, sku, quantity)
```

The warehouse picks, ships and confirms delivery in this service.

## 3. Read model (query-service)

Denormalized, one table per screen:

```
order_summary
  order_no, customer_name, city, order_date DATE, status 'PLACED'|'SHIPPED'|...,
  item_count, total_sek NUMERIC, carrier, tracking_no, shipped_at

daily_sales
  sales_date, sku, product_name, quantity, revenue_sek
```

`order_summary` combines data from **both** services: the order and its total from `order-service`,
carrier and tracking number from `shipping-service`. No single write database can answer
"show me my order with its tracking number", only the read model can. That is the reason for CQRS here.

## 4. How different the models are

| Write side | Read side / other system | Who closes the gap |
|---|---|---|
| `STATUS_CD 'P'`, `ORD_DT '20261113'`, `UNIT_PRICE_ORE 14900` | `'PLACED'`, `DATE`, `149.00 SEK` | translation |
| Customer is an ID in `ORDERS` | Name and city in the row | join with `CUSTOMER` |
| Product is an ID in `ORDER_LINE` | SKU and product name | join with `PRODUCT` |
| One row per order line | Item count and total per order, sales per product and day | aggregation |
| Order lines arrive one by one while the order is open | One shipment with all its items, created once | business event from a status change |
| Shipping status lives in another database | Carrier and tracking in `order_summary` | join across systems |

## 5. The two-way sync

| Direction | Trigger | Effect |
|---|---|---|
| order → shipping | Order goes from `O` to `P` | Create a shipment with recipient and all items |
| order → shipping | Order goes to `X` | Shipment becomes `CANCELLED` (only if not shipped yet) |
| shipping → order | Shipment becomes `SHIPPED` or `DELIVERED` | `ORDERS.STATUS_CD` becomes `S` or `D` |
| (not synced) | `PICKING` | Has no counterpart in the order model; lossy mapping on purpose |

The middleware writes into each service through that service's REST API, so each service keeps its own rules.

### The loop

1. The warehouse ships: `shipment.status = SHIPPED`.
2. CDC on the shipping DB → middleware → `order-service` sets `STATUS_CD = 'S'`.
3. CDC on the order DB sees an update on `ORDERS` → middleware → sends the status to `shipping-service` again
   → CDC on the shipping DB → ... forever.

A `source` header in Kafka does not help: the change in step 3 starts in the database, and CDC events do not carry
headers from earlier hops. The origin has to be **in the data**:

- every write done by the sync sets `LAST_UPD_BY = 'SYNC'` (order DB) or `updated_by = 'SYNC'` (shipping DB);
- the middleware drops CDC events with that value before routing them to the other system;
- the same events still go to the read model, so the read side stays correct.

## 6. Why the middleware is Kafka Streams

- **Why not let the services write to each other and to the read DB?** Every change would need extra writes to other
  systems (the dual-write problem), and each service would have to know the others' models. With CDC the services
  stay simple and know nothing about each other.
- **Why not a plain Kafka consumer?** Debezium gives one topic per table. Joins (orders with customers, lines with
  products), aggregation (totals, daily sales) and "create the shipment once, when the order is placed, with all its
  lines" all need state: KTables and aggregates. That is what Kafka Streams is for.

What the middleware does:

1. Unwraps the Debezium envelopes and translates codes, dates and prices.
2. Keeps KTables of customers, products, orders, order lines and shipments.
3. Builds and publishes the read model rows, keyed by their primary keys.
4. Routes changes between the two services, and filters out changes made by the sync (loop prevention).
5. Sends records it cannot process (unknown product, negative quantity, date `'20261332'`) to a dead-letter topic
   and keeps going.

## 7. Design decisions

- **Upsert, keyed by the read model's primary key.** Every output record is the full new state of a read row, so
  applying it twice changes nothing. Redelivered messages are harmless.
- **Idempotent sync.** "Create shipment for `ORD-000123`" is ignored if the shipment already exists; setting a status
  to the value it already has is a no-op. A redelivered sync message changes nothing.
- **The read model can be rebuilt** by resetting the middleware, emptying the read DB and replaying the CDC topics.
- **Eventual consistency is accepted.** The read side and the other system are a few seconds behind; consumer lag is
  measured and shown in Grafana.

## 8. Out of scope

Payments, stock levels, returns, partial shipments, authentication. They would add size but no new design question.

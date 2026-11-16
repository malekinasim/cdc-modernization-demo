-- Customers and products for the demo. This service has no API to create them.

INSERT INTO CUSTOMER (FIRST_NM, LAST_NM, EMAIL, CITY, LAST_UPD_BY, LAST_UPD_TS)
VALUES ('Sara', 'Lind', 'sara.lind@example.com', 'Stockholm', 'SEED', now()),
       ('Omid', 'Rad', 'omid.rad@example.com', 'Uppsala', 'SEED', now()),
       ('Eva', 'Berg', 'eva.berg@example.com', 'Goteborg', 'SEED', now());

INSERT INTO PRODUCT (SKU, PROD_NM, UNIT_PRICE_ORE, LAST_UPD_BY, LAST_UPD_TS)
VALUES ('MUG-500ML', 'Coffee mug 500 ml', 14900, 'SEED', now()),
       ('TSHIRT-BLK-L', 'T-shirt black, size L', 24900, 'SEED', now()),
       ('NOTEBOOK-A5', 'Notebook A5, dotted', 7900, 'SEED', now()),
       ('LAMP-DESK', 'Desk lamp, warm white', 39900, 'SEED', now()),
       ('CABLE-USBC-2M', 'USB-C cable 2 m', 12900, 'SEED', now());

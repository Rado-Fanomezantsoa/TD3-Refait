CREATE TABLE "table" (
                                  id SERIAL PRIMARY KEY,
                                  number INTEGER NOT NULL UNIQUE
);
ALTER TABLE order
    ADD COLUMN table_id INTEGER NOT NULL,
    ADD COLUMN arrival_datetime TIMESTAMP NOT NULL,
    ADD COLUMN departure_datetime TIMESTAMP NOT NULL;

ALTER TABLE order
    ADD CONSTRAINT fk_customer_order_table
        FOREIGN KEY (table_id)
            REFERENCES "table"(id);

INSERT INTO "table" (number) VALUES (1), (2), (3);

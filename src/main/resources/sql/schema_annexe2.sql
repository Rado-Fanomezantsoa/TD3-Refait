CREATE TABLE customer_order (
                                id SERIAL PRIMARY KEY,
                                reference VARCHAR(20) UNIQUE NOT NULL,
                                creation_datetime TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE dish_order (
                            id SERIAL PRIMARY KEY,
                            id_order INTEGER NOT NULL REFERENCES customer_order(id),
                            id_dish INTEGER NOT NULL REFERENCES dish(id),
                            quantity INTEGER NOT NULL CHECK (quantity > 0),
                            CONSTRAINT uq_order_dish UNIQUE (id_order, id_dish)
);
CREATE SEQUENCE order_reference_seq START 1;
ALTER TABLE public.dish DROP COLUMN IF EXISTS selling_price;


alter table ingredient
    add column if not exists initial_stock numeric(10, 2);

create table if not exists "order"
(
    id                serial primary key,
    reference         varchar(255),
    creation_datetime timestamp without time zone
);

create table if not exists dish_order
(
    id       serial primary key,
    id_order int references "order" (id),
    id_dish  int references dish (id),
    quantity int
);
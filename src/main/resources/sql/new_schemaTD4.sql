alter table public.ingredient drop column if exists required_quantity;
CREATE TYPE movement_type as enum('IN', 'OUT');
CREATE table stock_movement(
    id serial primary key,
    id_ingredient int references ingredient(id),
    quantity numeric,
    type movement_type,
    unit unit_type,
    creation_datetime timestamp
);

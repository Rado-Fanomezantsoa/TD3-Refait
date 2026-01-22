create type dish_type as enum ('STARTER', 'MAIN', 'DESSERT');


create table dish
(
    id        serial primary key,
    name      varchar(255),
    dish_type dish_type
);

ALTER TABLE dish
    ADD COLUMN IF NOT EXISTS selling_price numeric(10,2);

update dish
set selling_price = 2000.00
where name = 'Salade fraîche';

update dish
set selling_price = 6000.00
where name = 'Poulet grillé';

update dish
set selling_price = null
where name = 'Riz au légumes';

update dish
set selling_price = 8000.00
where name = 'Gâteau au chocolat';

update dish
set selling_price = null
where name = 'Salade de fruit'

create type ingredient_category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

create table ingredient
(
    id       serial primary key,
    name     varchar(255),
    price    numeric(10, 2),
    category ingredient_category,
    id_dish  int references dish (id)
);

alter table dish
    add column if not exists price numeric(10, 2);


alter table ingredient
    add column if not exists required_quantity numeric(10, 2);
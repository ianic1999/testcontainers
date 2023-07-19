create table customer
(
    id         bigserial primary key,
    username   varchar(255) not null unique,
    first_name varchar(255),
    last_name  varchar(255),
    active     boolean default true
);

create table product
(
    id       bigserial primary key,
    code     varchar(255) not null unique,
    name     varchar(255),
    price    real,
    in_stock boolean default true,
    category varchar(255)
);

create table customer_favorite_product
(
    customer_id bigint references customer (id),
    product_id  bigint references product (id),
    constraint customer_products_pk primary key (customer_id, product_id)
);

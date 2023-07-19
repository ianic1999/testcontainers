create table product
(
    id       bigserial primary key,
    code     varchar(255) not null unique,
    name     varchar(255),
    price    real,
    in_stock boolean default true,
    category varchar(255)
);

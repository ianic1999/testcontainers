create table customer
(
    id         bigserial primary key,
    username   varchar(255) not null unique,
    first_name varchar(255),
    last_name  varchar(255),
    active     boolean default true
);

create table customer_favorite_product
(
    customer_id bigint references customer (id),
    product_id  bigint references product (id),
    constraint customer_products_pk primary key (customer_id, product_id)
);

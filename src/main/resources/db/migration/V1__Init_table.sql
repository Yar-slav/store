drop table if exists carts;
drop table if exists products;
drop table if exists users;

create table carts
(
    id         bigint not null auto_increment,
    quantity   bigint,
    product_id bigint,
    user_id    bigint,
    primary key (id)
);

create table products
(
    id        bigint    not null auto_increment,
    available bigint,
    price     float(53) not null,
    title     varchar(255),
    primary key (id)
);

create table users
(
    id       bigint not null auto_increment,
    email    varchar(255) not null,
    password varchar(255) not null,
    role     varchar(255),
    primary key (id)
);

alter table carts
    add constraint carts_product_fk
        foreign key (product_id) references products (id);

alter table carts
    add constraint carts_user_fk
        foreign key (user_id) references users (id);
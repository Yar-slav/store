ALTER TABLE carts
    ADD status VARCHAR(255) NOT NULL,
    ADD order_id BIGINT,
    ADD ordered_on DATETIME,
    ADD canceled_on DATETIME;
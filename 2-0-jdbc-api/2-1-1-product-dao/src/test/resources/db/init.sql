CREATE TABLE IF NOT EXISTS products
(
    id              SERIAL       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    producer        VARCHAR(255) NOT NULL,
    price           DECIMAL(19, 4),
    expiration_date TIMESTAMP    NOT NULL,
    creation_time   TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT products_pk PRIMARY KEY (id)
);


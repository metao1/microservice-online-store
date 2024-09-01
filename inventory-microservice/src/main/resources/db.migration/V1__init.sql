CREATE TABLE IF NOT EXISTS product
(
  id             BYTEA        NOT NULL,
  version        BIGINT,
  asin           VARCHAR(255) NOT NULL,
  volume         DECIMAL,
  title          VARCHAR(255) NOT NULL,
  description    VARCHAR(1200),
  image          BYTEA        NOT NULL,
  price_value    DECIMAL      NOT NULL,
  price_currency VARCHAR(255) NOT NULL,
  CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE product_category
(
  id       BYTEA NOT NULL,
  category BYTEA NOT NULL,
  CONSTRAINT pk_product_category PRIMARY KEY (id)
);

CREATE TABLE product_category_map
(
  product_category_id BYTEA NOT NULL,
  product_id          BYTEA NOT NULL,
  CONSTRAINT pk_product_category_map PRIMARY KEY (product_category_id, product_id)
);

ALTER TABLE product
  ADD CONSTRAINT uc_product_asin UNIQUE (asin);

ALTER TABLE product_category_map
  ADD CONSTRAINT fk_procatmap_on_product_category_entity FOREIGN KEY (product_category_id) REFERENCES product_category (id);

ALTER TABLE product_category_map
  ADD CONSTRAINT fk_procatmap_on_product_entity FOREIGN KEY (product_id) REFERENCES product (id);
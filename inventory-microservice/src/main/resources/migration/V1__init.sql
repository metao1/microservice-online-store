CREATE TABLE product_table (
  id             BIGINT        PRIMARY KEY,
  asin           VARCHAR(10)   UNIQUE NOT NULL,
  version        BIGINT        NOT NULL,
  volume         DECIMAL,
  title          TEXT          NOT NULL,
  description    TEXT          NOT NULL,
  image_url      VARCHAR(2500) NOT NULL,
  price_value    DECIMAL       NOT NULL,
  price_currency VARCHAR(255)  NOT NULL,
  created_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE product_category
(
  id       BIGINT PRIMARY KEY,
  category VARCHAR(255) NOT NULL
);

CREATE TABLE product_category_map
(
  product_category_id BIGINT       NOT NULL,
  product_asin        VARCHAR(10)  NOT NULL,

  PRIMARY KEY (product_category_id, product_asin),

  CONSTRAINT fk_procatmap_category
    FOREIGN KEY (product_category_id)
      REFERENCES product_category (id),

  CONSTRAINT fk_procatmap_product
    FOREIGN KEY (product_asin)
      REFERENCES product_table (asin)
);
CREATE TABLE IF NOT EXISTS product_table (
  id             BIGINT        PRIMARY KEY,
  asin           UUID          UNIQUE NOT NULL,
  version        BIGINT        NOT NULL,
  volume         DECIMAL,
  title          VARCHAR(255)  NOT NULL,
  description    VARCHAR(2500),
  image_url       VARCHAR(2500) NOT NULL,
  price_value    DECIMAL       NOT NULL,
  price_currency VARCHAR(255)  NOT NULL,
  created_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_category
(
  id       BIGINT PRIMARY KEY,
  category VARCHAR(255) NOT NULL
);

CREATE TABLE product_category_map
(
  product_category_id BIGINT       NOT NULL,
  product_asin        UUID         NOT NULL,

  PRIMARY KEY (product_category_id, product_asin),

  CONSTRAINT fk_procatmap_category
    FOREIGN KEY (product_category_id)
      REFERENCES product_category (id),

  CONSTRAINT fk_procatmap_product
    FOREIGN KEY (product_asin)
      REFERENCES product_table (asin)
);
CREATE TABLE IF NOT EXISTS product_table
(
  sku             VARCHAR(255) PRIMARY KEY,
  version        BIGINT             NOT NULL,
  volume         DECIMAL,
  title          TEXT               NOT NULL,
  description    TEXT               NOT NULL,
  image_url      VARCHAR(2500)      NOT NULL,
  price_value    DECIMAL            NOT NULL,
  price_currency VARCHAR(255)       NOT NULL,
  created_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE product_category
(
  id       VARCHAR(255) PRIMARY KEY,
  category VARCHAR(255) NOT NULL
);

CREATE TABLE product_category_map
(
  product_category_id VARCHAR(255)      NOT NULL,
  product_sku         VARCHAR(255) NOT NULL,

  PRIMARY KEY (product_category_id, product_sku),

  CONSTRAINT fk_procatmap_category
    FOREIGN KEY (product_category_id)
      REFERENCES product_category (id),

  CONSTRAINT fk_procatmap_product
    FOREIGN KEY (product_sku)
      REFERENCES product_table (sku)
);
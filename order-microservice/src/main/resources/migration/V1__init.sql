CREATE TABLE IF NOT EXISTS order_table
(
  id            BYTEA                       NOT NULL,
  product_id    VARCHAR(255)                NOT NULL,
  customer_id   VARCHAR(255)                NOT NULL,
  quantity      DECIMAL                     NOT NULL,
  currency      VARCHAR(255)                NOT NULL,
  price         DECIMAL                     NOT NULL,
  status        VARCHAR(255)                NOT NULL,
  created_time  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_order_table PRIMARY KEY (id)
);

ALTER TABLE order_table ADD COLUMN order_id VARCHAR(255) NOT NULL default '';
CREATE INDEX order_id_index ON order_table (product_id);
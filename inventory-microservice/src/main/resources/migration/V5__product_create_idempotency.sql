CREATE TABLE IF NOT EXISTS product_create_request
(
  idempotency_key VARCHAR(255) PRIMARY KEY,
  sku             VARCHAR(255) NOT NULL,
  created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_create_request_sku ON product_create_request(sku);

DROP INDEX IF EXISTS idx_payment_order_id;
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_order_id ON payment (order_id);

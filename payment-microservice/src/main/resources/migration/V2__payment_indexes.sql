CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment (status);

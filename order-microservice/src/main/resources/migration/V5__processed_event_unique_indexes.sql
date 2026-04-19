CREATE UNIQUE INDEX IF NOT EXISTS uk_processed_payment_event_event_id
    ON processed_payment_event (event_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_processed_order_created_event_event_id
    ON processed_order_created_event (event_id);

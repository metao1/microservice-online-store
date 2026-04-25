CREATE UNIQUE INDEX IF NOT EXISTS uk_processed_order_created_event_event_id
    ON processed_order_created_event (event_id);

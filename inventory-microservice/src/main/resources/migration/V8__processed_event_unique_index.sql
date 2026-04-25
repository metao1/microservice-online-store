CREATE UNIQUE INDEX IF NOT EXISTS uk_processed_inventory_event_event_id
    ON processed_inventory_event (event_id);

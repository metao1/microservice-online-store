ALTER TABLE domain_event_outbox
    ADD COLUMN ordering_key VARCHAR(255);

CREATE INDEX ix_domain_event_outbox_ordering
    ON domain_event_outbox (ordering_key, status, occurred_at);

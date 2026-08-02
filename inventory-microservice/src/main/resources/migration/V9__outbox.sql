CREATE TABLE domain_event_outbox (
    event_id VARCHAR(255) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    schema_version INTEGER NOT NULL,
    partition_key VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    lease_owner VARCHAR(255),
    lease_until TIMESTAMP WITH TIME ZONE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX ix_domain_event_outbox_pending
    ON domain_event_outbox (status, next_attempt_at, occurred_at);

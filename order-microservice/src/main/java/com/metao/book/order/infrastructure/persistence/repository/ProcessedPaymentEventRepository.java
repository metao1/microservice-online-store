package com.metao.book.order.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcessedPaymentEventRepository {

    private static final String INSERT_IF_ABSENT_SQL = """
        INSERT INTO processed_payment_event(event_id, processed_at)
        VALUES (?, now())
        ON CONFLICT (event_id) DO NOTHING
        """;

    private final JdbcTemplate jdbcTemplate;

    public boolean markProcessed(String eventId) {
        return jdbcTemplate.update(INSERT_IF_ABSENT_SQL, eventId) > 0;
    }
}

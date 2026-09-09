package com.metao.book.outbox.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.outbox.infrastructure.OutboxMessagingAutoConfiguration;
import com.metao.book.shared.security.JwtSecurityAutoConfiguration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = JpaOutboxStoreIT.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///outbox-tests",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaOutboxStoreIT {

    private static final Instant NOW = Instant.parse("2026-09-09T10:00:00Z");

    @Autowired
    private OutboxStore store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearOutbox() {
        jdbcTemplate.update("delete from domain_event_outbox");
    }

    @Test
    void claimsOnlyOldestRecordPerOrderedKeyAndAllUnorderedRecords() {
        store.append(message("ordered-1", NOW, "order-1"));
        store.append(message("ordered-2", NOW.plusSeconds(1), "order-1"));
        store.append(message("other-order", NOW.plusSeconds(2), "order-2"));
        store.append(message("unordered", NOW.plusSeconds(3), null));

        var claimed = store.claimPending("worker", 10, NOW.plusSeconds(10), NOW.plusSeconds(70));

        assertThat(claimed).extracting(OutboxMessage::eventId)
            .containsExactly("ordered-1", "other-order", "unordered");

        store.markPublished("ordered-1", "worker", NOW.plusSeconds(11));
        var nextClaim = store.claimPending("worker-2", 10, NOW.plusSeconds(12), NOW.plusSeconds(72));

        assertThat(nextClaim).extracting(OutboxMessage::eventId).containsExactly("ordered-2");
    }

    @Test
    void retryDelayedHeadBlocksLaterRecordWithSameOrderingKey() {
        store.append(message("ordered-1", NOW, "order-1"));
        store.append(message("ordered-2", NOW.plusSeconds(1), "order-1"));
        store.claimPending("worker", 1, NOW.plusSeconds(10), NOW.plusSeconds(70));
        store.rescheduleFailure("ordered-1", "worker", NOW.plusSeconds(60), "temporary failure");

        var claimed = store.claimPending("other-worker", 10, NOW.plusSeconds(20), NOW.plusSeconds(80));

        assertThat(claimed).isEmpty();
    }

    @Test
    void leasedHeadBlocksLaterRecordWithSameOrderingKey() {
        store.append(message("ordered-1", NOW, "order-1"));
        store.append(message("ordered-2", NOW.plusSeconds(1), "order-1"));
        store.claimPending("worker", 1, NOW.plusSeconds(10), NOW.plusSeconds(70));

        var claimed = store.claimPending("other-worker", 10, NOW.plusSeconds(20), NOW.plusSeconds(80));

        assertThat(claimed).isEmpty();
    }

    private OutboxMessage message(String eventId, Instant occurredAt, String orderingKey) {
        return new OutboxMessage(
            eventId,
            "order",
            "order-1",
            "order.created",
            1,
            "order-1",
            new byte[] {1},
            occurredAt,
            orderingKey
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
        OutboxMessagingAutoConfiguration.class,
        JwtSecurityAutoConfiguration.class
    })
    static class TestApplication {
    }
}

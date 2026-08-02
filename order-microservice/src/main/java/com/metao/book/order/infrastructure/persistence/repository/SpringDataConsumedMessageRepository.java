package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.infrastructure.persistence.entity.ConsumedMessageId;
import com.metao.book.order.infrastructure.persistence.entity.ConsumedMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataConsumedMessageRepository
    extends JpaRepository<ConsumedMessageJpaEntity, ConsumedMessageId> {

    @Modifying
    @Query(value = """
        INSERT INTO consumed_message (consumer_name, event_id, processed_at)
        VALUES (:consumerName, :eventId, CURRENT_TIMESTAMP)
        ON CONFLICT (consumer_name, event_id) DO NOTHING
        """, nativeQuery = true)
    int claim(@Param("consumerName") String consumerName, @Param("eventId") String eventId);
}

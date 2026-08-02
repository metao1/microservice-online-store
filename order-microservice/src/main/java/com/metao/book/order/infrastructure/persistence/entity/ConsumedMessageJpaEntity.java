package com.metao.book.order.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consumed_message")
@IdClass(ConsumedMessageId.class)
@Getter
@Setter
@NoArgsConstructor
public class ConsumedMessageJpaEntity {

    @Id
    @Column(name = "consumer_name", nullable = false, updatable = false)
    private String consumerName;

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    public ConsumedMessageJpaEntity(String consumerName, String eventId, Instant processedAt) {
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.processedAt = processedAt;
    }
}

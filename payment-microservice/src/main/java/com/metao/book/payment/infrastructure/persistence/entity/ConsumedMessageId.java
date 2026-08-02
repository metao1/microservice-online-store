package com.metao.book.payment.infrastructure.persistence.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
public class ConsumedMessageId implements Serializable {

    private String consumerName;
    private String eventId;

    public ConsumedMessageId(String consumerName, String eventId) {
        this.consumerName = consumerName;
        this.eventId = eventId;
    }
}

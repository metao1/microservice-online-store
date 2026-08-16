package com.metao.book.shared.application.messaging;

import com.metao.book.shared.domain.base.DomainEvent;

public interface DomainEventPublisherPort {

    void publish(DomainEvent event);
}
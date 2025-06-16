package com.metao.book.order.domain.event;

import com.metao.book.shared.domain.base.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
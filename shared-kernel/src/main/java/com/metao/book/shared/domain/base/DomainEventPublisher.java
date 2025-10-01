package com.metao.book.shared.domain.base;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
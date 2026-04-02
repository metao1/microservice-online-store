package com.metao.book.order.application.usecase;

import com.metao.book.order.domain.event.OrderCreatedEvent;

public interface PersistOrderUseCase {

    void persistOrder(OrderCreatedEvent event);
}

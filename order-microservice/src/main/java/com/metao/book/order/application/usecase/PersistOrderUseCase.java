package com.metao.book.order.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.order.domain.event.OrderCreatedEvent;

@ApplicationUseCase
public interface PersistOrderUseCase {

    void persistOrder(OrderCreatedEvent event);
}

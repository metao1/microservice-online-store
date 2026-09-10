package com.metao.book.order.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;

@ApplicationUseCase
public interface CreateOrderUseCase {

    OrderId createOrder(UserId userId);
}

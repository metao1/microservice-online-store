package com.metao.book.order.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;

@ApplicationUseCase
public interface UpdateOrderStatusUseCase {

    void updateOrderStatus(OrderId orderId, OrderStatus status);
}

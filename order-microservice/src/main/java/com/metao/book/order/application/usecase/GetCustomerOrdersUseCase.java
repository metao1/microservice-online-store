package com.metao.book.order.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.UserId;
import java.util.List;
import org.springframework.data.domain.Page;

@ApplicationUseCase
public interface GetCustomerOrdersUseCase {

    List<OrderAggregate> getCustomerOrders(UserId userId);

    Page<OrderAggregate> getCustomerOrders(UserId userId, int offset, int limit);
}

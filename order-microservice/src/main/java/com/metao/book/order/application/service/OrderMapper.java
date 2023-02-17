package com.metao.book.order.application.service;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderMapper implements OrderMapperInterface {

    public OrderEntity toEntity(OrderEvent order) {
        final Currency currency = OrderMapperInterface.convertToCurrency(order.getCurrency());
        final Status status = OrderMapperInterface.convertStatus(order.getStatus());
        var money = new Money(currency, BigDecimal.valueOf(order.getPrice()));
        return new OrderEntity(order.getOrderId(),
            order.getCustomerId(),
            order.getProductId(),
            BigDecimal.valueOf(order.getQuantity()),
            money,
            status);
    }

}

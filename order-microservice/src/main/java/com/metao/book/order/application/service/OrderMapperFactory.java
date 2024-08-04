package com.metao.book.order.application.service;

import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderMapperFactory extends OrderMapper {

    @Override
    public <T> OrderEntity toEntity(T order) {
        OrderCreatedEvent oce = (OrderCreatedEvent) order;
        var money = new Money(Currency.getInstance(oce.getCurrency()), BigDecimal.valueOf(oce.getPrice()));
        return new OrderEntity(
            oce.getCustomerId(),
            oce.getProductId(),
            BigDecimal.valueOf(oce.getQuantity()),
            money,
            switch (oce.getStatus()) {
                case SUBMITTED -> OrderStatus.SUBMITTED;
                case REJECTED -> OrderStatus.REJECTED;
                case NEW -> OrderStatus.NEW;
                case CONFIRMED -> OrderStatus.CONFIRMED;
                case ROLLED_BACK -> OrderStatus.ROLLED_BACK;
                case UNRECOGNIZED -> null;
            }
        );
    }
}

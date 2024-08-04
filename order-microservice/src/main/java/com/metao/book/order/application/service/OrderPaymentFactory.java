package com.metao.book.order.application.service;

import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.util.Currency;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentFactory extends OrderMapper {

    @Override
    public <T> OrderEntity toEntity(T order) {
        OrderPaymentEvent oce = (OrderPaymentEvent) order;
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

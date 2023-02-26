package com.metao.book.shared.application.service.order;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.application.service.Validator;
import com.metao.book.shared.domain.financial.Currency;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
public class OrderValidator implements Validator<OrderEvent> {

    @Override
    public void validate(OrderEvent order) throws RuntimeException {
        Objects.requireNonNull(order, "order can't be null");
        Objects.requireNonNull(order.getStatus(), "order status can't be null");
        Arrays.stream(Status.values())
            .filter(o -> !o.equals(order.getStatus()))
            .findAny()
            .orElseThrow(() -> new RuntimeException("provided status can't be found"));
        Objects.requireNonNull(order.getCurrency(), "currency can't be null");
        Currency.valueOf(order.getCurrency().name().toUpperCase());
    }
}

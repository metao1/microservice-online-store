package com.metao.book.order.domain;

import com.metao.book.order.application.dto.OrderCreatedEvent;
import com.metao.book.shared.application.service.Validator;
import com.metao.book.shared.domain.order.OrderStatus;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class OrderEventValidator implements Validator<OrderCreatedEvent> {

    @Override
    public void validate(OrderCreatedEvent order) throws RuntimeException {
        Arrays.stream(OrderStatus.values())
            .findAny()
            .orElseThrow(() -> new RuntimeException("provided status can't be found"));
        Objects.requireNonNull(order.currency(), "currency can't be null");
    }
}

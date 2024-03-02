package com.metao.book.order.domain;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.shared.application.service.Validator;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class OrderEventValidator implements Validator<OrderDTO> {

    @Override
    public void validate(OrderDTO order) throws RuntimeException {
        Arrays.stream(Status.values())
            .findAny()
            .orElseThrow(() -> new RuntimeException("provided status can't be found"));
        Objects.requireNonNull(order.currency(), "currency can't be null");
    }
}

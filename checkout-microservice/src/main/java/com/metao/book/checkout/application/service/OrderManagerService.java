package com.metao.book.checkout.application.service;

import com.metao.book.checkout.domain.CustomerEntity;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManagerService {

    private final RemoteOrderService remoteOrderService;

    public void reserve(OrderEvent order, CustomerEntity customer) {
        if (enoughBudget(order, customer)) {
            acceptOrder(order, customer);
        } else {
            order.setStatus(Status.REJECT);
        }
        try {
            remoteOrderService.handle(order);
        } catch (Exception ex) {
            log.error("Could not save product entity, {}", ex.getMessage());
        }
    }

    public void confirm(OrderEvent order, CustomerEntity customer) {
        switch (order.getStatus()) {
            case CONFIRM -> confirmOrder(order, customer);
            case ROLLBACK -> rollbackOrder(order, customer);
            default -> throw new RuntimeException(String.format("status: %s, is not allowed.", order));
        }
    }

    void rollbackOrder(OrderEvent order, CustomerEntity customer) {
        var available = customer.getAmountAvailable();
        var reserved = customer.getAmountReserved();
        var price = BigDecimal.valueOf(order.getPrice());
        customer.setAmountAvailable(available.add(price));
        customer.setAmountReserved(reserved.subtract(price));
    }

    void acceptOrder(OrderEvent order, CustomerEntity customer) {
        var reserved = customer.getAmountReserved();
        var quantity = BigDecimal.valueOf(order.getPrice());
        customer.setAmountReserved(reserved.add(quantity));
        customer.setAmountAvailable(reserved.subtract(quantity));
        order.setStatus(Status.ACCEPT);
    }

    public void confirmOrder(OrderEvent order, CustomerEntity customer) {
        var reserved = customer.getAmountReserved();
        var quantity = BigDecimal.valueOf(order.getPrice());
        customer.setAmountReserved(reserved.add(quantity));
        customer.setAmountAvailable(reserved.subtract(quantity));
        order.setStatus(Status.CONFIRM);
    }

    private static boolean enoughBudget(OrderEvent order, CustomerEntity customer) {
        return order.getPrice() <= customer.getAmountAvailable().doubleValue();
    }
}

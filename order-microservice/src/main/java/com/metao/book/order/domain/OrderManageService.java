package com.metao.book.order.domain;

import org.springframework.stereotype.Service;

@Service
public class OrderManageService {

    public OrderEntity confirm(OrderEntity orderPayment, OrderEntity orderStock) {
        OrderEntity order = null;
        if ((orderPayment.status().equals(orderStock.status()) && orderPayment.status() == Status.ACCEPT)) {
           order = OrderEntity.of(Status.CONFIRM, orderPayment);
        } else if (orderPayment.status().equals(orderStock.status()) && orderPayment.status() == Status.REJECT ){
            order = OrderEntity.of(Status.REJECT, orderPayment);
        }else if (orderPayment.status().equals(Status.REJECT)){
            order = OrderEntity.of(Status.ROLLBACK, orderPayment);
        } else {
            order = OrderEntity.of(Status.ROLLBACK, orderPayment);
        }
        return order;
    }
}

package com.metao.book.order.domain;

import com.metao.book.order.application.dto.OrderDTO;

import org.springframework.stereotype.Service;

@Service
public class OrderManageService {

    public OrderDTO confirm(OrderDTO orderPayment, OrderDTO orderStock) {
        OrderDTO order = orderPayment;
        if ((orderPayment.getStatus().equals(orderStock.getStatus()) && orderPayment.getStatus() == Status.ACCEPT)) {
           order.setStatus(Status.CONFIRM);
        } else if (orderPayment.getStatus().equals(orderStock.getStatus()) && orderPayment.getStatus() == Status.REJECT ){
            order.setStatus(Status.REJECT);
        }else if (orderPayment.getStatus().equals(Status.REJECT)){
            order.setStatus(Status.ROLLBACK);
        } else {
            order.setStatus(Status.ROLLBACK);
        }
        return order;
    }
}

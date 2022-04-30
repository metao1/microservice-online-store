package com.metao.book.order.domain;

import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Status;
import org.springframework.stereotype.Service;

@Service
public class OrderManageService {

    public OrderAvro confirm(OrderAvro orderPayment, OrderAvro orderStock) {
        OrderAvro order = orderPayment;
        if ((orderPayment.getStatus().equals(orderStock.getStatus()) && orderPayment.getStatus() == Status.ACCEPT)) {
            order.setStatus(Status.CONFIRM);
        } else if (orderPayment.getStatus().equals(orderStock.getStatus())
                && orderPayment.getStatus() == Status.REJECT) {
            order.setStatus(Status.REJECT);
        } else if (orderPayment.getStatus().equals(Status.REJECT)) {
            order.setStatus(Status.ROLLBACK);
        } else {
            order.setStatus(Status.ROLLBACK);
        }
        return order;
    }
}

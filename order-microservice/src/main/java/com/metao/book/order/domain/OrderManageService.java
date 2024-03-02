package com.metao.book.order.domain;

import com.google.protobuf.Timestamp;
import com.metao.book.OrderEventOuterClass.OrderEvent;
import org.springframework.stereotype.Service;

@Service
public class OrderManageService {

    public OrderEvent confirm(
        OrderEvent orderPayment,
        OrderEvent orderStock
    ) {
        var newOrder = OrderEvent.newBuilder()
            .setId(orderPayment.getId())
            .setAccountId(orderPayment.getAccountId())
            .setProductId(orderPayment.getProductId())
            .setCreateTime(Timestamp.getDefaultInstance())
            .setStatus(OrderEvent.Status.SUBMITTED)
            .setQuantity(orderPayment.getQuantity())
            .setPrice(orderPayment.getPrice())
            .setCurrency(orderPayment.getCurrency());
        if (orderPayment.getStatus().equals(OrderEvent.Status.SUBMITTED) &&
            orderStock.getStatus().equals(OrderEvent.Status.SUBMITTED)) {
            newOrder.setStatus(OrderEvent.Status.CONFIRMED);
        } else if (orderPayment.getStatus().equals(OrderEvent.Status.REJECTED) &&
            orderStock.getStatus().equals(OrderEvent.Status.REJECTED)) {
            newOrder.setStatus(OrderEvent.Status.REJECTED);
        } else if (orderPayment.getStatus().equals(OrderEvent.Status.REJECTED) ||
            orderStock.getStatus().equals(OrderEvent.Status.REJECTED)) {
            newOrder.setStatus(OrderEvent.Status.ROLLED_BACK);
        }
        return newOrder.build();
    }

}
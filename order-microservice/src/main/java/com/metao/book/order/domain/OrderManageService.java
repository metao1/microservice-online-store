package com.metao.book.order.domain;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class OrderManageService {

    public OrderEvent confirm(OrderEvent orderPayment, OrderEvent orderStock) {
        var o = OrderEvent.newBuilder()
            .setOrderId(orderPayment.getOrderId())
            .setCustomerId(orderPayment.getCustomerId())
            .setProductId(orderPayment.getProductId())
            .setCreatedOn(Instant.now().toEpochMilli())
            .setStatus(Status.ACCEPT)
            .setQuantity(orderPayment.getQuantity())
            .setPrice(orderPayment.getPrice())
            .setSource(orderPayment.getSource())
            .setCurrency(orderPayment.getCurrency())
            .build();
        if (orderPayment.getStatus().equals(Status.ACCEPT) &&
            orderStock.getStatus().equals(Status.ACCEPT)) {
            o.setStatus(Status.CONFIRM);
        } else if (orderPayment.getStatus().equals(Status.REJECT) &&
            orderStock.getStatus().equals(Status.REJECT)) {
            o.setStatus(Status.REJECT);
        } else if (orderPayment.getStatus().equals(Status.REJECT) ||
            orderStock.getStatus().equals(Status.REJECT)) {
            String source = orderPayment.getStatus().equals(Status.REJECT) ? "PAYMENT" : "PRODUCT";
            o.setStatus(Status.ROLLBACK);
            o.setSource(source);
        }
        return o;
    }

}
package com.metao.product.checkout.service;

import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;

import java.util.UUID;

import static com.metao.product.utils.DateFormatter.now;

public interface CheckoutService {
    public OrderEntity checkout(String userId) throws NotEnoughProductsInStockException;

    default OrderEntity createOrder(String userId, String orderDetails, double orderTotal) {
        return OrderEntity.builder()
                .orderTime(now().toString())
                .userId(userId)
                .orderDetails(orderDetails)
                .id(UUID.randomUUID().toString())
                .orderTotal(orderTotal)
                .build();
    }
}
package com.metao.product.checkout.service;

import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.exception.CartIsEmptyException;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;
import com.metao.product.checkout.exception.UserException;

import java.util.UUID;

import static com.metao.product.utils.DateFormatter.now;

public interface CheckoutService {
    public OrderEntity checkout(String userId) throws NotEnoughProductsInStockException, CartIsEmptyException, UserException;

    default OrderEntity createOrder(String userId, String orderDetails, double orderTotal) {
        return OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .orderTime(now())
                .userId(userId)
                .orderDetails(orderDetails)
                .orderTotal(orderTotal)
                .build();
    }
}
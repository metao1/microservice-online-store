package com.metao.product.checkout.service;

import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.exception.CartIsEmptyException;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;
import com.metao.product.checkout.exception.UserException;
import com.metao.product.checkout.utils.DateFormatter;

import java.util.UUID;

public interface CheckoutService {

    OrderEntity checkout(String userId) throws NotEnoughProductsInStockException, CartIsEmptyException, UserException;

    default OrderEntity createOrder(String userId, String orderDetails, double orderTotal) {
        return OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .orderTime(DateFormatter.now())
                .userId(userId)
                .orderDetails(orderDetails)
                .orderTotal(orderTotal)
                .build();
    }
}

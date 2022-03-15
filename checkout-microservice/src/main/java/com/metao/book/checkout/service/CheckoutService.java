package com.metao.book.checkout.service;

// import com.metao.book.checkout.domain.OrderEntity;
// import com.metao.book.checkout.exception.CartIsEmptyException;
// import
// com.metao.book.checkout.exception.NotEnoughProductsInStockException;
// import com.metao.book.checkout.exception.UserException;
// import com.metao.book.checkout.utils.DateFormatter;

// import java.util.UUID;

// public interface CheckoutService {

// OrderEntity checkout(String userId) throws NotEnoughProductsInStockException,
// CartIsEmptyException, UserException;

// default OrderEntity createOrder(String userId, String orderDetails, double
// orderTotal) {
// return OrderEntity.builder()
// .id(UUID.randomUUID().toString())
// .orderTime(DateFormatter.now())
// .userId(userId)
// .orderDetails(orderDetails)
// .orderTotal(orderTotal)
// .build();
// }
// }

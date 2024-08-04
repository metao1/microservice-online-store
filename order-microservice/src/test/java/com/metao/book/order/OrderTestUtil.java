package com.metao.book.order;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderTestUtil {

    public static List<OrderEntity> buildDummyOrders(int count) {
        List<OrderEntity> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            OrderEntity order = buildOrderEntity(i);
            orders.add(order);
        }
        return orders;
    }

    public static OrderEntity buildOrderEntity(int i) {
        String customerId = "customer_" + i; // Generate a dummy customer ID
        String productId = "product_" + i; // Generate a dummy product ID
        BigDecimal productCount = BigDecimal.valueOf(i + 1); // Incremental product count
        Money money = new Money(OrderTestConstant.EUR, BigDecimal.valueOf((i + 1) * 10L)); // Dummy money
        var orderStatus = i % 2 == 0 ? OrderStatus.NEW : OrderStatus.SUBMITTED; // Set status to NEW for all orders
        return new OrderEntity(customerId, productId, productCount, money, orderStatus);
    }
}

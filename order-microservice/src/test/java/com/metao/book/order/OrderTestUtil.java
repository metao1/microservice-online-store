package com.metao.book.order;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderTestUtil {

    public static List<OrderEntity> buildMultipleOrders(int count) {
        List<OrderEntity> orders = new ArrayList<>();
        for (int id = 0; id < count; id++) {
            OrderEntity order = buildOrder(id);
            orders.add(order);
        }
        return orders;
    }

    public static OrderEntity buildOrder(int id) {
        String customerId = "customer_" + id; // Generate a dummy customer ID
        String productId = "product_" + id; // Generate a dummy product ID
        Money money = new Money(OrderTestConstant.EUR, BigDecimal.valueOf((id + 1) * 10d)); // Dummy money
        var orderStatus = id % 2 == 0 ? OrderStatus.NEW : OrderStatus.SUBMITTED; // Set status to NEW for all orders
        return new OrderEntity(customerId, productId, BigDecimal.valueOf(1d), money, orderStatus);
    }
}

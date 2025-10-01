package com.metao.book.order.presentation.dto;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {

    private String id;
    private String customerId;
    private List<OrderItemResponse> items;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Money total;

    public static OrderResponse fromDomain(OrderAggregate order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId().value());
        response.setCustomerId(order.getCustomerId().getValue());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setTotal(order.getTotal());

        response.setItems(order.getItems().stream()
            .map(item -> {
                OrderItemResponse itemResponse = new OrderItemResponse();
                itemResponse.setProductId(item.getProductId().getValue());
                itemResponse.setProductName(item.getProductName());
                itemResponse.setQuantity(item.getQuantity().getValue());
                itemResponse.setUnitPrice(item.getUnitPrice());
                itemResponse.setTotalPrice(item.getTotalPrice());
                return itemResponse;
            })
            .toList());

        return response;
    }

    @Data
    public static class OrderItemResponse {

        private String productId;
        private String productName;
        private int quantity;
        private Money unitPrice;
        private Money totalPrice;
    }
}
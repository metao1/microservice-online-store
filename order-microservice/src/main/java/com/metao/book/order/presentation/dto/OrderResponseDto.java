package com.metao.book.order.presentation.dto;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponseDto {

    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Money subtotal;
    private Money tax;
    private Money total;
    private Integer vatPercentage;

    public static OrderResponseDto fromDomain(OrderAggregate order) {
        OrderResponseDto response = new OrderResponseDto();
        response.setId(order.getId().value());
        response.setUserId(order.getUserId().value());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setTotal(order.getTotal());
        response.setVatPercentage(order.getVat() == null ? null : order.getVat().toInteger());

        response.setItems(order.getItems().stream()
            .map(item -> {
                OrderItemResponse itemResponse = new OrderItemResponse();
                itemResponse.setSku(item.getProductSku().value());
                itemResponse.setProductTitle(item.getTitle().value());
                itemResponse.setQuantity(item.getQuantity().value());
                itemResponse.setUnitPrice(item.getUnitPrice());
                itemResponse.setTotalPrice(item.getTotalPrice());
                return itemResponse;
            })
            .toList());

        return response;
    }

    @Data
    public static class OrderItemResponse {

        private String sku;
        private String productTitle;
        private BigDecimal quantity;
        private Money unitPrice;
        private Money totalPrice;
    }
}

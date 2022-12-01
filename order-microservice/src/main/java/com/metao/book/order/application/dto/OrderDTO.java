package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.domain.Currency;
import com.metao.book.order.domain.Status;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO implements Serializable {

    @JsonProperty("order_id")
    String orderId;

    @JsonProperty("product_id")
    String productId;

    @JsonProperty("customer_id")
    String customerId;

    @JsonProperty("status")
    Status status;

    @JsonProperty("quantity")
    int quantity;

    @JsonProperty("currency")
    Currency currency;

    @JsonProperty("price")
    double price;
}

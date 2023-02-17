package com.metao.book.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Currency;
import java.io.Serializable;
import java.math.BigDecimal;
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
    BigDecimal quantity;

    @JsonProperty("currency")
    Currency currency;

    @JsonProperty("price")
    BigDecimal price;

}

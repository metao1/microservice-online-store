package com.metao.book.order.application.dto;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import com.metao.book.order.domain.CustomerId;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO implements Serializable {

        OrderId orderId;

        @Pattern(regexp = "^[0-9]{10}", message = "productId format is wrong")
        ProductId productId;

        CustomerId customerId;

        Status status;

        int productCount;

        Currency currency;

        double amount;       
}

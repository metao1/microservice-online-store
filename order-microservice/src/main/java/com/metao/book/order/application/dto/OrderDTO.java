package com.metao.book.order.application.dto;

import com.metao.book.order.domain.CustomerId;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.base.ValueObject;
import com.metao.book.shared.domain.financial.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO implements ValueObject {

        OrderId orderId;

        ProductId productId;

        CustomerId customerId;

        Status status;

        int productCount;

        Money money;
}

package com.metao.book.order.presentation.controller;

import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.order.presentation.dto.OrderResponseDto;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Timed(value = "order.api", extraTags = {"controller", "order"})
public class OrderManagementController {

    private final OrderManagementService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderId createOrder(@RequestBody CreateOrderRequestDTO request) { // TODO update this DTO to have all order info
        return orderService.createOrder(new CustomerId(request.userId()));
    }

    @PutMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addItemToOrder(
        @PathVariable String orderId,
        @Valid @NotNull @RequestBody AddItemRequestDto request
    ) {
        request.items().forEach(item -> {
            orderService.addItemToOrder(
                OrderId.of(orderId),
                new ProductSku(item.sku()),
                new Quantity(item.quantity()),
                new Money(item.currency(), item.price()));
        });
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateStatus(
        @PathVariable String orderId,
        @RequestBody UpdateStatusRequestDto request
    ) {
        orderService.updateOrderStatus(OrderId.of(orderId), request.status());
    }

    @GetMapping("/customer/{customerId}")
    public List<OrderResponseDto> getCustomerOrders(@PathVariable String customerId) {
        return orderService.getCustomerOrders(new CustomerId(customerId)).stream()
            .map(OrderResponseDto::fromDomain)
            .toList();
    }
}

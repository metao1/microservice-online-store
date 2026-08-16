package com.metao.book.order.presentation.controller;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.application.usecase.CreateOrderUseCase;
import com.metao.book.order.application.usecase.GetCustomerOrdersUseCase;
import com.metao.book.order.application.usecase.UpdateOrderStatusUseCase;
import com.metao.book.order.presentation.dto.OrderPageResponseDto;
import com.metao.book.order.presentation.dto.OrderResponseDto;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.book.shared.architecture.InboundAdapter;
import com.metao.book.shared.security.CurrentUser;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@InboundAdapter(InboundAdapter.Kind.HTTP)
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Timed(value = "order.api", extraTags = {"controller", "order"})
@Observed(name = "order.api.controller", contextualName = "order-management-controller")
public class OrderManagementController {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final GetCustomerOrdersUseCase getCustomerOrdersUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_orders:write')")
    public OrderId createOrder() {
        return createOrderUseCase.createOrder(UserId.of(CurrentUser.subject()));
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasAuthority('SCOPE_orders:write')")
    public void updateStatus(
        @PathVariable String orderId,
        @RequestBody UpdateStatusRequestDto request
    ) {
        updateOrderStatusUseCase.updateOrderStatus(OrderId.of(orderId), request.statusEnum());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_orders:read')")
    public List<OrderResponseDto> getCustomerOrders() {
        return getCustomerOrdersUseCase.getCustomerOrders(UserId.of(CurrentUser.subject())).stream()
            .map(OrderResponseDto::fromDomain)
            .toList();
    }

    @GetMapping("/me/paged")
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_orders:read')")
    public OrderPageResponseDto getCustomerOrdersPaged(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit
    ) {
        var ordersPage = getCustomerOrdersUseCase.getCustomerOrders(UserId.of(CurrentUser.subject()), offset, limit)
            .map(OrderResponseDto::fromDomain);
        return OrderPageResponseDto.from(ordersPage, offset, limit);
    }
}

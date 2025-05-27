package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.config.OrderEventHandler;
import com.metao.book.order.domain.OrderService;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.dto.OrderDTO;
import com.metao.book.order.domain.dto.ShoppingCartDto;
import com.metao.book.order.domain.dto.ShoppingCartItem;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.mapper.OrderDTOMapper;
import com.metao.book.order.infrastructure.kafka.KafkaOrderMapper;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Import(OrderEventHandler.class)
@RequestMapping(path = "/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderEventHandler orderEventHandler;
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    public OrderDTO getOrderByOrderId(@RequestParam("order_id") String orderId) {
        return orderService.getOrderByOrderId(orderId)
            .map(OrderDTOMapper::toOrderDTO)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @GetMapping("/{pageSize}/{offset}/{sortByFieldName}")
    public Page<OrderDTO> getOrderByProductIdsAndStatusesPageable(
        @RequestParam(value = "productIds", required = false) Set<String> productIds,
        @RequestParam(value = "statuses", required = false) Set<OrderStatus> statuses,
        @PathVariable int offset,
        @PathVariable int pageSize,
        @PathVariable String sortByFieldName
    ) {
        return orderService.getOrderByProductIdsAndOrderStatus(productIds, statuses, offset, pageSize, sortByFieldName)
            .map(OrderDTOMapper::toOrderDTO);
    }

    @PostMapping
    public ResponseEntity<List<String>> createOrder(@RequestBody @Valid CreateOrderRequestDTO createOrderRequest) {
        String userId = createOrderRequest.getUserId();
        ShoppingCartDto cart = shoppingCartService.getCartForUser(userId);

        if (cart == null || cart.shoppingCartItems() == null || cart.shoppingCartItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("Cart is empty or user not found."));
        }

        List<String> eventIds = new ArrayList<>();
        for (ShoppingCartItem item : cart.shoppingCartItems()) {
            OrderDTO orderDtoFromCartItem = OrderDTO.builder()
                .orderId(null) // Will be generated downstream
                .productId(item.asin())
                .customerId(userId)
                .createdTime(OffsetDateTime.now())
                .quantity(item.quantity())
                .currency(item.currency().getCurrencyCode())
                .status(com.metao.book.order.domain.OrderStatus.NEW.name()) // Default status
                .price(item.price())
                .build();

            var event = KafkaOrderMapper.toOrderCreatedEvent(orderDtoFromCartItem);
            String eventId = orderEventHandler.handle(event.getId(), event); // Assuming handle returns the key/id
            eventIds.add(eventId != null ? eventId : event.getId());
        }

        if (!eventIds.isEmpty()) {
            shoppingCartService.clearCart(userId);
        }

        return ResponseEntity.ok(eventIds);
    }

    @PutMapping
    public String updateOrder(@RequestBody @Valid OrderDTO orderDto) {
        var updatedOrder = KafkaOrderMapper.toOrderUpdatedEvent(orderDto);
        return orderEventHandler.handle(updatedOrder.getId(), updatedOrder);
    }
}

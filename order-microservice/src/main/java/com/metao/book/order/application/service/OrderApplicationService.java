package com.metao.book.order.application.service;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartService;

import com.metao.book.order.domain.event.DomainEventPublisher;
import com.metao.book.order.domain.model.aggregate.Order;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.order.domain.repository.OrderRepository;

import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    public static final String ORDER_NOT_FOUND = "Order not found";
    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final ShoppingCartService shoppingCartService;

    @Transactional
    public OrderId createOrder(CustomerId customerId) {
        // Get items from shopping cart
        List<ShoppingCart> cartItems = shoppingCartService.getCartForUser(customerId.getValue()).shoppingCartItems()
            .stream()
            .map(item -> {
                // Convert ShoppingCartItem back to ShoppingCart for processing
                // This is a bit awkward, but we need the full cart data
                return new ShoppingCart(customerId.getValue(), item.asin(),
                    item.price(), item.price(), item.quantity(), item.currency());
            })
            .toList();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot create order: shopping cart is empty");
        }

        // Create order
        Order order = new Order(OrderId.generate(), customerId);

        // Add items from cart to order
        for (ShoppingCart cartItem : cartItems) {
            order.addItem(
                new ProductId(cartItem.getAsin()),
                cartItem.getAsin(), // Using ASIN as product name for now
                new Quantity(cartItem.getQuantity().intValue()),
                new Money(cartItem.getCurrency(), cartItem.getSellPrice())
            );
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear shopping cart
        shoppingCartService.clearCart(customerId.getValue());

        // Publish events
        publishEvents(savedOrder);
        return savedOrder.getId();
    }

    @Transactional
    public void addItemToOrder(
        OrderId orderId, ProductId productId, String productName, Quantity quantity,
        Money unitPrice
    ) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));

        order.addItem(productId, productName, quantity, unitPrice);
        Order savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional
    public void updateItemQuantity(OrderId orderId, ProductId productId, Quantity newQuantity) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));

        order.updateItemQuantity(productId, newQuantity);
        Order savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional
    public void removeItem(OrderId orderId, ProductId productId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));

        order.removeItem(productId);
        Order savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(OrderId orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.valueOf(status));
        Order savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<Order> getCustomerOrders(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    private void publishEvents(Order order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}
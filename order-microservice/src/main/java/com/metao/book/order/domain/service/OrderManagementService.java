package com.metao.book.order.domain.service;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.service.DomainEventToKafkaEventHandler;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.exception.ShoppingCartIsEmptyException;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final DomainEventToKafkaEventHandler eventPublisher;
    private final ShoppingCartService shoppingCartService;

    @Transactional
    public OrderId createOrder(CustomerId customerId) {
        // Get items from shopping cart
        var cart = shoppingCartService.getCartForUser(customerId.getValue());
        if (cart.shoppingCartItems().isEmpty()) {
            throw new ShoppingCartIsEmptyException();
        }

        var cartItems = cart.shoppingCartItems()
            .stream()
            .map(item -> {
                // Convert ShoppingCartItem back to ShoppingCart for processing
                // This is a bit awkward, but we need the full cart data
                return new ShoppingCart(customerId.getValue(), item.sku(),
                    item.price(), item.price(), item.quantity(), item.currency());
            })
            .toList();

        if (cartItems.isEmpty()) {
            throw new ShoppingCartIsEmptyException();
        }

        // Create order
        var order = new OrderAggregate(OrderId.generate(), customerId);

        // Add items from cart to order
        for (ShoppingCart cartItem : cartItems) {
            // Adds priced and quantified items to order
            order.addItem(
                new ProductSku(cartItem.getSku()),
                new Quantity(cartItem.getQuantity()),
                new Money(cartItem.getCurrency(), cartItem.getSellPrice())
            );
        }

        // Save order
        orderRepository.save(order);

        // Clear shopping cart
        shoppingCartService.clearCart(customerId.getValue());
        // Publish events
        // This will send OrderCreatedEvent
        publishEvents(order);
        return order.getId();
    }

    @Transactional
    public void addItemToOrder(
        OrderId orderId,
        ProductSku productId,
        Quantity quantity,
        Money unitPrice
    ) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.addItem(productId, quantity, unitPrice);
        orderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void updateItemQuantity(OrderId orderId) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.updateItemQuantity();
        OrderAggregate savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional
    public void removeItem(OrderId orderId, ProductSku productId) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.removeItem(productId);
        OrderAggregate savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(OrderId orderId, String status) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.updateStatus(OrderStatus.valueOf(status));
        OrderAggregate savedOrder = orderRepository.save(order);
        publishEvents(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderAggregate> getCustomerOrders(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public OrderAggregate getOrderById(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public OrderAggregate getOrderByIdForUpdate(OrderId orderId) {
        return orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void publishEvents(OrderAggregate order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}

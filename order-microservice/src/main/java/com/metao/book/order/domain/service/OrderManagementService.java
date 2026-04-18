package com.metao.book.order.domain.service;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.service.DomainEventToKafkaEventHandler;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.exception.ShoppingCartIsEmptyException;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final DomainEventToKafkaEventHandler eventPublisher;
    private final ShoppingCartService shoppingCartService;
    private final VAT vat;

    @Transactional
    public OrderId createOrder(UserId userId) {
        var cart = shoppingCartService.getCartForUser(userId.value());
        if (cart.shoppingCartItems().isEmpty()) {
            throw new ShoppingCartIsEmptyException();
        }

        var order = new OrderAggregate(OrderId.generate(), userId, vat);
        cart.shoppingCartItems().forEach(item -> {
            var cartItem = new ShoppingCart(
                userId.value(),
                item.sku(),
                item.productTitle(),
                item.price(),
                item.price(),
                item.quantity(),
                item.currency()
            );

            order.addItem(
                ProductSku.of(cartItem.getSku()),
                ProductTitle.of(cartItem.getProductTitle()),
                Quantity.of(cartItem.getQuantity()),
                Money.of(cartItem.getCurrency(), cartItem.getSellPrice())
            );
        });

        order.raiseOrderCreatedEvents();
        orderRepository.save(order);
        publishEvents(order);
        return order.getId();
    }

    @Transactional
    public void updateItemQuantity(OrderId orderId) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.updateItemQuantity();
        orderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void removeItem(OrderId orderId, ProductSku sku) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.removeItem(sku);
        orderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void updateOrderStatus(OrderId orderId, String status) {
        OrderAggregate order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.updateStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        publishEvents(order);
    }

    @Transactional(readOnly = true)
    public List<OrderAggregate> getCustomerOrders(UserId userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<OrderAggregate> getCustomerOrders(UserId userId, int offset, int limit) {
        return orderRepository.findByUserId(userId, offset, limit);
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

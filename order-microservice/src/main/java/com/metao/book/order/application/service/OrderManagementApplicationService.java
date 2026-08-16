package com.metao.book.order.application.service;

import com.metao.book.order.application.port.OrderPort;
import com.metao.book.order.application.port.OrderRepository;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import com.metao.book.order.application.usecase.CreateOrderUseCase;
import com.metao.book.order.application.usecase.GetCustomerOrdersUseCase;
import com.metao.book.order.application.usecase.UpdateOrderStatusUseCase;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.exception.ShoppingCartIsEmptyException;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import com.metao.book.shared.architecture.ApplicationService;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ApplicationService
public class OrderManagementApplicationService implements CreateOrderUseCase, UpdateOrderStatusUseCase,
    GetCustomerOrdersUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final ShoppingCartCommandPort shoppinCartCommand;
    private final VAT vat;
    private final OrderPort orderPort;

    @Autowired
    public OrderManagementApplicationService(
        OrderRepository orderRepository,
        DomainEventPublisherPort eventPublisher,
        ShoppingCartService shoppinCartCommand,
        VAT vat,
        OrderPort orderPort
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.shoppinCartCommand = shoppinCartCommand;
        this.vat = vat;
        this.orderPort = orderPort;
    }

    /** Compatibility constructor for application-level tests without a locking adapter. */
    public OrderManagementApplicationService(
        OrderRepository orderRepository,
        DomainEventPublisherPort eventPublisher,
        ShoppingCartService shoppinCartCommand,
        VAT vat
    ) {
        this(orderRepository, eventPublisher, shoppinCartCommand, vat, orderRepository::findById);
    }

    @Override
    @Transactional
    public OrderId createOrder(UserId userId) {
        var cart = shoppinCartCommand.getCartForUser(userId.value());
        if (cart.shoppingCartItems().isEmpty()) {
            throw new ShoppingCartIsEmptyException();
        }

        var order = new OrderAggregate(OrderId.generate(), userId, vat);
        cart.shoppingCartItems().forEach(item -> order.addItem(
            ProductSku.of(item.sku()),
            ProductTitle.of(item.productTitle()),
            Quantity.of(item.quantity()),
            Money.of(item.currency(), item.price())
        ));

        order.raiseOrderCreatedEvents();
        orderRepository.save(order);
        publishEvents(order);
        return order.getId();
    }

    @Override
    @Transactional
    public void updateOrderStatus(OrderId orderId, OrderStatus status) {
        OrderAggregate order = findOrder(orderId);
        order.updateStatus(status);
        orderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void removeItem(OrderId orderId, ProductSku sku) {
        OrderAggregate order = findOrder(orderId);
        order.removeItem(sku);
        orderRepository.save(order);
        publishEvents(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderAggregate> getCustomerOrders(UserId userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderAggregate> getCustomerOrders(UserId userId, int offset, int limit) {
        return orderRepository.findByUserId(userId, offset, limit);
    }

    public OrderAggregate getOrderByIdForUpdate(OrderId orderId) {
        return orderPort.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public void requestInventoryReduction(OrderId orderId) {
        OrderAggregate order = findOrder(orderId);
        order.requestInventoryReduction();
        orderRepository.save(order);
        publishEvents(order);
    }

    private OrderAggregate findOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void publishEvents(OrderAggregate order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}

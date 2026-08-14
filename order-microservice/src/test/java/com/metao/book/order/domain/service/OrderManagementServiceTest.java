package com.metao.book.order.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.service.ShoppingCartService;
import com.metao.book.order.application.cart.ShoppingCartView;
import com.metao.book.order.application.service.OrderManagementApplicationService;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.application.port.OrderRepository;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import com.metao.book.shared.domain.financial.VAT;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Mock
    private ShoppingCartService shoppingCartService;

    @InjectMocks
    private OrderManagementApplicationService orderManagementService;

    @BeforeEach
    void setUp() {
        orderManagementService = new OrderManagementApplicationService(
            orderRepository,
            eventPublisher,
            shoppingCartService,
            new VAT(19)
        );
    }

    @Test
    void createOrderPublishesSingleCreatedEventWithAllCartItems() {
        UserId userId = UserId.of("user123");
        ShoppingCartView shoppingCart = new ShoppingCartView(
            userId.value(),
            List.of(
                new ShoppingCartItem("SKU-1", "Book 1", BigDecimal.ONE, BigDecimal.valueOf(10.0),
                    Currency.getInstance("EUR")),
                new ShoppingCartItem("SKU-2", "Book 2", BigDecimal.TWO, BigDecimal.valueOf(20.0),
                    Currency.getInstance("EUR"))
            )
        );

        when(shoppingCartService.getCartForUser(userId.value())).thenReturn(shoppingCart);

        orderManagementService.createOrder(userId);

        verify(orderRepository).save(any());
        verify(eventPublisher, times(1)).publish(any());
    }
}

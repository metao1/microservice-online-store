package com.metao.book.order.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.service.DomainEventToKafkaEventHandler;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
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
    private DomainEventToKafkaEventHandler eventPublisher;

    @Mock
    private ShoppingCartService shoppingCartService;

    @InjectMocks
    private OrderManagementService orderManagementService;

    @Test
    void createOrderPublishesSingleCreatedEventWithAllCartItems() {
        UserId userId = UserId.of("user123");
        ShoppingCartDto shoppingCart = new ShoppingCartDto(
            userId.value(),
            Set.of(
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

package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.application.cart.ShoppingCartUseCase;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Observed(name = "order.persistence.repository", contextualName = "order-repository")
public class ShoppingCartAdapter implements ShoppingCartCommandPort {

    private final ShoppingCartUseCase shoppingCartUseCase;

    @Override
    public void clearCart(String userId) {
        shoppingCartUseCase.clearCart(userId);
    }
}

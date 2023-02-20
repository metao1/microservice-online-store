package com.metao.book.cart.service.mapper;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartMapperService extends CartMapper<ShoppingCart, OrderEvent> {

    public OrderEvent mapToOrderEvent(ShoppingCart shoppingCart) {
        return mapTo(() -> shoppingCart, s -> shoppingCart != null);
    }

    @Override
    protected OrderEvent createItem(ShoppingCart item) {
        return OrderEvent.newBuilder()
            .setQuantity(item.getQuantity())
            .setOrderId(UUID.randomUUID().toString())
            .setCreatedOn(Instant.now().toEpochMilli())
            .setCustomerId(item.getUserId())
            .setStatus(Status.NEW)
            .setProductId(item.getAsin())
            .build();
    }

    public ShoppingCartDto mapToShoppingCartDto(ShoppingCart shoppingCart) {
        return ShoppingCartDto.builder()
            .asin(shoppingCart.getAsin())
            .quantity(shoppingCart.getQuantity())
            .build();
    }
}

package com.metao.book.cart.service.mapper;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.domain.dto.ShoppingCartItem;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

public class CartMapperService {

    @Component
    public static final class ToEventMapper extends CartMapper<ShoppingCart, OrderEvent> {

        public OrderEvent mapToOrderEvent(ShoppingCart shoppingCart) {
            return doMap(() -> shoppingCart, s -> shoppingCart != null);
        }

        protected OrderEvent apply(ShoppingCart item) {
            return OrderEvent.newBuilder()
                .setQuantity(item.getQuantity().doubleValue())
                .setOrderId(UUID.randomUUID().toString())
                .setCreatedOn(Instant.now().toEpochMilli())
                .setCustomerId(item.getUserId())
                .setStatus(Status.NEW)
                .setProductId(item.getAsin())
                .build();
        }
    }

    @Component
    public static final class ToCartDto extends CartMapper<Set<ShoppingCart>, ShoppingCartDto> {

        public ShoppingCartDto mapToDto(Set<ShoppingCart> item) {
            return doMap(() -> item, s -> item != null && !item.isEmpty());
        }

        @Override
        protected ShoppingCartDto apply(Set<ShoppingCart> item) {
            final var shoppingCartDto = new ShoppingCartDto(Instant.now().toEpochMilli(), null, null);
            item.forEach(shoppingCart -> {
                shoppingCartDto.addItem(new ShoppingCartItem(shoppingCart.getAsin(), shoppingCart.getQuantity(),
                    shoppingCart.getBuyPrice()));
            });
            return shoppingCartDto;
        }
    }

    @Component
    public static final class ToEntityMapper extends CartMapper<ShoppingCartDto, List<ShoppingCart>> {

        public List<ShoppingCart> mapToEntity(ShoppingCartDto orderDto) {
            return doMap(() -> orderDto, s -> orderDto != null);
        }

        @Override
        protected List<ShoppingCart> apply(ShoppingCartDto item) {
            return item.shoppingCartItems()
                .stream()
                .map(shoppingCartItem -> ShoppingCart.createCart(
                        item.userId(),
                        shoppingCartItem.asin(),
                        shoppingCartItem.price(),
                        shoppingCartItem.price(),
                        shoppingCartItem.quantity()
                    )
                )
                .toList();
        }
    }

}

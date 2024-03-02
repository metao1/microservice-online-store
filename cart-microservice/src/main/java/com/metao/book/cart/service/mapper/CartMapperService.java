package com.metao.book.cart.service.mapper;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.domain.dto.ShoppingCartItem;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

public class CartMapperService {

    @Component
    public static final class toDtoMapper extends CartMapper<ShoppingCart, OrderEvent> {
        public OrderEvent mapToOrderEvent(ShoppingCart shoppingCart) {
            return doMap(() -> shoppingCart, s -> shoppingCart != null);
        }

        protected OrderEvent apply(ShoppingCart item) {
            return OrderEvent.newBuilder()
                .setQuantity(item.getQuantity().doubleValue())
                .setPrice(item.getBuyPrice().doubleValue())
                .setCreatedOn(Instant.now().toEpochMilli())
                .setOrderId(UUID.randomUUID().toString())
                .setCustomerId(item.getUserId())
                .setSource("CART")
                .setCurrency(mapCurrency(item.getCurrency()))
                .setProductId(item.getAsin())
                .setStatus(Status.NEW)
                .build();
        }

        private Currency mapCurrency(com.metao.book.shared.domain.financial.Currency currency) {
            return Currency.valueOf(currency.name().toLowerCase());
        }
    }

    @Component
    public static final class ToShoppingCartEntity extends CartMapper<OrderEvent, ShoppingCart> {

        public ShoppingCart mapToShoppingCart(OrderEvent item) {
            return doMap(() -> item, s -> item != null);
        }

        @Override
        protected ShoppingCart apply(OrderEvent item) {
            return ShoppingCart.createCart(item.getCustomerId(),
                item.getProductId(),
                BigDecimal.valueOf(item.getPrice()),
                null,
                BigDecimal.valueOf(item.getQuantity()),
                mapCurrency(item.getCurrency()));
        }

        private com.metao.book.shared.domain.financial.Currency mapCurrency(Currency currency) {
            return com.metao.book.shared.domain.financial.Currency.valueOf(currency.name().toUpperCase());
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
                    shoppingCart.getBuyPrice(), shoppingCart.getCurrency()));
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
                    shoppingCartItem.quantity(),
                    shoppingCartItem.currency())
                )
                .toList();
        }
    }

}

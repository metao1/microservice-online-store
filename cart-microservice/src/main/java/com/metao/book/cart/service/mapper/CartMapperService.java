package com.metao.book.cart.service.mapper;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartMapperService {

    public static final class ToEventMapper extends CartMapper<ShoppingCart, OrderEvent> {

        public OrderEvent mapToOrderEvent(ShoppingCart shoppingCart) {
            return mapToEvent(() -> shoppingCart, s -> shoppingCart != null);
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

    public final class ToEntityMapper extends CartMapper<OrderEvent, ShoppingCart> {

        public ShoppingCart mapToEntity(OrderEvent orderEvent) {
            return mapToEvent(() -> orderEvent, s -> orderEvent != null);
        }

        protected ShoppingCart apply(OrderEvent event) {
            return ShoppingCart.builder()
                .imageUrl(event.getProductId())
                .buyPrice(BigDecimal.valueOf(event.getPrice()))
                .sellPrice(BigDecimal.valueOf(event.getPrice()))
                .quantity(BigDecimal.valueOf(event.getQuantity()))
                .userId(event.getCustomerId())
                .asin(event.getProductId())
                .build();
        }
    }

}

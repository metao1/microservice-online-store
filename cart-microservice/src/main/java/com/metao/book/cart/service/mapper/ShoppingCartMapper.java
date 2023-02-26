package com.metao.book.cart.service.mapper;

import com.metao.book.cart.domain.dto.ShoppingCartItem;
import com.metao.book.shared.OrderEvent;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ShoppingCartMapper extends CartMapper<OrderEvent, ShoppingCartItem> {

    public ShoppingCartItem mapToShoppingCartDto(OrderEvent event) {
        return new ShoppingCartItem(
            event.getCustomerId(),
            event.getProductId(),
            BigDecimal.valueOf(event.getQuantity()),
            BigDecimal.valueOf(event.getPrice())
        );
    }
}

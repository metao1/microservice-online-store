package com.metao.book.order.infrastructure.persistence.cart;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ShoppingCartJpaKey implements Serializable {
    private String userId;
    private String sku;
}

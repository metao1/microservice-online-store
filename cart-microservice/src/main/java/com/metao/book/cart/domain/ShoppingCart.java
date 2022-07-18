package com.metao.book.cart.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ShoppingCartKey.class)
@Table(name = "shopping_cart")
public class ShoppingCart {

    @Transient
    private static final int DEFAULT_QUANTITY = 1;

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "asin")
    private String asin;

    @Column(name = "time_added")
    private Instant timeAdded;

    @Column(name = "quantity")
    private int quantity;

    public static ShoppingCart createCart(ShoppingCartKey currentKey) {
        return ShoppingCart
                .builder()
                .userId(currentKey.getUserId())
                .asin(currentKey.getAsin())
                .quantity(DEFAULT_QUANTITY)
                .timeAdded(Instant.now())
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ShoppingCart that = (ShoppingCart) obj;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(asin, that.asin)
                && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, asin, timeAdded, quantity);
    }


}
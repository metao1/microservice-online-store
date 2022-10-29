package com.metao.book.cart.domain;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "shopping_cart")
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

    public int increaseQuantity() {
        if (quantity == Integer.MAX_VALUE) {
            throw new IllegalStateException("Quantity is already at max value");
        }
        return ++quantity;
    }

    public int decreaseQuantity() {
        if(this.quantity > 1) {
            this.quantity--;
        }
        return this.quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ShoppingCart that = (ShoppingCart) o;
        return userId != null && Objects.equals(userId, that.userId)
                && asin != null && Objects.equals(asin, that.asin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, asin);
    }
}
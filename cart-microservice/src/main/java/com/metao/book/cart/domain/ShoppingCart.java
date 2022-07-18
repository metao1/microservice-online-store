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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ShoppingCart that = (ShoppingCart) obj;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(asin, that.asin)
                && Objects.equals(timeAdded, that.timeAdded)
                && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, asin, timeAdded, quantity);
    }
}
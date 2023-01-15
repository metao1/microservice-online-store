package com.metao.book.cart.domain;

import com.metao.book.cart.service.mapper.BaseDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "shopping_cart")
@IdClass(ShoppingCartKey.class)
@Table(name = "shopping_cart")
public class ShoppingCart extends BaseDTO {

    @Transient
    private static final int DEFAULT_QUANTITY = 1;

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "asin")
    private String asin;

    @Column(name = "time_created")
    private Long createdOn;

    @Column(name = "time_updated")
    private Long updateOn;

    @Column(name = "quantity")
    private int quantity;

    public static ShoppingCart createCart(ShoppingCartKey currentKey) {
        return ShoppingCart.builder()
            .createdOn(Instant.now().toEpochMilli())
            .userId(currentKey.getUserId())
            .asin(currentKey.getAsin())
            .quantity(DEFAULT_QUANTITY)
            .build();
    }

    public int increaseQuantity() {
        if (quantity == Integer.MAX_VALUE) {
            throw new IllegalStateException("Quantity is already at max value");
        }
        setUpdateOn(Instant.now().toEpochMilli());
        return ++quantity;
    }

    public int decreaseQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
        setUpdateOn(Instant.now().toEpochMilli());
        return this.quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ShoppingCart that = (ShoppingCart) o;
        return userId != null && Objects.equals(userId, that.userId)
            && asin != null && Objects.equals(asin, that.asin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, asin);
    }
}
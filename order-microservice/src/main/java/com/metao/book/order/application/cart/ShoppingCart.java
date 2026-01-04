package com.metao.book.order.application.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@ToString
@Getter
@Setter
@Entity(name = "shopping_cart")
@IdClass(ShoppingCartKey.class)
@Table(name = "shopping_cart")
@NoArgsConstructor
public class ShoppingCart {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "sku")
    private String sku;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "updated_time")
    private Long updatedOn;

    @Column(name = "created_time")
    private Long createdOn;

    @Column(name = "buy_price")
    private BigDecimal buyPrice;

    @Column(name = "sell_price")
    private BigDecimal sellPrice;

    @Column(name = "currency")
    private Currency currency;

    public ShoppingCart(
        String userId,
        String sku,
        BigDecimal buyPrice,
        BigDecimal sellPrice,
        BigDecimal quantity,
        Currency currency
    ) {
        this.createdOn = Instant.now().toEpochMilli();
        this.sku = sku;
        this.userId = userId;
        this.quantity = quantity;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.currency = currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, sku);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShoppingCart that) || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        return userId != null && Objects.equals(userId, that.userId)
            && sku != null && Objects.equals(sku, that.sku);
    }

}
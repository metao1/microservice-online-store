package com.metao.book.order.infrastructure.persistence.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopping_cart")
@IdClass(ShoppingCartJpaKey.class)
@Getter
@Setter
    @NoArgsConstructor
public class ShoppingCartJpaEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "sku")
    private String sku;

    @Column(name = "product_title")
    private String productTitle;

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

    public ShoppingCartJpaEntity(
        String userId,
        String sku,
        String productTitle,
        BigDecimal buyPrice,
        BigDecimal sellPrice,
        BigDecimal quantity,
        Currency currency
    ) {
        this.userId = userId;
        this.sku = sku;
        this.productTitle = productTitle;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        this.currency = currency;
        this.createdOn = Instant.now().toEpochMilli();
    }

    public ShoppingCartJpaEntity(String userId, ShoppingCartJpaEntity source) {
        this.userId = userId;
        this.sku = source.sku;
        this.productTitle = source.productTitle;
        this.quantity = source.quantity;
        this.updatedOn = source.updatedOn;
        this.createdOn = source.createdOn == null ? Instant.now().toEpochMilli() : source.createdOn;
        this.buyPrice = source.buyPrice;
        this.sellPrice = source.sellPrice;
        this.currency = source.currency;
    }
}

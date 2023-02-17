package com.metao.book.order.domain;

import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "order_table")
public class OrderEntity extends AbstractEntity<OrderId> {

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_count", nullable = false)
    private BigDecimal productCount;

    @Column(name = "currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public OrderEntity(
        String orderId,
        String customerId,
        String productId,
        BigDecimal productCount,
        Money money,
        Status status
    ) {
        super(new OrderId(orderId));
        this.status = status;
        this.productId = productId;
        this.customerId = customerId;
        this.productCount = productCount;
        this.currency = money.currency();
        this.price = money.doubleAmount();
    }

}

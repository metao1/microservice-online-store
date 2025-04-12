package com.metao.book.order.domain;

import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.DomainObjectId;
import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Indexed;

@Getter
@Setter
@Entity
@Indexed
@ToString
@NoArgsConstructor
@Table(name = "order_table")
public class OrderEntity extends AbstractEntity<OrderId> {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "currency", nullable = false, precision = 5, scale = 2)
    private Currency currency;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public OrderEntity(
            String orderId,
            String productId,
            String customerId,
            BigDecimal quantity,
            Currency currency,
            BigDecimal price,
            OrderStatus status) {
        super(DomainObjectId.randomId(OrderId.class));
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.currency = currency;
        this.price = price;
        this.status = status;
    }

    public OrderEntity(
            String customerId,
            String productId,
            BigDecimal quantity,
            Money money,
            OrderStatus status) {
        this(buildOrderId(customerId, productId), productId, customerId, quantity, money.currency(),
                money.doubleAmount(), status);
        this.createdTime = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = (o instanceof HibernateProxy s)
                ? (s.getHibernateLazyInitializer().getPersistentClass())
                : o.getClass();
        Class<?> thisEffectiveClass = (o instanceof HibernateProxy proxy)
                ? (proxy).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(this.productId, that.productId) &&
                Objects.equals(this.customerId, that.customerId) &&
                Objects.equals(this.orderId, that.orderId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.productId, this.customerId, this.orderId);
    }

    private static String buildOrderId(String customerId, String productId) {
        return customerId +
                "::" +
                productId +
                "::" +
                System.currentTimeMillis();
    }
}

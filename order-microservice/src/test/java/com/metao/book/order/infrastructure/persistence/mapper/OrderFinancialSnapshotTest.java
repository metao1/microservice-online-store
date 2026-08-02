package com.metao.book.order.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class OrderFinancialSnapshotTest {

    @Test
    void rehydratesHistoricalTotalsFromPersistedSnapshot() {
        OrderAggregate original = new OrderAggregate(OrderId.generate(), UserId.of("customer-1"), new VAT(19));
        original.addItem(
            ProductSku.of("SKU-1"),
            ProductTitle.of("Book"),
            Quantity.of(BigDecimal.ONE),
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(100))
        );

        OrderJpaEntity persisted = OrderEntityMapper.toEntity(original);
        OrderAggregate rehydratedWithChangedConfiguration = OrderEntityMapper.toDomain(persisted, new VAT(100));

        assertThat(rehydratedWithChangedConfiguration.getVat()).isEqualTo(new VAT(19));
        assertThat(rehydratedWithChangedConfiguration.getSubtotal().fixedPointAmount()).isEqualByComparingTo("100");
        assertThat(rehydratedWithChangedConfiguration.getTax().fixedPointAmount()).isEqualByComparingTo("19");
        assertThat(rehydratedWithChangedConfiguration.getTotal().fixedPointAmount()).isEqualByComparingTo("119");
    }
}

package com.metao.book.shared.spring.persistence;

import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class MoneyEmbeddable {

    private Currency currency;
    private BigDecimal amount;

    private MoneyEmbeddable(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static MoneyEmbeddable from(Money money) {
        return money == null ? null : new MoneyEmbeddable(money.currency(), money.fixedPointAmount());
    }

    public Money toDomain() {
        return Money.of(currency, amount);
    }
}

package com.metao.book.shared.domain.financial;

import com.metao.book.shared.domain.base.ValueObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing an amount of money. The amount is stored as a fixed-point integer where the last two digits
 * represent the decimals.
 */
public class Money implements ValueObject {

    private Currency currency;
    private BigDecimal amount;

    public static final Money ZERO = new Money();
    // Default constructor for Hibernate
    public Money() {
        // ignore
    }

    /**
     * Creates a new {@code Money} object.
     *
     * @param currency the currency.
     * @param amount   fixed-point integer where the last two digits represent decimals.
     */
    public Money(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * Returns a new {@code Money} object whose amount is the sum of this amount and {@code augend}'s amount.
     *
     * @param augend the {@code Money} object to add to this object.
     * @return {@code this} + {@code augend}
     * @throws IllegalArgumentException if this object and {@code augend} have different currencies.
     */
    public Money add(Money augend) {
        if (currency != augend.currency) {
            throw new IllegalArgumentException("Cannot add two Money objects with different currencies");
        }
        return Money.of(currency, amount.add(augend.amount));
    }

    /**
     * Returns a new {@code Money} object whose amount is the difference between this amount and {@code subtrahend}'s
     * amount.
     *
     * @param subtrahend the {@code Money} object to remove from this object.
     * @return {@code this} - {@code augend}
     * @throws IllegalArgumentException if this object and {@code subtrahend} have different currencies.
     */
    public Money subtract(Money subtrahend) {
        if (currency != subtrahend.currency) {
            throw new IllegalArgumentException("Cannot subtract two Money objects with different currencies");
        }
        return Money.of(currency, amount.subtract(subtrahend.amount));
    }

    /**
     * Returns a new {@code Money} object whose amount is this amount multiplied by {@code multiplicand}.
     *
     * @param multiplicand the value to multiply the amount by.
     * @return {@code this} * {@code multiplicand}
     */
    public Money multiply(BigDecimal multiplicand) {
        return Money.of(currency, amount.multiply(multiplicand));
    }

    /**
     * Returns a new {@code Money} object whose amount is this amount divided by {@code divisor}.
     *
     * @param divisor the value to divide the amount by.
     * @return {@code this} / {@code divisor}
     */
    public Money divide(BigDecimal divisor) {
        return Money.of(currency, amount.divide(divisor, RoundingMode.HALF_UP));
    }

    /**
     * Creates a Money.of( object with the specified amount and currency.
     */
    public static Money of(Currency currency, BigDecimal amount) {
        return new Money(currency, amount);
    }

    /**
     * Checks if this money amount is greater than the specified amount.
     */
    public boolean isGreaterThan(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this money amount is greater than or equal to the specified amount.
     */
    public boolean isGreaterThanOrEqual(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) >= 0;
    }

    /**
     * Checks if this money amount is less than the specified amount.
     */
    public boolean isLessThan(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if this money amount is less than or equal to the specified amount.
     */
    public boolean isLessThanOrEqual(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) <= 0;
    }

    /**
     * Returns the currency.
     */
    public Currency currency() {
        return currency;
    }

    /**
     * JavaBean-compatible read accessor for JSON serializers.
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * JavaBean-compatible read accessor for JSON serializers.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the amount as a fixed-point integer where the last two digits represent decimals.
     */
    public BigDecimal fixedPointAmount() {
        return amount;
    }

    /**
     * Returns the amount as a double.
     */
    public BigDecimal doubleAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Money money = (Money) o;
        // Use compareTo for BigDecimal to ensure mathematical equality (ignoring scale)
        return (amount == null ? money.amount == null : amount.compareTo(money.amount) == 0)
            && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        // Normalize BigDecimal to ensure consistent hash codes for mathematically equal values
        BigDecimal normalizedAmount = amount != null ? amount.stripTrailingZeros() : null;
        return Objects.hash(currency, normalizedAmount);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency, amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}

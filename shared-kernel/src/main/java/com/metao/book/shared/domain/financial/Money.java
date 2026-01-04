package com.metao.book.shared.domain.financial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import org.springframework.lang.NonNull;

/**
 * Value object representing an amount of money. The amount is stored as a fixed-point integer where the last two digits
 * represent the decimals.
 */
@Embeddable
public class Money implements ValueObject {

    @JsonProperty("currency")
    private Currency currency;
    @JsonProperty("amount")
    private BigDecimal amount;

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
    @JsonCreator
    public Money(@NonNull @JsonProperty("currency") Currency currency, @JsonProperty("amount") BigDecimal amount) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.amount = amount;
    }

    /**
     * Returns a new {@code Money} object whose amount is the sum of this amount and {@code augend}'s amount.
     *
     * @param augend the {@code Money} object to add to this object.
     * @return {@code this} + {@code augend}
     * @throws IllegalArgumentException if this object and {@code augend} have different currencies.
     */
    @NonNull
    public Money add(@NonNull Money augend) {
        Objects.requireNonNull(augend, "augend must not be null");
        if (currency != augend.currency) {
            throw new IllegalArgumentException("Cannot add two Money objects with different currencies");
        }
        return new Money(currency, amount.add(augend.amount));
    }

    /**
     * Returns a new {@code Money} object whose amount is the difference between this amount and {@code subtrahend}'s
     * amount.
     *
     * @param subtrahend the {@code Money} object to remove from this object.
     * @return {@code this} - {@code augend}
     * @throws IllegalArgumentException if this object and {@code subtrahend} have different currencies.
     */
    @NonNull
    public Money subtract(@NonNull Money subtrahend) {
        Objects.requireNonNull(subtrahend, "subtrahend must not be null");
        if (currency != subtrahend.currency) {
            throw new IllegalArgumentException("Cannot subtract two Money objects with different currencies");
        }
        return new Money(currency, amount.subtract(subtrahend.amount));
    }

    /**
     * Returns a new {@code Money} object whose amount is this amount multiplied by {@code multiplicand}.
     *
     * @param multiplicand the value to multiply the amount by.
     * @return {@code this} * {@code multiplicand}
     */
    @NonNull
    public Money multiply(BigDecimal multiplicand) {
        return new Money(currency, amount.multiply(multiplicand));
    }

    /**
     * Returns a new {@code Money} object whose amount is this amount divided by {@code divisor}.
     *
     * @param divisor the value to divide the amount by.
     * @return {@code this} / {@code divisor}
     */
    @NonNull
    public Money divide(BigDecimal divisor) {
        return new Money(currency, amount.divide(divisor, RoundingMode.HALF_UP));
    }

    /**
     * Creates a new Money object with the specified amount and currency.
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(currency, amount);
    }

    /**
     * Checks if this money amount is greater than the specified amount.
     */
    public boolean isGreaterThan(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this money amount is greater than or equal to the specified amount.
     */
    public boolean isGreaterThanOrEqual(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) >= 0;
    }

    /**
     * Checks if this money amount is less than the specified amount.
     */
    public boolean isLessThan(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if this money amount is less than or equal to the specified amount.
     */
    public boolean isLessThanOrEqual(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare Money objects with different currencies");
        }
        return amount.compareTo(other.amount) <= 0;
    }

    /**
     * Returns the currency.
     */
    @NonNull
    public Currency currency() {
        return currency;
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
        String amountString;
        if (Objects.equals(amount, BigDecimal.ZERO)) {
            amountString = "000";
        } else {
            amountString = amount.toString();
        }
        return String.format("%s %s.%s", currency, amountString.substring(0, amountString.length() - 2),
            amountString.substring(amountString.length() - 2));
    }
}

package com.metao.book.shared.domain.financial;

import com.metao.book.shared.domain.base.ValueObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a VAT (Value Added Tax) percentage.
 */
public class VAT implements ValueObject {

    private final int percentage;

    /**
     * Creates a new {@code VAT} object.
     *
     * @param percentage the percentage as an integer where e.g. 24 means 24 %.
     */
    public VAT(int percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("VAT should not be negative");
        }
        this.percentage = percentage;
    }

    /**
     * Creates a new {@code VAT} object of {@code percentage} is not null.
     *
     * @param percentage the percentage as an integer where e.g. 24 means 24 %.
     * @return the new {@code VAT} object or {@code null} if {@code percentage} is null.
     */
    public static VAT valueOf(Integer percentage) {
        return percentage == null ? null : new VAT(percentage);
    }

    /**
     * Returns the VAT percentage, e.g. 24 % returns 24.
     */
    public int toInteger() {
        return percentage;
    }

    /**
     * Returns the VAT percentage as a fraction, e.g. 24 % returns 0.24.
     */
    public double toDouble() {
        return percentage / 100d;
    }

    /**
     * Adds tax to the given amount.
     *
     * @param amount the amount to add tax to.
     * @return the amount including tax.
     */
    public Money addTax(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return amount.add(calculateTax(amount));
    }

    /**
     * Subtracts tax from the given amount.
     *
     * @param amount the amount to subtract tax from.
     * @return the amount excluding tax.
     */
    public Money subtractTax(Money amount) {
        var withoutTax = (amount.fixedPointAmount().multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(percentage + 100), RoundingMode.HALF_UP));
        Objects.requireNonNull(amount, "amount must not be null");
        return Money.of(amount.currency(), withoutTax);
    }

    /**
     * Calculates the tax for the given amount.
     *
     * @param amount the amount to calculate the tax for.
     * @return the amount of tax.
     */
    public Money calculateTax(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        // Multiply before dividing to avoid a scale-0 intermediate that collapses
        // percentages under 100 to zero (e.g. 21 / 100 at scale 0 rounds to 0).
        var tax = amount.fixedPointAmount()
            .multiply(BigDecimal.valueOf(percentage))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return Money.of(amount.currency(), tax);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VAT vat = (VAT) o;
        return percentage == vat.percentage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentage);
    }

    @Override
    public String toString() {
        return String.format("%d %%", percentage);
    }
}

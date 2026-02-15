package com.metao.book.payment.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.metao.book.shared.domain.base.ValueObject;
import java.util.Arrays;
import java.util.Locale;
import lombok.Getter;
import lombok.NonNull;

/**
 * Payment method value object
 *
 * @param details Masked card number, PayPal email, etc.
 */
public record PaymentMethod(PaymentMethod.Type type,
                            String details) implements ValueObject {

    public PaymentMethod(@NonNull Type type, String details) {
        this.type = type;
        this.details = details != null ? details.trim() : "";
    }

    public static PaymentMethod creditCard(String maskedCardNumber) {
        return new PaymentMethod(Type.CREDIT_CARD, maskedCardNumber);
    }

    public static PaymentMethod debitCard(String maskedCardNumber) {
        return new PaymentMethod(Type.DEBIT_CARD, maskedCardNumber);
    }

    public static PaymentMethod paypal(String email) {
        return new PaymentMethod(Type.PAYPAL, email);
    }

    public static PaymentMethod bankTransfer(String accountInfo) {
        return new PaymentMethod(Type.BANK_TRANSFER, accountInfo);
    }

    public static PaymentMethod digitalWallet(String walletInfo) {
        return new PaymentMethod(Type.DIGITAL_WALLET, walletInfo);
    }

    public boolean isCardPayment() {
        return type == Type.CREDIT_CARD || type == Type.DEBIT_CARD;
    }

    public boolean isDigitalPayment() {
        return type == Type.PAYPAL || type == Type.DIGITAL_WALLET;
    }

    @Override
    @NonNull
    public String toString() {
        return type.getDisplayName() + (details.isEmpty() ? "" : " (" + details + ")");
    }

    @Getter
    public enum Type {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        PAYPAL("PayPal"),
        BANK_TRANSFER("Bank Transfer"),
        DIGITAL_WALLET("Digital Wallet");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static Type fromJson(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Payment method type cannot be null or empty");
            }

            String normalized = value.trim();

            return Arrays.stream(values())
                .filter(type ->
                    type.name().equalsIgnoreCase(normalized)
                        || type.displayName.equalsIgnoreCase(normalized)
                        || type.name().replace('_', ' ').equalsIgnoreCase(normalized)
                        || type.displayName.replace(' ', '_').toUpperCase(Locale.ROOT)
                        .equals(normalized.toUpperCase(Locale.ROOT))
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method type: " + value));
        }
    }
}

package com.metao.book.payment.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
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

    }
}

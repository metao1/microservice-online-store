package com.metao.book.payment.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentMethod value object
 */
class PaymentMethodTest {

    @Test
    void creditCard_shouldCreateCreditCardPaymentMethod() {
        // When
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-****-****-1234");

        // Then
        assertThat(paymentMethod.type()).isEqualTo(PaymentMethod.Type.CREDIT_CARD);
        assertThat(paymentMethod.details()).isEqualTo("****-****-****-1234");
        assertThat(paymentMethod.isCardPayment()).isTrue();
        assertThat(paymentMethod.isDigitalPayment()).isFalse();
    }

    @Test
    void debitCard_shouldCreateDebitCardPaymentMethod() {
        // When
        PaymentMethod paymentMethod = PaymentMethod.debitCard("****-5678");

        // Then
        assertThat(paymentMethod.type()).isEqualTo(PaymentMethod.Type.DEBIT_CARD);
        assertThat(paymentMethod.details()).isEqualTo("****-5678");
        assertThat(paymentMethod.isCardPayment()).isTrue();
        assertThat(paymentMethod.isDigitalPayment()).isFalse();
    }

    @Test
    void paypal_shouldCreatePayPalPaymentMethod() {
        // When
        PaymentMethod paymentMethod = PaymentMethod.paypal("user@example.com");

        // Then
        assertThat(paymentMethod.type()).isEqualTo(PaymentMethod.Type.PAYPAL);
        assertThat(paymentMethod.details()).isEqualTo("user@example.com");
        assertThat(paymentMethod.isCardPayment()).isFalse();
        assertThat(paymentMethod.isDigitalPayment()).isTrue();
    }

    @Test
    void bankTransfer_shouldCreateBankTransferPaymentMethod() {
        // When
        PaymentMethod paymentMethod = PaymentMethod.bankTransfer("IBAN: DE89370400440532013000");

        // Then
        assertThat(paymentMethod.type()).isEqualTo(PaymentMethod.Type.BANK_TRANSFER);
        assertThat(paymentMethod.details()).isEqualTo("IBAN: DE89370400440532013000");
        assertThat(paymentMethod.isCardPayment()).isFalse();
        assertThat(paymentMethod.isDigitalPayment()).isFalse();
    }

    @Test
    void digitalWallet_shouldCreateDigitalWalletPaymentMethod() {
        // When
        PaymentMethod paymentMethod = PaymentMethod.digitalWallet("Apple Pay");

        // Then
        assertThat(paymentMethod.type()).isEqualTo(PaymentMethod.Type.DIGITAL_WALLET);
        assertThat(paymentMethod.details()).isEqualTo("Apple Pay");
        assertThat(paymentMethod.isCardPayment()).isFalse();
        assertThat(paymentMethod.isDigitalPayment()).isTrue();
    }

    @Test
    void toString_shouldReturnFormattedString() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.creditCard("****-1234");

        // When
        String result = paymentMethod.toString();

        // Then
        assertThat(result).isEqualTo("Credit Card (****-1234)");
    }

    @Test
    void toString_withEmptyDetails_shouldReturnTypeOnly() {
        // Given
        PaymentMethod paymentMethod = new PaymentMethod(PaymentMethod.Type.CREDIT_CARD, "");

        // When
        String result = paymentMethod.toString();

        // Then
        assertThat(result).isEqualTo("Credit Card");
    }

    @Test
    void equals_withSameTypeAndDetails_shouldBeEqual() {
        // Given
        PaymentMethod method1 = PaymentMethod.creditCard("****-1234");
        PaymentMethod method2 = PaymentMethod.creditCard("****-1234");

        // Then
        assertThat(method1).isEqualTo(method2);
        assertThat(method1.hashCode()).hasSameHashCodeAs(method2.hashCode());
    }

    @Test
    void equals_withDifferentTypeOrDetails_shouldNotBeEqual() {
        // Given
        PaymentMethod creditCard = PaymentMethod.creditCard("****-1234");
        PaymentMethod debitCard = PaymentMethod.debitCard("****-1234");
        PaymentMethod differentCard = PaymentMethod.creditCard("****-5678");

        // Then
        assertThat(creditCard)
            .isNotEqualTo(debitCard)
            .isNotEqualTo(differentCard);
    }
}

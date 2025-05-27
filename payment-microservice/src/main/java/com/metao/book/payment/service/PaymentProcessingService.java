package com.metao.book.payment.service;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderPaymentEvent; // Corrected to use existing OrderPaymentEvent
import com.metao.book.order.OrderPaymentEvent.Status; // Corrected to use existing Status from OrderPaymentEvent
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);
    private final Random random = new Random();

    public OrderPaymentEvent processPayment(OrderCreatedEvent orderEvent) {
        log.info("Processing payment for order item: {}, product: {}, amount: {} {}", 
                 orderEvent.getId(), orderEvent.getProductId(), orderEvent.getPrice(), orderEvent.getCurrency());

        try {
            // Simulate processing delay
            TimeUnit.SECONDS.sleep(random.nextInt(3) + 1); // 1-3 seconds delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted for order item: {}", orderEvent.getId(), e);
            // Fall through to create a FAILED event or rethrow, depending on desired handling
        }

        boolean paymentSuccessful = random.nextBoolean(); // 50/50 chance for mock

        Status status = paymentSuccessful ? Status.SUCCESSFUL : Status.FAILED;
        String message = paymentSuccessful ? "Payment successful." : "Payment failed due to insufficient funds (mock).";
         
        log.info("Payment for order item {}: {}", orderEvent.getId(), message);

        // Assuming OrderPaymentEvent has these fields or similar.
        // If OrderPaymentEvent.newBuilder() is not found, it means the class is not generated
        // from shared-kernel or not available in classpath.
        return OrderPaymentEvent.newBuilder()
                .setOrderId(orderEvent.getId()) 
                .setPaymentId(UUID.randomUUID().toString())
                .setStatus(status)
                .setErrorMessage(paymentSuccessful ? "" : message) // Assuming ErrorMessage field for failure details
                .setProcessedTimestamp(Instant.now().toEpochMilli())
                .build();
    }
}

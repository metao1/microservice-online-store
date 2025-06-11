package com.metao.book.payment.service;

import com.google.protobuf.Timestamp;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.book.shared.OrderPaymentEvent.Status;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessingService {

    private static final Random random = new Random();
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

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
        String message = paymentSuccessful ? "successful." : "failed due to insufficient funds (mock).";
         
        log.info("Payment for order item {}: {}", orderEvent.getId(), message);

        // Assuming OrderPaymentEvent has these fields or similar.
        // If OrderPaymentEvent.newBuilder() is not found, it means the class is not generated
        // from shared-kernel or not available in classpath.
        return OrderPaymentEvent.newBuilder()
                .setOrderId(orderEvent.getId()) 
                .setPaymentId(UUID.randomUUID().toString())
                .setStatus(status)
                .setErrorMessage(paymentSuccessful ? "" : message) // Assuming ErrorMessage field for failure details
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();
    }
}

package com.metao.book.outbox.infrastructure;

import com.google.protobuf.Message;
import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodecRegistry;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

/** Publishes only durable, service-local outbox messages. */
@Slf4j
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private static final int BATCH_SIZE = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10;
    private static final Duration LEASE_DURATION = Duration.ofMinutes(1);

    private final OutboxStore outboxStore;
    private final ProtobufMessageCodecRegistry codecRegistry;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Value("${outbox.publisher.retry-delay-seconds:30}")
    private long retryDelaySeconds;

    private final String workerId = createWorkerId();

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    public void publishPending() {
        Instant now = Instant.now();
        for (OutboxMessage message : outboxStore.claimPending(
            workerId, BATCH_SIZE, now, now.plus(LEASE_DURATION)
        )) {
            publish(message);
        }
    }

    private void publish(OutboxMessage message) {
        try {
            ProtobufMessageCodec codec = codecRegistry.get(message.eventType(), message.schemaVersion());
            Message payload = codec.deserialize(message.payload());
            kafkaTemplate.send(new ProducerRecord<>(codec.topic(), message.partitionKey(), payload))
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            outboxStore.markPublished(message.eventId(), workerId, Instant.now());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            reschedule(message, exception);
        } catch (ExecutionException | TimeoutException | RuntimeException exception) {
            reschedule(message, exception);
        }
    }

    private void reschedule(OutboxMessage message, Exception exception) {
        String error = exception.getMessage();
        outboxStore.rescheduleFailure(
            message.eventId(), workerId, Instant.now().plusSeconds(retryDelaySeconds),
            error == null || error.isBlank() ? exception.getClass().getSimpleName() : error.substring(0, Math.min(error.length(), 2_000))
        );
        log.error("Failed to publish durable outbox event {}; it will be retried", message.eventId(), exception);
    }

    private static String createWorkerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
        } catch (Exception ignored) {
            return "outbox-worker-" + UUID.randomUUID();
        }
    }
}

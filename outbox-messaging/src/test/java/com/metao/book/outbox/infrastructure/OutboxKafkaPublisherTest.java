package com.metao.book.outbox.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class OutboxKafkaPublisherTest {

    private static final OutboxMessage MESSAGE = new OutboxMessage(
        "event-1", "order", "order-1", "order.created", 1, "order-1",
        new byte[] {1}, Instant.now(), "order-1");

    @Mock
    private OutboxStore outboxStore;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void marksMessagePublishedOnlyAfterKafkaAcknowledgesSend() {
        OutboxKafkaPublisher<String> publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE));
        when(kafkaTemplate.send(anyRecord()))
            .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(kafkaTemplate).send(anyRecord());
        verify(outboxStore).markPublished(any(), any(), any());
    }

    @Test
    void reschedulesMessageWhenKafkaSendFails() {
        OutboxKafkaPublisher<String> publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE));
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send(anyRecord())).thenReturn(failedSend);

        publisher.publishPending();

        verify(outboxStore).rescheduleFailure(any(), any(), any(), any());
    }

    @Test
    void stopsPublishingMessagesForSameOrderingKeyWhenEarlierSendFails() {
        OutboxMessage nextMessage = new OutboxMessage(
            "event-2", "order", "order-1", "order.created", 1, "order-1",
            new byte[] {2}, MESSAGE.occurredAt().plusSeconds(1), "order-1");
        OutboxKafkaPublisher<String> publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE, nextMessage));
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send(anyRecord()))
            .thenReturn(failedSend, CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(kafkaTemplate, times(1)).send(anyRecord());
        verify(outboxStore).rescheduleFailure(eq("event-1"), any(), any(), any());
        verify(outboxStore, never()).markPublished(eq("event-2"), any(), any());
        verify(outboxStore).releaseClaim(eq("event-2"), any());
    }

    @Test
    void continuesPublishingUnorderedMessagesWhenEarlierSendFails() {
        OutboxMessage first = unorderedMessage("event-1", 1, MESSAGE.occurredAt());
        OutboxMessage second = unorderedMessage("event-2", 2, MESSAGE.occurredAt().plusSeconds(1));
        OutboxKafkaPublisher<String> publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(first, second));
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send(anyRecord()))
            .thenReturn(failedSend, CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(kafkaTemplate, times(2)).send(anyRecord());
        verify(outboxStore).markPublished(eq("event-2"), any(), any());
    }

    @Test
    void continuesPublishingDifferentOrderingKeyWhenEarlierSendFails() {
        OutboxMessage nextMessage = new OutboxMessage(
            "event-2", "order", "order-2", "order.created", 1, "order-2",
            new byte[] {2}, MESSAGE.occurredAt().plusSeconds(1), "order-2");
        OutboxKafkaPublisher<String> publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE, nextMessage));
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send(anyRecord()))
            .thenReturn(failedSend, CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(kafkaTemplate, times(2)).send(anyRecord());
        verify(outboxStore).markPublished(eq("event-2"), any(), any());
    }

    private OutboxMessage unorderedMessage(String eventId, int payload, Instant occurredAt) {
        return new OutboxMessage(
            eventId, "order", "order-1", "order.created", 1, "order-1",
            new byte[] {(byte) payload}, occurredAt);
    }

    private ProducerRecord<String, String> anyRecord() {
        return any();
    }

    private OutboxKafkaPublisher<String> publisher() {
        OutboxPayloadCodec<String> codec = new OutboxPayloadCodec<>() {
            @Override
            public boolean supports(String eventType, int schemaVersion) {
                return eventType.equals(MESSAGE.eventType()) && schemaVersion == MESSAGE.schemaVersion();
            }

            @Override
            public String deserialize(byte[] payload) {
                return "payload";
            }

            @Override
            public String topic() {
                return "order-created";
            }
        };
        return new OutboxKafkaPublisher<>(
            outboxStore,
            new OutboxPayloadCodecRegistry<>(List.of(codec)),
            kafkaTemplate
        );
    }
}

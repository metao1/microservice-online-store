package com.metao.book.outbox.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.Empty;
import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodecRegistry;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class OutboxKafkaPublisherTest {

    private static final OutboxMessage MESSAGE = new OutboxMessage(
        "event-1", "order", "order-1", "order.created", 1, "order-1",
        new byte[] {1}, Instant.now());

    @Mock
    private OutboxStore outboxStore;

    @Mock
    private KafkaTemplate<String, com.google.protobuf.Message> kafkaTemplate;

    @Test
    void marksMessagePublishedOnlyAfterKafkaAcknowledgesSend() {
        OutboxKafkaPublisher publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishPending();

        verify(kafkaTemplate).send(any(ProducerRecord.class));
        verify(outboxStore).markPublished(any(), any(), any());
    }

    @Test
    void reschedulesMessageWhenKafkaSendFails() {
        OutboxKafkaPublisher publisher = publisher();
        when(outboxStore.claimPending(any(), any(Integer.class), any(), any()))
            .thenReturn(List.of(MESSAGE));
        CompletableFuture<?> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(failedSend);

        publisher.publishPending();

        verify(outboxStore).rescheduleFailure(any(), any(), any(), any());
    }

    private OutboxKafkaPublisher publisher() {
        ProtobufMessageCodec codec = new ProtobufMessageCodec() {
            @Override
            public boolean supports(String eventType, int schemaVersion) {
                return eventType.equals(MESSAGE.eventType()) && schemaVersion == MESSAGE.schemaVersion();
            }

            @Override
            public com.google.protobuf.Message deserialize(byte[] payload) {
                return Empty.getDefaultInstance();
            }

            @Override
            public String topic() {
                return "order-created";
            }
        };
        return new OutboxKafkaPublisher(
            outboxStore,
            new ProtobufMessageCodecRegistry(List.of(codec)),
            kafkaTemplate
        );
    }
}

package com.metao.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * Kafka Runner helps to submit Kafka messages asynchronously using lightweight Executor On event of
 * {@link ContextClosedEvent} the application will gracefully shut down and wait for in-flight tasks to complete.
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaFactory<V> extends EventHandler<CompletableFuture<SendResult<String, V>>> {

    private final ConcurrentLinkedQueue<Message<String, V>> ongoingQueue = new ConcurrentLinkedQueue<>();

    @Setter
    private String topic;
    @Getter
    private final Class<V> type;
    private final KafkaTemplate<String, V> kafkaTemplate;

    public void subscribe() {
        subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                log.debug("Subscription created: {}", subscription);
            }

            @Override
            public void onNext(CompletableFuture<SendResult<String, V>> item) {
                log.debug("Item received: {}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Error occurred: {}", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                log.debug("Completed");
            }
        });
    }

    public void addEvent(String key, V event) {
        ongoingQueue.add(new Message<>(topic, key, event));
    }

    public void publish() {
        List<Message<String, V>> messagesToPublish = new ArrayList<>();
        while (!ongoingQueue.isEmpty()) {
            Message<String, V> message = ongoingQueue.poll();
            if (message != null) {
                messagesToPublish.add(message);
            }
        }
        for (Message<String, V> message : messagesToPublish) {
            CompletableFuture<SendResult<String, V>> future = kafkaTemplate.send(message.topic, message.key, message.message);
            // The call to 'this.submit(future)' is incorrect because the parent 'EventHandler'
            // is a custom class and not a standard SubmissionPublisher.
            // Since the only subscriber is a logger, we can achieve the same outcome
            // by handling the future directly here.
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error sending message to Kafka topic '{}': {}", message.topic(), ex.getMessage(), ex);
                } else {
                    log.debug("Message sent successfully to topic '{}': {}", message.topic(), result);
                }
            });
        }
    }

    @Override
    public CompletableFuture<SendResult<String, V>> getEvent() {
        throw new UnsupportedOperationException("getEvent should not be called directly in this implementation.");
    }

    @EventListener
    private void run(ContextClosedEvent event) {
        cancel();
    }

    private record Message<K, V>(String topic, K key, V message) {
    }
}

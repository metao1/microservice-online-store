package com.metao.book.shared.application.kafka;

import com.metao.book.shared.application.service.EventHandler;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
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

    private final DelayQueue<Message<String, V>> delayeds = new DelayQueue<>();
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

    public void submit(String key, V event) {
        delayeds.add(new Message<>(topic, key, event, 2000));
    }

    public void publish() {
        publish(delayeds.size());
    }

    @Override
    public CompletableFuture<SendResult<String, V>> getEvent() {
        CompletableFuture<SendResult<String, V>> send = null;
        if (!delayeds.isEmpty()) {
            final Message<String, V> message = delayeds.remove();
            send = kafkaTemplate.send(message.topic, message.key,
                message.message);
        }
        return send;
    }

    @EventListener
    private void run(ContextClosedEvent event) {
        cancel();
    }

    private record Message<K, V>(String topic, K key, V message, long delay) implements Delayed {

        @Override
        public int compareTo(@Nonnull Delayed o) {
            return 0;
        }

        @Override
        public long getDelay(@Nonnull TimeUnit unit) {
            return unit.toMillis(delay);
        }
    }
}

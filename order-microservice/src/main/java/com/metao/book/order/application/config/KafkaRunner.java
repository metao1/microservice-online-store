package com.metao.book.order.application.config;

import com.metao.book.shared.application.service.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

/**
 * Kafka Runner helps to submit Kafka messages asynchronously using lightweight Executor On event of
 * {@link ContextClosedEvent} the application will gracefully shut down and wait for in-flight tasks to complete.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRunner<V> extends EventHandler<String, List<CompletableFuture<SendResult<String, V>>>> {

    private final DelayQueue<Message<String, V>> delayeds = new DelayQueue<>();
    private final KafkaTemplate<String, V> kafkaTemplate;
    private final List<CompletableFuture<SendResult<String, V>>> list = new ArrayList<>();

    public void subscribe() {
        subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                log.debug("Subscription created: {}", subscription);
            }

            @Override
            public void onNext(List<CompletableFuture<SendResult<String, V>>> item) {
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

    public void submit(String topic, String key, V event) {
        delayeds.add(new Message<>(topic, key, event, 2000));
    }

    public void publish() {
        publish(delayeds.size());
    }

    @Override
    public List<CompletableFuture<SendResult<String, V>>> getEvent() {
        for (Message<String, V> delayed : delayeds) {
            list.add(kafkaTemplate.send(delayed.topic, delayed.key, delayed.message));
        }
        return list;
    }

    @EventListener
    private void run(ContextClosedEvent event) {
        cancel();
    }

    private record Message<K, V>(String topic, K key, V message, long delay) implements Delayed {

        @Override
        public int compareTo(@NotNull Delayed o) {
            return 0;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.toMillis(delay);
        }
    }
}

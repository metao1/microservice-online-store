package com.metao.kafka;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EventHandler<V> implements Publisher<V> {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();// virtual-thread
    private Subscription subscription;

    @Override
    public void subscribe(Subscriber<? super V> subscriber) {
        subscription = new EventSubscription(subscriber, executor);
    }

    public void publish(int n) {
        if (subscription != null) {
            subscription.request(n);
        }
    }

    public void cancel() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    protected abstract V getEvent();

    @RequiredArgsConstructor
    private class EventSubscription implements Subscription {

        private final AtomicBoolean isCompleted = new AtomicBoolean(false);
        private final Subscriber<? super V> subscriber;
        private final ExecutorService executor;
        private Future<?> future;

        @Override
        public synchronized void request(long n) {
            if (isCompleted.get()) {
                return;
            }
            if (n <= 0) {
                var exception = new IllegalArgumentException("n must be greater than 0");
                log.error(exception.getMessage(), exception);
                executor.execute(() -> subscriber.onError(exception));
                return;
            }
            future = executor.submit(() -> {
                try {
                    for (long i = 0; i < n && !isCompleted.get(); i++) {
                        subscriber.onNext(getEvent());
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                if (!isCompleted.get()) {
                    subscriber.onComplete();
                }
            });
        }

        @Override
        public synchronized void cancel() {
            // Perform cleanup logic here, e.g., closing resources or waiting for tasks to complete
            log.info("Application is shutting down gracefully...");

            if (future != null) {
                future.cancel(false);
            }
            // Simulate waiting for in-flight tasks to complete
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("Failed to wait for tasks to complete. forcing shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("Failed to wait for tasks to complete: {}", e.getMessage());
            }
            isCompleted.set(true);
            log.info("Resources cleaned up and application shut down.");
        }
    }
}



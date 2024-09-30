package com.metao.book.shared.application.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EventHandler<T> {

    private final ExecutorService executor = ForkJoinPool.commonPool(); // daemon-based
    private Subscription subscription;

    public void subscribe(Subscriber<? super T> subscriber) {
        subscription = new EventSubscription(subscriber, executor);
    }

    public void publish() {
        if (subscription != null) {
            subscription.request(1);
        }
    }

    public void cancel() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    public abstract T getEvent();

    public class EventPublisher implements Publisher<T> {

        @Override
        public void subscribe(Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new EventSubscription(subscriber, executor));
        }

    }

    @RequiredArgsConstructor
    private class EventSubscription implements Subscription {

        private final Subscriber<? super T> subscriber;
        private final ExecutorService executor;
        private AtomicBoolean isCompleted = new AtomicBoolean(false);
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
            if (future != null) {
                future.cancel(false);
            }
            isCompleted.set(true);
        }
    }
}



package com.metao.book.shared;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.metao.book.shared.application.service.EventHandler;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Flow.Subscriber;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class EventHandlerTest {

    @Test
    void testSubscribe() {
        //GIVEN
        Queue<Integer> queue = IntStream.of(1, 2, 3, 4).collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
        Subscriber<Integer> subscriber = mock(Subscriber.class);
        doNothing().when(subscriber).onNext(anyInt());

        var eventHandler = new EventHandler<Integer>() {

            @Override
            public Integer getEvent() {
                return queue.poll();
            }
        };

        //WHEN
        eventHandler.subscribe(subscriber);
        eventHandler.publish(queue.size());

        //THEN
        for (int i = 0; i < queue.size(); i++) {
            int finalI = i;
            await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> verify(subscriber).onNext(finalI + 1));
        }
    }

    @Test
    void testCancel() {
        //GIVEN
        var payload = "This is a test event";
        Subscriber<String> subscriber = mock(Subscriber.class);
        var eventHandler = new EventHandler<String>() {
            @Override
            public String getEvent() {
                return payload;
            }
        };

        //WHEN
        eventHandler.subscribe(subscriber);
        eventHandler.cancel();
        eventHandler.publish(1);

        //THEN
        verify(subscriber, never()).onNext(payload);
    }
}

package com.metao.book.shared;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.metao.book.shared.application.service.EventHandler;
import java.util.concurrent.Flow.Subscriber;
import org.junit.jupiter.api.Test;

class EventHandlerTest {

    private EventHandler<String> eventHandler;

    @Test
    void testSubscribe() {
        //GIVEN
        var payload = "This is a test event";
        Subscriber<String> subscriber = mock(Subscriber.class);
        eventHandler = new EventHandler<>() {
            @Override
            public String getEvent() {
                return payload;
            }
        };

        //WHEN
        eventHandler.subscribe(subscriber);
        eventHandler.publish();

        //THEN
        verify(subscriber).onNext(payload);
    }

    @Test
    void testCancel() {
        //GIVEN
        var payload = "This is a test event";
        Subscriber<String> subscriber = mock(Subscriber.class);
        eventHandler = new EventHandler<>() {
            @Override
            public String getEvent() {
                return payload;
            }
        };

        //WHEN
        eventHandler.subscribe(subscriber);
        eventHandler.cancel();

        //THEN
        verify(subscriber, never()).onNext(payload);
    }
}

package com.metao.book.product.infrastructure.factory.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
public class EventHandler<T> {

    private final List<Consumer<T>> messageHandlers = new LinkedList<>();

    public void subscribe(Consumer<T> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void publish(T event) {
        messageHandlers.forEach(me -> me.accept(event));
    }
}

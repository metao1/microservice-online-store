package com.metao.product.infrustructure.factory.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.metao.product.domain.event.CreateProductEvent;

import org.springframework.stereotype.Service;

import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductEventHandler {

    private final Set<MessageHandler<CreateProductEvent>> messageHandlers = new HashSet<>();

    public void addMessageHandler(MessageHandler<CreateProductEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void sendEvent(CreateProductEvent event) {
        messageHandlers
                .stream()
                .forEach(me -> me.onMessage(event));
    }
}

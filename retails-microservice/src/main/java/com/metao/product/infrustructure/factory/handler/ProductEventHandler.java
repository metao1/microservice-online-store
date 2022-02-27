package com.metao.product.infrustructure.factory.handler;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.event.CreateProductEvent;

import org.springframework.stereotype.Service;

@Service
public class ProductEventHandler {

    private final Set<MessageHandler<CreateProductEvent>> messageHandlers = new HashSet<>();
    private static final ExecutorService executor = Executors.newWorkStealingPool(4);
    private static final long TIMEOUT = 20;

    public void addMessageHandler(MessageHandler<CreateProductEvent> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void sendEvent(CreateProductEvent event) {
        messageHandlers
        //.stream()
        //.map(s-> CompletableFuture.runAsync(()-> s.onMessage(event), executor))
        .forEach(mh -> {
            // try {
            //     mh.get(TIMEOUT, TimeUnit.SECONDS);
            // } catch (InterruptedException | ExecutionException | TimeoutException e) {
            //     Thread.currentThread().interrupt();
            //     log.error(e.getMessage(), e);
            // }
            mh.onMessage(event);
        });
    }

    public CreateProductEvent createEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
    }

}

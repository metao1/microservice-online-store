package com.metao.book.product.infrastructure.factory;

import com.metao.book.product.infrastructure.factory.handler.FileHandler;
import com.metao.book.product.infrastructure.factory.handler.LogMessageHandler;
import com.metao.book.product.infrastructure.factory.handler.ProductEventHandler;
import com.metao.book.product.infrastructure.factory.handler.ProductMessageHandler;
import com.metao.book.product.infrastructure.mapper.ProductDtoMapper;
import com.metao.book.product.infrastructure.util.EventUtil;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGenerator {

    private final ProductMessageHandler productMessageHandler;
    private final LogMessageHandler logMessageHandler;

    private final ProductDtoMapper mapper;
    private final FileHandler fileHandler;
    private final ProductEventHandler eventHandler;

    @PostConstruct
    public void produceProducts() {
        eventHandler.addMessageHandler(productMessageHandler);
        eventHandler.addMessageHandler(logMessageHandler);
    }

    public void loadProducts() {
        log.info("importing products data from resources");
        try (var source = fileHandler.readFromFile("data/products.txt")) {
            source
                .map(mapper::convertToDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EventUtil::createEvent)
                .forEach(eventHandler::sendEvent);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("finished writing to database.");
    }

    /**
     * Waits for the {@link ReadinessState#ACCEPTING_TRAFFIC} and starts task execution
     *
     * @param event The {@link AvailabilityChangeEvent}
     * @throws ExecutionException   If task execution failed
     * @throws InterruptedException If task execution has been interrupted
     */
    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) throws ExecutionException, InterruptedException {
        log.info("Application ReadinessState changed to: {}", event.getState());
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            /*
             * We can not use simple CommandLineRunner because it blocks the main thread.
             * While CommandLineRunner tasks are executed the Application does not turn in to the ReadinessState#ACCEPTING_TRAFFIC.
             * That's why we use CompletableFuture and wait for their execution.
             * https://www.baeldung.com/spring-liveness-readiness-probes#1-readiness-and-liveness-state-transitions
             */
            CompletableFuture.runAsync(this::loadProducts).get();
        }
    }

}

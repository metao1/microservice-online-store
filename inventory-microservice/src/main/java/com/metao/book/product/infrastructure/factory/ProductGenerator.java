package com.metao.book.product.infrastructure.factory;

import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.factory.handler.EventHandler;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.infrastructure.mapper.ProductDtoMapper;
import com.metao.book.product.infrastructure.mapper.ProductEventMapper;
import com.metao.book.shared.application.service.FileHandler;
import jakarta.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "local")
public class ProductGenerator implements KafkaListenerConfigurer {

    private final EventHandler<ProductCreatedEvent> eventHandler;
    private final ProductKafkaHandler productKafkaHandler;
    private final ProductDtoMapper dtoMapper;
    private final ProductEventMapper productMapper;

    @Value("${product-sample-data-path}")
    String productsDataPath;

    @PostConstruct
    public void produceProducts() {
        eventHandler.subscribe(productKafkaHandler);
    }

    public void loadProducts() {
        log.info("importing products data from resources");
        try (var source = FileHandler.readResourceInPath(getClass(), productsDataPath)) {
            source
                .map(dtoMapper::toDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(productMapper::toEvent)
                .filter(Objects::nonNull)
                .forEach(eventHandler::publish);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("finished publishing products.");
    }

    /**
     * Waits for the {@link ReadinessState#ACCEPTING_TRAFFIC} and starts task execution
     *
     * @param event The {@link AvailabilityChangeEvent}
     */
    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) {
        log.info("Application ReadinessState changed to: {}", event.getState());
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            /*
             * We can not use simple CommandLineRunner because it blocks the main thread.
             * While CommandLineRunner tasks are executed the Application does not turn in
             * to the ReadinessState#ACCEPTING_TRAFFIC.
             * That's why we use CompletableFuture and wait for their execution.
             * https://www.baeldung.com/spring-liveness-readiness-probes#1-readiness-and-
             * liveness-state-transitions
             */
            CompletableFuture.runAsync(this::loadProducts);
        }
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {

    }
}

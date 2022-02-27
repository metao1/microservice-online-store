package com.metao.product.infrustructure.factory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.factory.handler.FileHandler;
import com.metao.product.infrustructure.factory.handler.LogMessageHandler;
import com.metao.product.infrustructure.factory.handler.ProductEventHandler;
import com.metao.product.infrustructure.factory.handler.ProductMessageHandler;
import com.metao.product.infrustructure.mapper.ProductDtoMapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGenerator implements InitializingBean{
    
    private final ProductMessageHandler productMessageHandler;
    private final LogMessageHandler logMessageHandler;

    private final ProductDtoMapper mapper;
    private final FileHandler fileHandler;
    private final ProductEventHandler eventHandler;

    @Async
    @PostConstruct
    public void produceProducts() {
        log.debug("importing products data from resources");
        try {
            String dataSource = fileHandler.readFromFile("data/products.json");
            Stream.of(dataSource.split("!"))
                    .map(mapper::convertToDto)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(CreateProductEvent::createEvent)
                    .forEach(eventHandler::sendEvent);
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        log.debug("finished writing to database.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventHandler.addMessageHandler(productMessageHandler);
        eventHandler.addMessageHandler(logMessageHandler);
    }

}

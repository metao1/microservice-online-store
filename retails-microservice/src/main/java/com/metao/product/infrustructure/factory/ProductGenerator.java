package com.metao.product.infrustructure.factory;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.infrustructure.mapper.DTOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGenerator {
    private final DTOMapper<String, Optional<ProductDTO>> mapper;
    private final FileHandler fileHandler;
    private final EventHandler eventHandler;

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
                    .map(eventHandler::createEvent)
                    .forEach(eventHandler::sendEvent);
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        log.debug("finished writing to database.");
    }

}

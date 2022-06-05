package com.metao.book.retails.infrustructure.factory;

import com.metao.book.retails.infrustructure.factory.handler.FileHandler;
import com.metao.book.retails.infrustructure.factory.handler.LogMessageHandler;
import com.metao.book.retails.infrustructure.factory.handler.ProductEventHandler;
import com.metao.book.retails.infrustructure.factory.handler.ProductMessageHandler;
import com.metao.book.retails.infrustructure.mapper.ProductDtoMapper;
import com.metao.book.retails.infrustructure.util.EventUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
//@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductGenerator implements InitializingBean {

    private final ProductMessageHandler productMessageHandler;
    private final LogMessageHandler logMessageHandler;

    private final ProductDtoMapper mapper;
    private final FileHandler fileHandler;
    private final ProductEventHandler eventHandler;
    private final Executor executor;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        executor.execute(this::loadProducts);
    }

}

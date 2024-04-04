package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogMessageHandler implements MessageHandler<ProductCreatedEvent> {

    @Override
    public void onMessage(@NonNull ProductCreatedEvent productCreatedEvent) {
        log.info("event {} created.", productCreatedEvent.productEvent().asin());
    }

}

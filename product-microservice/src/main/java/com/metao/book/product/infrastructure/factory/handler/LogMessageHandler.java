package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.domain.event.CreateProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogMessageHandler implements MessageHandler<CreateProductEvent> {

    @Override
    public void onMessage(@NonNull CreateProductEvent createProductEvent) {
        log.info("event {} created.", createProductEvent.productDto().asin());
    }

}

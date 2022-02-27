package com.metao.product.infrustructure.factory.handler;

import com.metao.product.domain.event.CreateProductEvent;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogMessageHandler implements MessageHandler<CreateProductEvent> {

        @Override
        public void onMessage(@NonNull CreateProductEvent createProductEvent) {
                log.info("event {} created.", createProductEvent.productDTO().getAsin());
        }

}

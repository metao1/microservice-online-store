package com.metao.book.order.application.service;

import com.metao.book.order.infrastructure.KafkaListenableCallback;
import com.order.microservice.avro.OrderAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaOrderListener implements KafkaListenableCallback<String, OrderAvro> {

    @Override
    public void onSuccess(SendResult<String, OrderAvro> result) {
        if (result != null) {
            log.info("orderId: {} , sent to topic: {}", result.getProducerRecord().key(),
                    result.getProducerRecord().topic());
        }
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("error sending to kafka topic, msg: {}", ex.getMessage());
    }

}

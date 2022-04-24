package com.metao.book.order.application.service;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.KafkaListenableCallback;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaOrderListener implements KafkaListenableCallback<OrderId, OrderEntity> {

        @Override
        public void onSuccess(SendResult<OrderId, OrderEntity> result) {                
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

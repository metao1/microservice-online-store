package com.metao.book.order.infrastructure.kafka;

import javax.validation.constraints.NotBlank;

import com.metao.book.order.application.dto.OrderDTO;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    public void send(String topic, @NotBlank String orderId, @NotBlank OrderDTO orderDTO) {
        log.info("sending order='{}' to topic='{}'", orderDTO, topic);
        kafkaTemplate.send(topic, orderId, orderDTO);
    }
}

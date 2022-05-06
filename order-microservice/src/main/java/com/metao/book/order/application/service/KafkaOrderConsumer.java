package com.metao.book.order.application.service;

import com.order.microservice.avro.OrderAvro;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

//@Slf4j
//@Server
//public class KafkaOrderConsumer {
//
//    private CountDownLatch latch = new CountDownLatch(10);
//    private String payload = null;
//
//    @KafkaListener(id = "orders", topics = "order", groupId = "payment")
//    public void onEvent(ConsumerRecord<String, OrderAvro> record) {
//        log.info("Consumed message -> {}", record.value());
//        latch.countDown();
//    }
//
//    public CountDownLatch getLatch() {
//        return latch;
//    }
//
//    public String getPayload() {
//        return payload;
//    }
//}
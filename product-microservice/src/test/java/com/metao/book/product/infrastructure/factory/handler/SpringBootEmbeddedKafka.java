package com.metao.book.product.infrastructure.factory.handler;

import org.springframework.kafka.test.context.EmbeddedKafka;

@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class SpringBootEmbeddedKafka {

}
package com.metao.book.order.kafka;

import com.metao.book.shared.OrderAvro;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.annotation.BeforeTestClass;

@DirtiesContext
@RequiredArgsConstructor
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class SpringBootEmbeddedKafka {

    protected EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, true, 0, "product");

    @BeforeTestClass
    public void setUpClass() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
        System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
        System.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.LongSerializer.class.getName());
        System.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderAvro.class.getName());
    }

}
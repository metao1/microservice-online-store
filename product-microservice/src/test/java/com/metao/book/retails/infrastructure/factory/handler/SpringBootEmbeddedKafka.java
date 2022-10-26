package com.metao.book.retails.infrastructure.factory.handler;

import com.metao.book.retails.domain.event.CreateProductEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.annotation.BeforeTestClass;

@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public abstract class SpringBootEmbeddedKafka {

    @Autowired
    public EmbeddedKafkaBroker kafkaEmbeddedBroker;

    public static EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, true, 0, "product");

    @BeforeTestClass
    public void setUpClass() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
        System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
        System.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongSerializer.class.getName());
        System.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CreateProductEvent.class.getName());
    }

}
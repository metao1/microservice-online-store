package com.metao.book.checkout.application;

import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.validation.annotation.Validated;

import java.util.Random;

import static com.order.microservice.avro.Status.NEW;

@Slf4j
@Validated
@Configuration
@EnableKafka
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    private final Random random = new Random();
    private final CheckoutService checkoutService;
    @Value("${kafka.stream.topic.payment-order}")
    String paymentOrderTopic;

    @KafkaListener(id = "payment-order-listener", topics = "${kafka.stream.topic.payment-order}")
    public void listen(OrderAvro order) {
        log.info("Received: {}", order);
        if (order.getStatus().equals(NEW)) {
            checkoutService.reserve(order);
        } else {
            checkoutService.confirm(order);
        }
    }

//    @Bean("order-payment")
//    public KStream<String, OrderAvro> stream(@Value("${kafka.stream.topic.payment-order}") String paymentOrderTopic,
//                                             @Value("${kafka.stream.topic.order}") String orderTopic,
//                                             KafkaOrderProducer template,
//                                             StreamsBuilder builder) {
//        var orderSerde = new SpecificAvroSerde<OrderAvro>();
//        var rsvSerde = new SpecificAvroSerde<Reservation>();
//        KStream<String, OrderAvro> stream = builder
//                .stream(orderTopic, Consumed.with(Serdes.String(), orderSerde))
//                .peek((k, order) -> log.info("New: {}", order));
//
//        var customerOrderStoreSupplier = Stores.persistentKeyValueStore("stock-order");
//        Aggregator<String, OrderAvro, Reservation> aggregatorService = (id, order, rsv) -> {
//            if (order.getStatus().equals(Status.CONFIRM)) {
//                rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
//            } else if (order.getStatus().equals(Status.ROLLBACK)) {
//                if (!order.getSource().equals("PAYMENT")) {
//                    rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
//                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
//                }
//            } else if (order.getStatus().equals(NEW)) {
//                if (order.getPrice() <= rsv.getAmountAvailable()) {
//                    rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getPrice());
//                    rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
//                    order.setStatus(ACCEPT);
//                } else {
//                    order.setStatus(REJECT);
//                }
//                template.send(paymentOrderTopic, order.getOrderId(), order);
//            }
//
//            log.info("{}", rsv);
//            return rsv;
//        };
//
//        stream.selectKey((k, v) -> v.getCustomerId())
//                .groupByKey(Grouped.with(Serdes.Long(), orderSerde))
//                .aggregate(() -> Reservation.newBuilder().setAmountAvailable(random.nextDouble()).build(),
//                        aggregatorService,
//                        Materialized.<String, Reservation>as(customerOrderStoreSupplier)
//                                .withKeySerde(Serdes.String())
//                                .withValueSerde(rsvSerde))
//                .toStream()
//                .peek((k, trx) -> log.info("Commit: {}", trx));
//
//        return stream;
//    }
}

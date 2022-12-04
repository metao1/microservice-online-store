package com.metao.book.product.application.service;

import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ProductsResponseEvent;
import com.metao.book.shared.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
public class KafkaListenerService {
    private final OrderManageService orderManager;

    @KafkaListener(id = "get-products", topics = "get-products", groupId = "get-products")
    public void processProductRequestedEvent(ConsumerRecord<String, ProductsResponseEvent> record) {
        log.info("Received order-request='{}'", record);
    }

    @KafkaListener(id = "orders", topics = "order-test", groupId = "order-group")
    public void onEvent(ConsumerRecord<Long, OrderEvent> record) {
        log.info("Consumed message -> {}", record.value());
        var order = record.value();
        try {
            if (order.getStatus().equals(Status.NEW)) {
                orderManager.reserve(order);
            } else {
                orderManager.confirm(order);
            }
        } catch (Exception e) {
            log.error("Error processing order", e);
        }
    }

    // this builds a stream from orders then aggregates the order with reservation
    //@Bean
//    public KStream<Long, OrderEvent> stream(StreamsBuilder builder,
//                                           ProductService productService,
//                                           SpecificAvroSerde<OrderEvent> OrderEventSerde,
//                                           SpecificAvroSerde<Reservation> rsvSerde) {
//
//        KStream<Long, OrderEvent> stream = builder
//                .stream(orderKafkaTopic, Consumed.with(Serdes.Long(), OrderEventSerde));
//
//        KeyValueBytesStoreSupplier stockOrderStoreSupplier =
//                Stores.persistentKeyValueStore(orderKafkaTopic);
//
//        Aggregator<Long, OrderEvent, Reservation> aggrSrv = (id, order, rsv) -> {
//            switch (order.getStatus()) {
//                case CONFIRM -> rsv.setAmountReserved(rsv.getAmountReserved() - order.getQuantity());
//                case ROLLBACK -> {
//                    if (!order.getSource().equals(STOCK)) {
//                        rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getQuantity());
//                        rsv.setAmountReserved(rsv.getAmountReserved() - order.getQuantity());
//                    }
//                }
//                case NEW -> {
//                    if (order.getQuantity() <= rsv.getAmountAvailable()) {
//                        rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getQuantity());
//                        rsv.setAmountReserved(rsv.getAmountReserved() + order.getQuantity());
//                        order.setStatus(ACCEPT);
//                    } else {
//                        order.setStatus(REJECT);
//                    }
//                    template.send(paymentKafkaTopic, order.getOrderId(), order)
//                            .addCallback(result -> log.info("Sent: {}",
//                                    result != null ? result.getProducerRecord().value() : null), ex -> {});
//                }
//            }
//            log.info("{}", rsv);
//            return rsv;
//        };
//
//        stream.selectKey((k, v) -> v.getProductId())
//                .groupByKey(Grouped.with(Serdes.Long(), OrderEventSerde))
//                .aggregate(() -> Reservation.newBuilder()
//                                .setAmountAvailable(productService.countProducts())
//                                .setAmountReserved(0)
//                                .build(),
//                        aggrSrv,
//                        Materialized.<Long, Reservation>as(stockOrderStoreSupplier)
//                                .withKeySerde(Serdes.Long())
//                                .withValueSerde(rsvSerde))
//                .toStream()
//                .peek((k, trx) -> log.info("Commit: {}", trx));
//
//        return stream;
//    }

}

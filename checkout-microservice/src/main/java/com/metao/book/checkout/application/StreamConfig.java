package com.metao.book.checkout.application;

/*@Slf4j
@Validated
@Configuration
public class StreamConfig {

    @Bean("order-payment")
    public KStream<String, OrderEvent> stream(
        @Value("${kafka.stream.topic.payment}") String paymentOrderTopic,
        @Value("${kafka.stream.topic.order}") String orderTopic,
        KafkaOrderProducer template,
        StreamsBuilder builder
    ) {
        var orderSerde = new SpecificAvroSerde<OrderEvent>();
        var rsvSerde = new SpecificAvroSerde<Reservation>();
        KStream<String, OrderEvent> stream = builder
            .stream(orderTopic, Consumed.with(Serdes.String(), orderSerde))
            .peek((k, order) -> log.info("New: {}", order));

        var customerOrderStoreSupplier = Stores.persistentKeyValueStore("stock-order");
        Aggregator<String, OrderEvent, Reservation> reservedAggregation = (id, order, rsv) -> {
            if (order.getStatus().equals(Status.CONFIRM)) {
                rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
            } else if (order.getStatus().equals(Status.ROLLBACK)) {
                if (!order.getSource().equals("PAYMENT")) {
                    rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                }
            } else if (order.getStatus().equals(NEW)) {
                if (order.getPrice() <= rsv.getAmountAvailable()) {
                    rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getPrice());
                    rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
                    order.setStatus(ACCEPT);
                } else {
                    order.setStatus(REJECT);
                }
                template.send(paymentOrderTopic, order.getOrderId(), order);
            }
            return rsv;
        };

        stream.selectKey((k, v) -> v.getCustomerId())
            .groupByKey(Grouped.with(Serdes.String(), orderSerde))
            .aggregate(() -> Reservation.newBuilder().setAmountAvailable(random.nextDouble()).build(),
                reservedAggregation,
                Materialized.<String, Reservation>as(customerOrderStoreSupplier)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(rsvSerde))
            .toStream()
            .peek((k, trx) -> log.info("Commit: {}", trx));

        return stream;
    }
}*/

package com.metao.book.product.application.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.metao.book.product.application.config.ProductStreamConfig;
import com.metao.book.product.application.service.OrderAggregator;
import com.metao.book.product.application.service.OrderProductJoiner;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.StockReservationEvent;
import com.metao.book.shared.kafka.StreamsUtils;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;

@Slf4j
class ProductStreamConfigTest {

        private static final String PRODUCT_ID = "PRODUCT_ID", PRODUCT_ID2 = "PRODUCT_ID2";
        ProductStreamConfig kafkaStreamsConfig = new ProductStreamConfig(new OrderProductJoiner(),
                        new OrderAggregator());

        @Test
        void productReservationStream() {
            final var orderInputTopicName = new NewTopic("order", 1, (short) 1);
            final var productInputTopicName = new NewTopic("product", 1, (short) 1);
            final var orderStockTopicName = new NewTopic("order-product-test", 1, (short) 1);
            final var reservationTopicName = new NewTopic("order-reservation-test", 1, (short) 1);

            final var streamProps = StreamsUtils.getStreamsProperties();
            final var configMap = StreamsUtils.propertiesToMap(streamProps);

            final var sb = new StreamsBuilder();
            final var orderSerdes = StreamsUtils.<OrderEvent>getSpecificAvroSerdes(configMap);
            final var productSerdes = StreamsUtils.<ProductEvent>getSpecificAvroSerdes(configMap);
            final var reservationSerdes = StreamsUtils.<StockReservationEvent>getSpecificAvroSerdes(configMap);

            kafkaStreamsConfig.reservationStream(
                sb,
                reservationTopicName,
                productInputTopicName,
                orderInputTopicName,
                productSerdes,
                orderSerdes,
                reservationSerdes);

                try (final TopologyTestDriver testDriver = new TopologyTestDriver(sb.build(), streamProps)) {
                        var orderList = createOrderInput();
                        var productList = createProductInput();
                        var productInputTopic = testDriver.createInputTopic(productInputTopicName.name(),
                                        Serdes.String().serializer(),
                                        productSerdes.serializer());
                        var orderInputTopic = testDriver.createInputTopic(orderInputTopicName.name(),
                                        Serdes.String().serializer(),
                                        orderSerdes.serializer());
                        productList.forEach(product -> productInputTopic.pipeInput(product.getProductId(), product));
                        orderList.forEach(order -> orderInputTopic.pipeInput(order.getProductId(), order));

                        var outputTopic = testDriver.createOutputTopic(orderStockTopicName.name(),
                                        Serdes.String().deserializer(),
                                        orderSerdes.deserializer());

                        var reservationOutput = testDriver.createOutputTopic(reservationTopicName.name(),
                                        Serdes.String().deserializer(),
                                        reservationSerdes.deserializer());

                        OrderEvent expectedOrderValues;
                        while (!outputTopic.isEmpty()) {
                                expectedOrderValues = outputTopic.readValue();
                                log.info("order:" + expectedOrderValues);
                                assertThat(expectedOrderValues)
                                                .extracting(OrderEvent::getStatus)
                                                .isEqualTo(Status.ACCEPT)
                                                .isNotNull();
                        }

                    StockReservationEvent expectedReservationValues;
                        expectedReservationValues = reservationOutput.readValue();
                        log.info("reservation:" + expectedReservationValues);
                        assertThat(expectedReservationValues)
                                        .satisfies(reservationEvent -> {
                                                assertEquals(98, (double) reservationEvent.getAvailable());
                                                assertEquals(2, (double) reservationEvent.getReserved());
                                                assertEquals(PRODUCT_ID, reservationEvent.getProductId());
                                                assertEquals("CUSTOMER_ID", reservationEvent.getCustomerId());
                                        })
                                        .isNotNull();

                        expectedReservationValues = reservationOutput.readValue();
                        assertThat(expectedReservationValues)
                                        .satisfies(reservationEvent -> {
                                                assertEquals(97, (double) reservationEvent.getAvailable());
                                                assertEquals(3, (double) reservationEvent.getReserved());
                                                assertEquals(PRODUCT_ID, reservationEvent.getProductId());
                                                assertEquals("CUSTOMER_ID", reservationEvent.getCustomerId());
                                        })
                                        .isNotNull();

                        expectedReservationValues = reservationOutput.readValue();
                        assertThat(expectedReservationValues)
                                        .satisfies(reservationEvent -> {
                                                assertEquals(99, (double) reservationEvent.getAvailable());
                                                assertEquals(1, (double) reservationEvent.getReserved());
                                                assertEquals(PRODUCT_ID2, reservationEvent.getProductId());
                                                assertEquals("CUSTOMER_ID", reservationEvent.getCustomerId());
                                        })
                                        .isNotNull();

                }
        }

        private List<ProductEvent> createProductInput() {
                List<ProductEvent> productList = new LinkedList<>();
                productList.add(ProductEvent.newBuilder()
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setCurrency(Currency.eur)
                                .setPrice(123)
                                .setVolume(100d)
                                .setTitle("TITLE")
                                .setProductId(PRODUCT_ID)
                                .setDescription("DESCRIPTION")
                                .setImageUrl("IMAGE_URL")
                                .build());
                productList.add(ProductEvent.newBuilder()
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setCurrency(Currency.eur)
                                .setPrice(200)
                                .setVolume(100d)
                                .setTitle("TITLE")
                                .setProductId(PRODUCT_ID2)
                                .setDescription("DESCRIPTION")
                                .setImageUrl("IMAGE_URL")
                                .build());
                return productList;
        }

        private List<OrderEvent> createOrderInput() {
                List<OrderEvent> orderList = new LinkedList<>();
                orderList.add(OrderEvent.newBuilder()
                                .setProductId(PRODUCT_ID)
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setCurrency(Currency.eur)
                                .setStatus(Status.NEW)
                                .setSource("source")
                                .setCustomerId("id")
                                .setOrderId("1")
                                .setQuantity(2)
                                .setPrice(10)
                                .build());
                orderList.add(OrderEvent.newBuilder()
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setProductId(PRODUCT_ID)
                                .setStatus(Status.NEW)
                                .setOrderId("2")
                                .setCurrency(Currency.eur)
                                .setQuantity(1)
                                .setPrice(10)
                                .setCustomerId("id")
                                .setSource("source")
                                .build());
                orderList.add(OrderEvent.newBuilder()
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setProductId(PRODUCT_ID2)
                                .setStatus(Status.NEW)
                                .setOrderId("3")
                                .setCurrency(Currency.eur)
                                .setQuantity(1)
                                .setPrice(10)
                                .setCustomerId("id")
                                .setSource("source")
                                .build());
                return orderList;
        }
}
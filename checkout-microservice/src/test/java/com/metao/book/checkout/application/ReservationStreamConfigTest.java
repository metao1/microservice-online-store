package com.metao.book.checkout.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.kafka.StreamsUtils;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.Test;

public class ReservationStreamConfigTest {

        @Test
        void shouldAggregateRecord() {
            var streamProps = StreamsUtils.getStreamsProperties();
            final String inputTopicName = "input";
            final String outputTopicName = "output";
            final Map<String, String> configMap = StreamsUtils.propertiesToMap(streamProps);
            final SpecificAvroSerde<OrderEvent> orderSerdes = StreamsUtils.getSpecificAvroSerdes(configMap);

            final StreamsBuilder sb = new StreamsBuilder();
            final KStream<String, OrderEvent> orderEventStream = sb.stream(inputTopicName,
                Consumed.with(Serdes.String(), orderSerdes));
            orderEventStream
                .groupByKey()
                .aggregate(() -> 0.0, (key, order, total) -> total + order.getPrice(),
                    Materialized.with(Serdes.String(), Serdes.Double()))
                .toStream()
                .to(outputTopicName, Produced.with(Serdes.String(), Serdes.Double()));
                try (final TopologyTestDriver testDriver = new TopologyTestDriver(sb.build(), streamProps)) {
                        var inputTopic = testDriver.createInputTopic(inputTopicName,
                                        Serdes.String().serializer(),
                                        orderSerdes.serializer());
                        var outputTopic = testDriver.createOutputTopic(outputTopicName,
                                        Serdes.String().deserializer(),
                                        Serdes.Double().deserializer());
                        List<OrderEvent> orderList = new LinkedList<>();
                        orderList.add(OrderEvent.newBuilder()
                                        .setCreatedOn(Instant.now().toEpochMilli())
                                        .setOrderId("1")
                                        .setCurrency(Currency.eur)
                                        .setProductId("1")
                                        .setQuantity(1)
                                        .setPrice(10)
                                        .setCustomerId("id")
                                        .setStatus(Status.ACCEPT)
                                        .setSource("source")
                                        .build());
                        orderList.add(OrderEvent.newBuilder()
                                        .setCreatedOn(Instant.now().toEpochMilli())
                                        .setOrderId("2")
                                        .setCurrency(Currency.eur)
                                        .setProductId("2")
                                        .setQuantity(1)
                                        .setPrice(15)
                                        .setCustomerId("id")
                                        .setStatus(Status.ACCEPT)
                                        .setSource("source")
                                        .build());
                        orderList.forEach(order -> inputTopic.pipeInput(order.getOrderId(), order));

                        var expectedValues = outputTopic.readValue();
                        assertThat(expectedValues).isNotNull();
                        assertThat(expectedValues).isEqualTo(10);
                }

        }
}

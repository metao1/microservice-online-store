package com.metao.book.order.kafka;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.metao.book.order.utils.StreamsUtils;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.Status;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.Test;

public class OrderStreamTest {

    @Test
    void shouldAggregateRecord() {
        var streamProps = getStreamsProperties();
        final String inputTopicName = "input";
        final String outputTopicName = "output";
        final Map<String, Object> configMap = StreamsUtils.propertiesToMap(streamProps);
        final SpecificAvroSerde<OrderAvro> orderSerds = StreamsUtils.getSpecificAvroSerds(configMap);

        final StreamsBuilder sb = new StreamsBuilder();
        final KStream<String, OrderAvro> orderAvroStream =
            sb.stream(inputTopicName, Consumed.with(Serdes.String(), orderSerds));
        orderAvroStream
            .groupByKey()
            .aggregate(() -> 0.0, (key, order, total) -> total + order.getPrice()
                , Materialized.with(Serdes.String(), Serdes.Double())).toStream()
            .to(outputTopicName, Produced.with(Serdes.String(), Serdes.Double()));
        try (final TopologyTestDriver testDriver = new TopologyTestDriver(sb.build(), streamProps)) {
            var inputTopic = testDriver.createInputTopic(inputTopicName,
                Serdes.String().serializer(),
                orderSerds.serializer());
            var outputTopic = testDriver.createOutputTopic(outputTopicName,
                Serdes.String().deserializer(),
                Serdes.Double().deserializer());
            List<OrderAvro> orderList = new LinkedList<>();
            orderList.add(OrderAvro.newBuilder()
                .setOrderId("1")
                .setCurrency(Currency.eur)
                .setProductId("1")
                .setQuantity(1)
                .setPrice(10)
                .setCustomerId("id")
                .setStatus(Status.ACCEPT)
                .setSource("source")
                .build());
            orderList.add(OrderAvro.newBuilder()
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

    private static Properties getStreamsProperties() {
        final Properties streamProps = new Properties();
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregate_test");
        streamProps.put("schema.registry.url", "mock://aggregation-test");
        return streamProps;
    }
}

package com.metao.book.shared.kafka;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.experimental.UtilityClass;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.kafka.config.TopicBuilder;

@UtilityClass
public class StreamsUtils {

    public static Map<String, String> propertiesToMap(Properties streamProps) {
        var hasMap = new HashMap<String, String>();
        streamProps.forEach((key, value) -> hasMap.put((String) key, (String) value));
        return hasMap;
    }

    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerdes(
        Map<String, String> configMap
    ) {
        var serde = new SpecificAvroSerde<T>();
        serde.configure(configMap, false);
        return serde;
    }

    public static NewTopic createTopic(String topicName) {
        return TopicBuilder
            .name(topicName)
            .partitions(1)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }

    public static Properties getStreamsProperties() {
        final Properties streamProps = new Properties();
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregate_test");
        streamProps.put("schema.registry.url", "mock://aggregation-test");
        streamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde.class.getName());

        return streamProps;
    }
}
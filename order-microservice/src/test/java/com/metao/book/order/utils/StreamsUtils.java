package com.metao.book.order.utils;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.experimental.UtilityClass;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

@UtilityClass
public class StreamsUtils {

    public static Map<String, Object> propertiesToMap(Properties streamProps) {
        var hasMap = new HashMap<String, Object>();
        streamProps.forEach((key, value) -> hasMap.put((String) key, value));
        return hasMap;
    }

    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerds(Map<String, Object> configMap) {
        var serde = new SpecificAvroSerde<T>();
        serde.configure(configMap, false);
        return serde;
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

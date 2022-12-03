package com.metao.book.order.utils;

import com.metao.book.shared.OrderAvro;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamsUtils {

    public static Map<String, Object> propertiesToMap(Properties streamProps) {
        var hasMap = new HashMap<String, Object>();
        streamProps.forEach((key, value) -> hasMap.put((String) key, value));
        return hasMap;
    }

    public static SpecificAvroSerde<OrderAvro> getSpecificAvroSerds(Map<String, Object> configMap) {
        var serde = new SpecificAvroSerde<OrderAvro>();
        serde.configure(configMap, false);
        return serde;
    }
}

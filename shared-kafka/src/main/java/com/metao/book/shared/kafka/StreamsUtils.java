package com.metao.book.shared.kafka;

import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.kafka.config.TopicBuilder;

@UtilityClass
public class StreamsUtils {

    public static NewTopic createTopic(String topicName) {
        return TopicBuilder
            .name(topicName)
            .partitions(1)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            .compact()
            .build();
    }
}
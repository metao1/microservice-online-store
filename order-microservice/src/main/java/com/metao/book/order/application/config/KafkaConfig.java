package com.metao.book.order.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    @Bean
    public NewTopic orderTopic(@Value("${kafka.topic.name}") String kafkaTopic) {
        return createTopic(kafkaTopic);
    }

//    @Bean
//    public Map<String, NewTopic> orderTopic(KafkaTopic kafkaTopic) {
//        return kafkaTopic.getTopics().stream()
//            .collect(Collectors.toMap(
//                KafkaTopic.Topic::id,
//                KafkaConfig::createTopic)
//            );
//    }

    private static NewTopic createTopic(String topic) {
        return TopicBuilder
            .name(topic)
            .partitions(1)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
            //.compact()
            .build();
    }
//
//    @Getter
//    @ConfigurationProperties(prefix = "kafka")
//    public static class KafkaTopic {
//
//        public record Topic(String id, String name, String groupId) {
//
//        }
//
//        Topic topic;
//
//    }
}

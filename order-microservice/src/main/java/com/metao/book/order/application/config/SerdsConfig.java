package com.metao.book.order.application.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.metao.book.order.application.dto.OrderDTO;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class SerdsConfig {

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;
	
        @Bean
	public RecordMessageConverter converter() {
		return new JsonMessageConverter();
	}

        @Bean
        public ObjectMapper provideObjectMapper() {                
                var objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                return objectMapper;
        }

        @Bean
        public Map<String, Object> consumerConfigs() {
                Map<String, Object> props = new HashMap<>();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, "json");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
                return props;
        }

        @Bean
        public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OrderDTO>> kafkaListenerContainerFactory() {
                var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderDTO>();
                factory.setConsumerFactory(consumerFactory());
                factory.setConcurrency(3);
                factory.getContainerProperties().setPollTimeout(3000);
                return factory;
        }

        @Bean
        public ConsumerFactory<String, OrderDTO> consumerFactory() {
                return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(OrderDTO.class));
        }

}

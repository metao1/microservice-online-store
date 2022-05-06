package order.presentation;

import com.metao.book.order.application.service.KafkaOrderConsumer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class KafkaOrderConsumerConfiguration {

        @Bean
        KafkaOrderConsumer kafkaOrderConsumer() {
                return new KafkaOrderConsumer();
        }
}
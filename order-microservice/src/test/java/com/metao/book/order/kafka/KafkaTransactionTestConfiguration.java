package com.metao.book.order.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * This configuration is only needed for Embedded Kafka along with Transaction Management to work
 */
@Configuration
public class KafkaTransactionTestConfiguration {

    @Bean
    public DataSourceTransactionManager kafkaTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

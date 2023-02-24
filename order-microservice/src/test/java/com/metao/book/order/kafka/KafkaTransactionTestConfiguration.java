package com.metao.book.order.kafka;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * This configuration is only needed for Embedded Kafka along with Transaction Management to work
 */
@TestConfiguration
@EnableTransactionManagement
public class KafkaTransactionTestConfiguration {

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSourceTransactionManager kafkaTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

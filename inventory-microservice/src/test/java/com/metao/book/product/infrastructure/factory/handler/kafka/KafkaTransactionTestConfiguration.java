package com.metao.book.product.infrastructure.factory.handler.kafka;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class KafkaTransactionTestConfiguration {

    @Bean
    public DataSourceTransactionManager kafkaTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

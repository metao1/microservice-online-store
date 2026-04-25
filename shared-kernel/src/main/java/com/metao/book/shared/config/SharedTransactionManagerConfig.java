package com.metao.book.shared.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaTransactionManager;

/**
 * Shared transaction manager configuration.
 * Eliminates duplicate TransactionManagerConfig classes across services.
 */
@Configuration
@EnableTransactionManagement
public class SharedTransactionManagerConfig {

    @Bean(name = "transactionManager")
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

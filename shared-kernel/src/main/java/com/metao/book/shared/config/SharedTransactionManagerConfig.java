package com.metao.book.shared.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * Shared transaction manager configuration.
 * Eliminates duplicate TransactionManagerConfig classes across services.
 */
@Configuration
public class SharedTransactionManagerConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public SharedTransactionManagerConfig(
        @Qualifier("transactionManager") PlatformTransactionManager transactionManager
    ) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }
}

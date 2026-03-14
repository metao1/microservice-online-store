package com.metao.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
public class TransactionManagerConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public TransactionManagerConfig(
        @Qualifier("transactionManager") PlatformTransactionManager transactionManager
    ) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }
}

package com.metao.book.product.application.config;

import com.metao.book.shared.application.ObjectMapperConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@ImportAutoConfiguration(value = ObjectMapperConfig.class)
public class AppConfig {

    /**
     * After including Spring Data JPA there are two registered TransactionManager beans with names transactionManager
     * and kafkaTransactionManager. Therefore we need to choose the name of the transaction manager inside the
     * @Transactional annotation or introduce {@link JpaTransactionManager}.
     * In the first approach, we add a new entity to the database.
     * The primary key id is auto-generated in the database and then returned to the object. After that, we
     * get groupId and generate the sequence of orders within that group. Of course, both operations (save to database,
     * sent to Kafka) are part of the same transaction.
     * There is also possible to configure  {@link JpaTransactionManager} as below to chain it using JPA
     * In this project I decided to use the first approach
     **/
//    @Bean
//    @Primary
//    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}

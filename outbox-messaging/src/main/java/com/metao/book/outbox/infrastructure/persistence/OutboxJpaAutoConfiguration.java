package com.metao.book.outbox.infrastructure.persistence;

import com.metao.book.outbox.application.OutboxStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(EntityManager.class)
@Import(OutboxEntityScanRegistrar.class)
public class OutboxJpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxStore.class)
    OutboxStore outboxStore(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        return new JpaOutboxStore(entityManager);
    }
}

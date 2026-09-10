package com.metao.book.outbox.infrastructure;

import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.outbox.infrastructure.persistence.OutboxJpaAutoConfiguration;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import com.metao.book.shared.infrastructure.messaging.protobuf.DelegatingDomainEventTranslator;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration(after = OutboxJpaAutoConfiguration.class)
@ConditionalOnBean(OutboxStore.class)
public class OutboxMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    <T> OutboxPayloadCodecRegistry<T> outboxPayloadCodecRegistry(List<OutboxPayloadCodec<T>> codecs) {
        return new OutboxPayloadCodecRegistry<>(codecs);
    }

    @Bean
    @ConditionalOnMissingBean
    <T> OutboxKafkaPublisher<T> outboxKafkaPublisher(
        OutboxStore outboxStore,
        OutboxPayloadCodecRegistry<T> codecRegistry,
        KafkaTemplate<String, T> kafkaTemplate
    ) {
        return new OutboxKafkaPublisher<>(outboxStore, codecRegistry, kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    AfterCommitOutboxDispatcher afterCommitOutboxDispatcher(OutboxKafkaPublisher<?> publisher) {
        return new AfterCommitOutboxDispatcher(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    DomainEventPublisherPort domainEventPublisher(
        DelegatingDomainEventTranslator translator,
        OutboxStore outboxStore,
        AfterCommitOutboxDispatcher dispatcher
    ) {
        return new TransactionalOutboxDomainEventPublisherPort(translator, outboxStore, dispatcher);
    }
}

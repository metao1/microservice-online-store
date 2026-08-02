package com.metao.book.outbox.infrastructure;

import com.google.protobuf.Message;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.shared.application.messaging.DomainEventPublisher;
import com.metao.book.shared.infrastructure.messaging.protobuf.DelegatingDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodecRegistry;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnBean(OutboxStore.class)
public class OutboxMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ProtobufMessageCodecRegistry protobufMessageCodecRegistry(List<ProtobufMessageCodec> codecs) {
        return new ProtobufMessageCodecRegistry(codecs);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(KafkaTemplate.class)
    OutboxKafkaPublisher outboxKafkaPublisher(
        OutboxStore outboxStore,
        ProtobufMessageCodecRegistry codecRegistry,
        KafkaTemplate<String, Message> kafkaTemplate
    ) {
        return new OutboxKafkaPublisher(outboxStore, codecRegistry, kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    AfterCommitOutboxDispatcher afterCommitOutboxDispatcher(OutboxKafkaPublisher publisher) {
        return new AfterCommitOutboxDispatcher(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    DomainEventPublisher domainEventPublisher(
        DelegatingDomainEventTranslator translator,
        OutboxStore outboxStore,
        AfterCommitOutboxDispatcher dispatcher
    ) {
        return new TransactionalOutboxDomainEventPublisher(translator, outboxStore, dispatcher);
    }
}

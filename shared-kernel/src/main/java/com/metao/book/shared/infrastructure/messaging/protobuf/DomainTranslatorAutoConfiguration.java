package com.metao.book.shared.infrastructure.messaging.protobuf;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DelegatingDomainEventTranslator.class)
public class DomainTranslatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DelegatingDomainEventTranslator delegatingDomainEventTranslator(List<ProtobufDomainEventTranslator> translators) {
        return new DelegatingDomainEventTranslator(translators);
    }
}

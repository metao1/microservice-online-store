package com.metao.book.shared.domain.base;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DelegatingDomainEventTranslator.class)
public class DomainTranslatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DelegatingDomainEventTranslator.class)
    public DelegatingDomainEventTranslator delegatingDomainEventTranslator(
        List<ProtobufDomainTranslator<?>> translators
    ) {
        return new DelegatingDomainEventTranslator(translators);
    }
}

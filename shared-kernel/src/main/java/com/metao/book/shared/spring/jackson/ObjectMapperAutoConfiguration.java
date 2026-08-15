package com.metao.book.shared.spring.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import java.math.BigDecimal;
import java.util.Currency;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class ObjectMapperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper provideObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .addMixIn(Money.class, MoneyJsonMixin.class)
            .addMixIn(VAT.class, VatJsonMixin.class)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .build();
    }

    private abstract static class MoneyJsonMixin {

        @JsonCreator
        MoneyJsonMixin(
            @JsonProperty("currency") Currency currency,
            @JsonProperty("amount") BigDecimal amount
        ) {
        }

        @JsonProperty("currency")
        abstract Currency currency();

        @JsonProperty("amount")
        abstract BigDecimal fixedPointAmount();
    }

    private abstract static class VatJsonMixin {

        @JsonCreator
        VatJsonMixin(int percentage) {
        }

        @JsonValue
        abstract int toInteger();
    }

}

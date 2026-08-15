package com.metao.book.shared.spring.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class ObjectMapperAutoConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapperAutoConfiguration().provideObjectMapper();

    @Test
    void serializesAndDeserializesMoneyWithoutDomainJacksonAnnotations() throws Exception {
        Money money = Money.of(Currency.getInstance("EUR"), new BigDecimal("12.34"));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(money));

        assertThat(json.get("currency").textValue()).isEqualTo("EUR");
        assertThat(json.get("amount").decimalValue()).isEqualByComparingTo("12.34");
        assertThat(objectMapper.treeToValue(json, Money.class)).isEqualTo(money);
    }

    @Test
    void serializesAndDeserializesVatAsItsPercentage() throws Exception {
        VAT vat = new VAT(19);

        String json = objectMapper.writeValueAsString(vat);

        assertThat(json).isEqualTo("19");
        assertThat(objectMapper.readValue(json, VAT.class)).isEqualTo(vat);
    }
}

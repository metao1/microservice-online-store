package com.metao.book.order.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import com.metao.book.order.kafka.KafkaTransactionTestConfiguration;
import com.metao.book.order.kafka.SpringBootEmbeddedKafka;
import com.metao.book.shared.application.ObjectMapperConfig;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.test.TestUtils;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "container"})
@Import({
    ObjectMapperConfig.class,
    KafkaTransactionTestConfiguration.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest extends SpringBootEmbeddedKafka {

    @MockBean
    KafkaOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc restTemplate;

    @Test
    public void createOrderIsOk() throws Exception {
        // get request for '/order'
        var order = OrderDTO.builder()
            .orderId("123")
            .productId("1234567891")
            .customerId("CUSTOMER_ID")
            .status(Status.NEW)
            .currency(Currency.EUR)
            .quantity(BigDecimal.valueOf(100))
            .price(BigDecimal.valueOf(123d))
            .build();

        this.restTemplate.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(objectMapper, order)))
            .andExpect(status().isOk());

    }

}

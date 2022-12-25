package com.metao.book.order.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import javax.persistence.criteria.Order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.config.OrderStreamConfig;
import com.metao.book.order.application.config.SerdsConfig;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.application.service.OrderService;
import com.metao.book.order.domain.Currency;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import com.metao.book.order.kafka.KafkaOrderConsumerTestConfig;
import com.metao.book.order.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.order.kafka.SpringBootEmbeddedKafka;
import com.metao.book.order.utils.TestUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@Import({
                KafkaConfig.class,
                OrderService.class,
                KafkaOrderService.class,
                OrderMapper.class,
                KafkaProductConsumerConfiguration.class,
                KafkaOrderConsumerTestConfig.class,
                SerdsConfig.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest extends SpringBootEmbeddedKafka {

        @Autowired
        private MockMvc restTemplate;

        @MockBean
        KafkaOrderService orderService;

        @LocalServerPort
        private int port;

        @Test
        public void createOrderIsOk() throws IOException, Exception {
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
                                .content(TestUtils.convertObjectToJsonBytes(order)))
                                .andExpect(status().isOk());
        }

}

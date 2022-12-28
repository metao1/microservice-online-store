package com.metao.book.order.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.config.SerdesConfig;
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
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles({ "test" })
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestInstance(Lifecycle.PER_CLASS)
@Import({
                KafkaConfig.class,
                OrderService.class,
                KafkaOrderService.class,
                OrderMapper.class,
                KafkaProductConsumerConfiguration.class,
                KafkaOrderConsumerTestConfig.class,
                SerdesConfig.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderControllerTest extends SpringBootEmbeddedKafka {

        @Autowired
        private MockMvc restTemplate;

        @MockBean
        KafkaOrderService orderService;

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
                                .content(TestUtils.convertObjectToJsonBytes(order)))
                                .andExpect(status().isOk());
        }

}

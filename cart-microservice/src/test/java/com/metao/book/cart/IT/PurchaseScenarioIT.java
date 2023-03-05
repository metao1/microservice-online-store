package com.metao.book.cart.IT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.cart.controller.ShoppingCartController;
import com.metao.book.cart.repository.ShoppingCartRepository;
import com.metao.book.cart.service.ShoppingCartFactory;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.cart.util.BasePostgresIntegrationTest;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.application.service.order.OrderEventValidator;
import com.metao.book.shared.kafka.RemoteKafkaService;
import com.metao.book.shared.test.TestUtils;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "container"})
@WebMvcTest(ShoppingCartController.class)
@Import({ShoppingCartFactory.class, CartMapperService.ToCartDto.class, OrderEventValidator.class,
    CartMapperService.ToEventMapper.class})
public class PurchaseScenarioIT extends BasePostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RemoteKafkaService<String, OrderEvent> orderTemplate;

    @MockBean
    KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @MockBean
    ShoppingCartRepository shoppingCartRepository;

    @MockBean
    NewTopic orderTopic;

    @Test
    @SneakyThrows
    void submitOrderInCarts_isSuccessful() {
        var body = "user_id";
        mockMvc.perform(post("/cart/submit")
            .content(TestUtils.convertObjectToJsonBytes(objectMapper, body))
        ).andExpect(status().isBadRequest());
    }
}

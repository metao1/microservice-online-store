package com.metao.book.cart.IT;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.cart.controller.ShoppingCartController;
import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.repository.ShoppingCartRepository;
import com.metao.book.cart.service.ConstantsTest;
import com.metao.book.cart.service.ShoppingCartFactory;
import com.metao.book.cart.service.ShoppingCartService;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.cart.util.BasePostgresIntegrationTest;
import com.metao.book.shared.OrderEvent;

import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.kafka.RemoteKafkaService;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "container"})
@WebMvcTest(ShoppingCartController.class)
@Import({ShoppingCartFactory.class, CartMapperService.ToCartDto.class, OrderEventValidator.class,
    CartMapperService.toDtoMapper.class})
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

    @SpyBean
    ShoppingCartService shoppingCartService;

    @MockBean
    NewTopic orderTopic;

    @Test
    @SneakyThrows
    void submitEmptyOrderInCarts_isBadRequest() {
        var body = "user_id";
        mockMvc.perform(post("/cart/submit")
            .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, body))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void submitOrderInCarts_isSuccessful() {
        final var shoppingCart = ShoppingCart.createCart(ConstantsTest.USER_ID,
            ConstantsTest.ASIN,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_QUANTITY,
            Currency.EUR);

        when(shoppingCartRepository.findOrdersInCartByUserId(eq(shoppingCart.getUserId())))
            .thenReturn(Set.of(shoppingCart));

        mockMvc.perform(post("/cart/submit")
            .param("user_id", shoppingCart.getUserId())
        ).andExpect(status().isCreated());

        verify(shoppingCartService).clearCart(eq(shoppingCart.getUserId()));
    }

    @Test
    @SneakyThrows
    void submitOrderInCartsMoreThanOnce_isBad() {
        final var shoppingCart = ShoppingCart.createCart(ConstantsTest.USER_ID,
            ConstantsTest.ASIN,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_QUANTITY,
            Currency.EUR);

        when(shoppingCartRepository.findOrdersInCartByUserId(eq(shoppingCart.getUserId())))
            .thenReturn(Set.of(shoppingCart));

        mockMvc.perform(post("/cart/submit")
            .param("user_id", shoppingCart.getUserId())
        ).andExpect(status().isCreated());

        mockMvc.perform(post("/cart/submit")
            .param("user_id", shoppingCart.getUserId())
        ).andExpect(status().isBadRequest());

        verify(shoppingCartService).clearCart(eq(shoppingCart.getUserId()));
    }
}

package com.metao.book.cart.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.repository.ShoppingCartRepository;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.application.service.order.OrderEventValidator;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.kafka.RemoteKafkaService;
import com.metao.book.shared.test.TestUtils.StreamBuilder;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.mockito.Mock;

@TestInstance(Lifecycle.PER_CLASS)
class ShoppingCartServiceTest {

    ShoppingCartRepository shoppingCartRepository = spy(ShoppingCartRepository.class);

    @Mock
    RemoteKafkaService<String, OrderEvent> remoteKafkaService;

    @Mock
    OrderEventValidator orderEventValidator;

    CartMapperService.ToEventMapper cartMapperService = new CartMapperService.ToEventMapper();
    NewTopic orderTopic = new NewTopic("order-topic", 1, (short) 1);

    ShoppingCartFactory shoppingCartService = new ShoppingCartFactory(
        remoteKafkaService,
        cartMapperService,
        shoppingCartRepository,
        orderEventValidator,
        orderTopic);

    @TestFactory
    Stream<DynamicTest> addAndGetShoppingCartScenario() {
        // Generates display names like: input:5, input:37, input:85, etc.
        Function<ShoppingCart, String> displayNameGenerator = (input) -> "input:" + input;
        ThrowingConsumer<ShoppingCart> testExecutor = (shoppingCart) -> {
            shoppingCartService.addOrderToShoppingCart(shoppingCart);
            verify(shoppingCartRepository).save(shoppingCart);
            assertNotNull(shoppingCartService.getProductsInCartByUserId(shoppingCart.getUserId()));
        };
        Stream<ShoppingCart> of = buildShoppingCartStream();
        return DynamicTest.stream(of.iterator(), displayNameGenerator, testExecutor);
    }

    private Stream<ShoppingCart> buildShoppingCartStream() {
        return StreamBuilder.of(ShoppingCart.class, 1, 2,
            i -> ShoppingCart.createCart(ConstantsTest.USER_ID,
                ConstantsTest.ASIN,
                ConstantsTest.DEFAULT_PRICE,
                ConstantsTest.DEFAULT_PRICE,
                ConstantsTest.DEFAULT_QUANTITY,
                Currency.EUR));
    }
}
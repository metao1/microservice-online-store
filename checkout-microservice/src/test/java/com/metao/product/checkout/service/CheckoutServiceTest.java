package com.metao.product.checkout.service;

import com.metao.product.checkout.clients.ProductCatalogRestClient;
import com.metao.product.checkout.clients.ShoppingCartRestClient;
import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.domain.ProductInventoryEntity;
import com.metao.product.checkout.exception.CartIsEmptyException;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;
import com.metao.product.checkout.exception.UserException;
import com.metao.product.checkout.repository.ProductInventoryRepository;
import com.metao.product.checkout.service.impl.CheckoutServiceImplementation;
import com.metao.product.models.ProductDTO;
import com.metao.product.utils.DateFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @InjectMocks
    private CheckoutServiceImplementation checkoutServiceImplementation;
    private static final int QUANTITY = 12;
    private final String USER_ID = "userId";
    final String orderDetail = "Customer bought these Items:  Product: title, Quantity: 12; orderEntity: 144.0";
    final double orderTotal = 144d;
    final double price = 12d;
    final String title = "title";

    @Spy
    CheckoutService checkoutService;

    @Spy
    private ShoppingCartRestClient shoppingCartRestClient;

    @Mock
    private ProductCatalogRestClient productCatalogRestClient;

    @Mock
    private ProductInventoryRepository productInventoryRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Spy
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private EntityManager entityManager;

    @Test
    void checkoutSuccessful() throws NotEnoughProductsInStockException, CartIsEmptyException, UserException {

        @Pattern(regexp = "^[0-9]{10}") String ASIN = "1234567890";
        ProductDTO productDTO = ProductDTO.builder()
                .asin(ASIN)
                .title(title)
                .price(price)
                .build();
        when(productCatalogRestClient.getProductDetails(USER_ID))
                .thenReturn(productDTO);
        Map<String, Integer> productCatalogMap = new HashMap<>();

        productCatalogMap.put(USER_ID, QUANTITY);
        when(shoppingCartRestClient.getProductsInCart(USER_ID))
                .thenReturn(productCatalogMap);
        checkoutServiceImplementation.setTransactionManager(platformTransactionManager);
        Query query = Mockito.mock(Query.class);
        ProductInventoryEntity productInventoryEntity = new ProductInventoryEntity();
        productInventoryEntity.setId(ASIN);
        productInventoryEntity.setQuantity(QUANTITY);
        when(productInventoryRepository.findById(USER_ID)).thenReturn(Optional.of(productInventoryEntity));
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(entityManager.createNativeQuery(anyString()).executeUpdate()).thenReturn(0);//success checkout
        Map<String, Integer> userId = shoppingCartRestClient.getProductsInCart(USER_ID);
        Assertions.assertNotNull(userId);
        OrderEntity order = checkoutService.createOrder(USER_ID, orderDetail, orderTotal);
        OrderEntity orderEntityResult = checkoutServiceImplementation.checkout(USER_ID);
        verify(shoppingCartRestClient).clearCart(USER_ID);

        assertThat(orderEntityResult)
                .extracting(OrderEntity::getOrderDetails)
                .isNotNull()
                .isEqualTo(order.getOrderDetails());

        assertThat(orderEntityResult)
                .extracting(OrderEntity::getOrderTime)
                .isNotNull()
                .isEqualTo(DateFormatter.now());

        assertThat(orderEntityResult)
                .extracting(OrderEntity::getUserId)
                .isNotNull()
                .isEqualTo(USER_ID);
    }

    @Test
    public void checkoutFailed_noProductInCart() {

    }

    @Test
    void createOrder() {
        OrderEntity order = checkoutService.createOrder(USER_ID, orderDetail, orderTotal);
        assertThat(order)
                .extracting(OrderEntity::getUserId)
                .isNotNull()
                .isEqualTo(USER_ID);

        assertThat(order)
                .extracting(OrderEntity::getOrderDetails)
                .isNotNull()
                .isEqualTo(orderDetail);

        assertThat(order)
                .extracting(OrderEntity::getOrderTime)
                .isNotNull()
                .isEqualTo(DateFormatter.now());

    }
}
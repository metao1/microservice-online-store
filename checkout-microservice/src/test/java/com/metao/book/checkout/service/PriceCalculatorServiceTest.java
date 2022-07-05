package com.metao.book.checkout.service;

// import com.metao.book.checkout.clients.ProductCatalogRestClient;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.HashMap;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class PriceCalculatorServiceTest {

// @Mock
// private ProductCatalogRestClient productCatalogRestClient;

// @InjectMocks
// private PriceCalculatorService priceCalculatorService;

// @Test
// void calculateTotalPriceInCart() {
// String ASIN = "1234567890";
// String ASIN2 = "1234567891";
// ProductDTO productDTO = ProductDTO.builder()
// .asin(ASIN)
// .title("title")
// .price(109d)
// .build();
// ProductDTO productDTO2 = ProductDTO.builder()
// .asin(ASIN2)
// .title("title2")
// .price(200d)
// .build();
// when(productCatalogRestClient.getProductDetails(ASIN))
// .thenReturn(productDTO);
// when(productCatalogRestClient.getProductDetails(ASIN2))
// .thenReturn(productDTO2);
// Map<String, Integer> productKeyAndQuantityMap = new HashMap<>();
// productKeyAndQuantityMap.put(ASIN, 2);
// productKeyAndQuantityMap.put(ASIN2, 1);
// Double totalPrice =
// priceCalculatorService.calculateTotalPriceInCart(productKeyAndQuantityMap);
// assertThat(totalPrice).isEqualTo(2 * 109d + 200d);
// }
// }

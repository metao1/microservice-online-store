package com.metao.book.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.domain.exception.OrderNotFoundException; // Correct import
import com.metao.book.order.application.cart.ShoppingCart; // Correct import for the entity
import com.metao.book.order.presentation.dto.AddItemRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingCartController.class)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String asin;
    private Currency currency;
    private ShoppingCartDto shoppingCartDto;
    private ShoppingCart shoppingCartEntity;
    private Long currentTime;

    @BeforeEach
    void setUp() {
        userId = "testUser";
        asin = "B00TESTASIN";
        currency = Currency.getInstance("USD");
        currentTime = System.currentTimeMillis();

        Set<ShoppingCartItem> items = new HashSet<>();
        items.add(new ShoppingCartItem(asin, BigDecimal.ONE, BigDecimal.TEN, currency));
        // ShoppingCartDto(Long createdOn, String userId, Set<ShoppingCartItem> shoppingCartItems)
        shoppingCartDto = new ShoppingCartDto(null, userId, items); // createdOn is not set by service for getCart
        
        shoppingCartEntity = new ShoppingCart(userId, asin, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, currency);
        shoppingCartEntity.setCreatedOn(currentTime);
        shoppingCartEntity.setUpdatedOn(currentTime);
    }

    @Test
    void getCartByUserId_whenCartExists_returnsOkWithCartDto() throws Exception {
        when(shoppingCartService.getCartForUser(userId)).thenReturn(shoppingCartDto);

        mockMvc.perform(get("/cart/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id").value(userId)) // Matches @JsonProperty in DTO
                .andExpect(jsonPath("$.shopping_cart_items[0].asin").value(asin));
    }

    @Test
    void addItemToCart_returnsOkWithCartItem() throws Exception {
        AddItemRequestDTO requestDTO = new AddItemRequestDTO(BigDecimal.ONE, BigDecimal.TEN, currency);
        when(shoppingCartService.addItemToCart(eq(userId), eq(asin), eq(requestDTO.quantity()), eq(requestDTO.price()), eq(requestDTO.currency())))
                .thenReturn(shoppingCartEntity);

        mockMvc.perform(post("/cart/{userId}/{asin}", userId, asin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asin").value(asin))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void updateItemQuantity_whenPositiveQuantity_returnsOkWithUpdatedItem() throws Exception {
        UpdateCartItemQtyDTO requestDTO = new UpdateCartItemQtyDTO(BigDecimal.valueOf(5));
        ShoppingCart updatedEntity = new ShoppingCart(userId, asin, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(5), currency);
        updatedEntity.setCreatedOn(currentTime);
        updatedEntity.setUpdatedOn(currentTime + 1000); // Simulate update

        when(shoppingCartService.updateItemQuantity(userId, asin, requestDTO.quantity()))
                .thenReturn(updatedEntity);

        mockMvc.perform(put("/cart/{userId}/{asin}", userId, asin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asin").value(asin))
                .andExpect(jsonPath("$.quantity").value(5));
    }
    
    @Test
    void updateItemQuantity_whenZeroQuantity_returnsNoContent() throws Exception {
        UpdateCartItemQtyDTO requestDTO = new UpdateCartItemQtyDTO(BigDecimal.ZERO);
        when(shoppingCartService.updateItemQuantity(userId, asin, requestDTO.quantity()))
                .thenReturn(null); // Service returns null when item is deleted

        mockMvc.perform(put("/cart/{userId}/{asin}", userId, asin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateItemQuantity_whenItemNotFound_throwsException_returnsNotFound() throws Exception {
        UpdateCartItemQtyDTO requestDTO = new UpdateCartItemQtyDTO(BigDecimal.valueOf(5));
        when(shoppingCartService.updateItemQuantity(userId, asin, requestDTO.quantity()))
            .thenThrow(new OrderNotFoundException("Item not found for update")); // Use the correct exception

        mockMvc.perform(put("/cart/{userId}/{asin}", userId, asin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound()); // Assuming @ControllerAdvice handles OrderNotFoundException
    }

    @Test
    void removeItemFromCart_whenItemExists_returnsNoContent() throws Exception {
        doNothing().when(shoppingCartService).removeItemFromCart(userId, asin);

        mockMvc.perform(delete("/cart/{userId}/{asin}", userId, asin))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItemFromCart_whenItemNotFound_throwsException_returnsNotFound() throws Exception {
        doThrow(new OrderNotFoundException("Item not found for delete")).when(shoppingCartService).removeItemFromCart(userId, asin);

        mockMvc.perform(delete("/cart/{userId}/{asin}", userId, asin))
                .andExpect(status().isNotFound()); // Assuming @ControllerAdvice
    }

    @Test
    void clearCart_returnsNoContent() throws Exception {
        doNothing().when(shoppingCartService).clearCart(userId);

        mockMvc.perform(delete("/cart/{userId}", userId))
                .andExpect(status().isNoContent());
    }
}
```

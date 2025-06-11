package com.metao.book.order.domain;

import com.metao.book.order.application.cart.OrderRepository;
import com.metao.book.shared.domain.financial.Money; // Correct import from shared-kernel
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import for Sort
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity orderEntity;
    private String orderIdToUseInTests; // Renamed to avoid confusion with OrderEntity's orderId field

    @BeforeEach
    void setUp() {
        orderIdToUseInTests = "manualTestOrderId123"; // A manually set ID for predictable testing
        
        // Using the constructor: public OrderEntity(String customerId, String productId, BigDecimal quantity, Money money, OrderStatus status)
        // This constructor internally generates an orderId like: customerId + "::" + productId + "::" + System.currentTimeMillis();
        Money money = new Money(Currency.getInstance("USD"), BigDecimal.TEN);
        orderEntity = new OrderEntity("cust123", "prod456", BigDecimal.ONE, money, OrderStatus.NEW);
        
        // Override the generated orderId with our predictable one for testing getByOrderId
        orderEntity.setOrderId(orderIdToUseInTests); 
    }

    @Test
    void getOrderByOrderId_whenOrderExists_returnsOptionalOfOrder() {
        when(orderRepository.findByOrderId(orderIdToUseInTests)).thenReturn(Optional.of(orderEntity));

        Optional<OrderEntity> result = orderService.getOrderByOrderId(orderIdToUseInTests);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderIdToUseInTests);
    }

    @Test
    void getOrderByOrderId_whenOrderDoesNotExist_returnsEmptyOptional() {
        String nonExistentOrderId = "nonExistentId";
        when(orderRepository.findByOrderId(nonExistentOrderId)).thenReturn(Optional.empty());

        Optional<OrderEntity> result = orderService.getOrderByOrderId(nonExistentOrderId);

        assertThat(result).isEmpty();
    }

    @Test
    void getOrderByProductIdsAndOrderStatus_callsRepository() {
        Set<String> productIds = Set.of("prod456");
        Set<OrderStatus> statuses = Set.of(OrderStatus.NEW);
        int offset = 0;
        int pageSize = 10;
        String sortByFieldName = "createdTime"; 
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        
        Page<OrderEntity> expectedPage = new PageImpl<>(List.of(orderEntity), pageable, 1);

        // We mock the repository call, OrderSpecifications is used internally by the service
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<OrderEntity> result = orderService.getOrderByProductIdsAndOrderStatus(productIds, statuses, offset, pageSize, sortByFieldName);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).containsExactly(orderEntity);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    void getOrderByProductIdsAndOrderStatus_whenNoResults_returnsEmptyPage() {
        Set<String> productIds = Set.of("prodNonExistent");
        Set<OrderStatus> statuses = Set.of(OrderStatus.NEW);
        int offset = 0;
        int pageSize = 10;
        String sortByFieldName = "createdTime";
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        
        Page<OrderEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<OrderEntity> result = orderService.getOrderByProductIdsAndOrderStatus(productIds, statuses, offset, pageSize, sortByFieldName);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void save_callsRepositorySave() {
        // No need to mock the return of save if it's void or we don't use the returned value from save
        // when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity); // This line can be removed if save returns void or is not chained

        orderService.save(orderEntity);

        verify(orderRepository).save(eq(orderEntity));
    }
}


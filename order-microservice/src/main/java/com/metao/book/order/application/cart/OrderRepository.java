package com.metao.book.order.application.cart;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import java.util.List; // Added for List return type
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for managing OrderEntity instances.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, OrderId> {

    /**
     * Finds an OrderEntity by its orderId.
     * 
     * @param orderId the ID of the order to find
     * @return an Optional containing the found OrderEntity, or empty if not found
     */
    @Query("""
            select distinct o
                from OrderEntity o
                    where o.orderId = :orderId
            """)
    Optional<OrderEntity> findByOrderId(String orderId);

    /**
     * Finds all OrderEntity instances matching the given specification and
     * pageable.
     * 
     * @param spec     the specification to filter the orders
     * @param pageable the pagination information
     * @return a Page of OrderEntity instances
     */
    Page<OrderEntity> findAll(
            Specification<OrderEntity> spec,
            Pageable pageable);

    /**
     * Finds all OrderEntity instances for a given customerId.
     *
     * @param customerId the ID of the customer
     * @return a List of OrderEntity instances
     */
    List<OrderEntity> findAllByCustomerId(String customerId);
}

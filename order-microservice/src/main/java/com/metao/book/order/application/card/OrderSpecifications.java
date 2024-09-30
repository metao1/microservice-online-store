package com.metao.book.order.application.card;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class OrderSpecifications {

    public static Specification<OrderEntity> findByOrdersByCriteria(
        Set<String> productIds, Set<OrderStatus> statuses
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (!CollectionUtils.isEmpty(productIds)) {
                predicate = criteriaBuilder.and(
                    predicate,
                    root.get("productId").in(productIds)
                );
            }
            if (!CollectionUtils.isEmpty(statuses)) {
                predicate = criteriaBuilder.and(
                    predicate,
                    root.get("status").in(statuses)
                );
            }
            return predicate;
        };
    }
}
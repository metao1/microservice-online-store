package com.metao.book.order.application.cart;

import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
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

            // For productIds, we need to join with OrderItemEntity
            if (!CollectionUtils.isEmpty(productIds)) {
                var itemsJoin = root.join("items");
                var productIdPath = itemsJoin.get("productId").get("value");
                predicate = criteriaBuilder.and(predicate, productIdPath.in(productIds));
            }

            // For status, we can use the direct field
            predicate = addPredicateForField(criteriaBuilder, predicate, root.get("status"), statuses);
            return predicate;
        };
    }

    private Predicate addPredicateForField(
        CriteriaBuilder criteriaBuilder,
        Predicate predicate,
        Path<Object> path,
        Set<?> values
    ) {
        if (!CollectionUtils.isEmpty(values)) {
            return criteriaBuilder.and(predicate, path.in(values));
        }
        return predicate;
    }
}
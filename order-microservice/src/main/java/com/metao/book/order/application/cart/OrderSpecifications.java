package com.metao.book.order.application.cart;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
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
            predicate = addPredicateForField(criteriaBuilder, predicate, root.get("productId"), productIds);
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
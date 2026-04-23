package com.metao.book.order.application.cart;

import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class OrderSpecifications {

    public static Specification<OrderJpaEntity> findByOrdersByCriteria(
        Set<String> skus, Set<OrderStatus> statuses
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (!CollectionUtils.isEmpty(skus)) {
                var itemsJoin = root.join("items");
                var skuPath = itemsJoin.get("productSku").get("value");
                predicate = criteriaBuilder.and(predicate, skuPath.in(skus));
            }

            predicate = addPredicateForField(criteriaBuilder, predicate, root.get("status"), statuses);
            return predicate;
    public static Specification<OrderJpaEntity> findOrdersByCriteria(
    }

    private static Predicate addPredicateForField(
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

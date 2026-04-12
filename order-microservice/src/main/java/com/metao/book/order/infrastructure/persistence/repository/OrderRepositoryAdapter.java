package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.entity.OrderItemEntity;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.metao.book.order.infrastructure.persistence.mapper.OrderEntityMapper;
import com.metao.book.shared.application.persistence.OffsetBasedPageRequest;
import io.micrometer.observation.annotation.Observed;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Observed(name = "order.persistence.repository", contextualName = "order-repository")
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public void save(OrderAggregate order) {
        OrderJpaEntity entity = OrderEntityMapper.toEntity(order);
        springDataOrderRepository.findById(order.getId().value())
            .ifPresent(existing -> {
                entity.setVersion(existing.getVersion());

                var existingItemIdsBySku = new HashMap<String, Long>();
                for (OrderItemEntity item : existing.getItems()) {
                    existingItemIdsBySku.put(item.getProductSku().value(), item.getId());
                }

                entity.getItems().forEach(item -> {
                    Long existingItemId = existingItemIdsBySku.get(item.getProductSku().value());
                    if (existingItemId != null) {
                        item.setId(existingItemId);
                    }
                });
            });
        springDataOrderRepository.save(entity);
    }

    @Override
    public Optional<OrderAggregate> findById(OrderId orderId) {
        return springDataOrderRepository.findById(orderId.value())
            .map(OrderEntityMapper::toDomain);
    }

    @Override
    public Optional<OrderAggregate> findByIdForUpdate(OrderId orderId) {
        return springDataOrderRepository.findByIdForUpdate(orderId.value())
            .map(OrderEntityMapper::toDomain);
    }

    @Override
    public List<OrderAggregate> findByUserId(UserId userId) {
        return springDataOrderRepository.findByUserId(userId).stream()
            .map(OrderEntityMapper::toDomain)
            .toList();
    }

    @Override
    public Page<OrderAggregate> findByUserId(UserId userId, int offset, int limit) {
        Page<String> orderIdPage = springDataOrderRepository.findIdsByUserIdOrderByCreatedAtDesc(
            userId,
            new OffsetBasedPageRequest(offset, limit)
        );

        List<String> orderIds = orderIdPage.getContent();
        if (orderIds.isEmpty()) {
            return new PageImpl<>(List.of(), orderIdPage.getPageable(), orderIdPage.getTotalElements());
        }

        var orderIndexById = new HashMap<String, Integer>();
        for (int index = 0; index < orderIds.size(); index += 1) {
            orderIndexById.put(orderIds.get(index), index);
        }

        List<OrderAggregate> orders = springDataOrderRepository.findAllByIdInWithItems(orderIds).stream()
            .sorted(Comparator.comparingInt(order -> orderIndexById.getOrDefault(order.getId(), Integer.MAX_VALUE)))
            .map(OrderEntityMapper::toDomain)
            .toList();

        return new PageImpl<>(orders, orderIdPage.getPageable(), orderIdPage.getTotalElements());
    }

    @Override
    public void delete(OrderId orderId) {
        throw new UnsupportedOperationException("Delete operation is not supported for orders.");
    }
}

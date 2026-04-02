package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.entity.OrderItemEntity;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.metao.book.order.infrastructure.persistence.mapper.OrderEntityMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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
    public void delete(OrderId orderId) {
        throw new UnsupportedOperationException("Delete operation is not supported for orders.");
    }
}

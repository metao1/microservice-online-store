package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.aggregate.Order;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.entity.OrderEntity;
import com.metao.book.order.infrastructure.persistence.mapper.OrderEntityMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity savedEntity = jpaOrderRepository.save(entity);
        return OrderEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaOrderRepository.findById(orderId.value())
            .map(OrderEntityMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaOrderRepository.findByCustomerId(customerId).stream()
            .map(OrderEntityMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(OrderId orderId) {
        jpaOrderRepository.deleteById(orderId.value());
    }

}
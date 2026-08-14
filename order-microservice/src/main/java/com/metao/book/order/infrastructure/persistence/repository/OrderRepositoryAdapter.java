package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.application.port.OrderPort;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.application.port.OrderRepository;
import com.metao.book.order.infrastructure.persistence.entity.OrderItemEntity;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.metao.book.order.infrastructure.persistence.mapper.OrderEntityMapper;
import com.metao.book.shared.spring.persistence.OffsetBasedPageRequest;
import com.metao.book.shared.domain.financial.VAT;
import com.metao.book.shared.architecture.OutboundAdapter;
import io.micrometer.observation.annotation.Observed;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@OutboundAdapter
@RequiredArgsConstructor
@Observed(name = "order.persistence.repository", contextualName = "order-repository")
public class OrderRepositoryAdapter implements OrderRepository, OrderPort {

    private final SpringDataOrderRepository springDataOrderRepository;
    private final VAT vat;

    @Override
    public void save(OrderAggregate order) {
        Optional<OrderJpaEntity> existingOrder = springDataOrderRepository.findById(order.getId().value());
        if (existingOrder.isEmpty()) {
            springDataOrderRepository.save(OrderEntityMapper.toEntity(order));
            return;
        }

        updateManagedEntity(existingOrder.orElseThrow(), order);
    }

    private void updateManagedEntity(OrderJpaEntity target, OrderAggregate order) {
        OrderJpaEntity mapped = OrderEntityMapper.toEntity(order);
        target.setUserId(mapped.getUserId());
        target.setStatus(mapped.getStatus());
        target.setCreatedAt(mapped.getCreatedAt());
        target.setUpdatedAt(mapped.getUpdatedAt());
        target.setSubtotalAmount(mapped.getSubtotalAmount());
        target.setTaxAmount(mapped.getTaxAmount());
        target.setTotalAmount(mapped.getTotalAmount());
        target.setFinancialCurrency(mapped.getFinancialCurrency());
        target.setVatRate(mapped.getVatRate());

        Map<String, OrderItemEntity> existingBySku = new HashMap<>();
        target.getItems().forEach(item -> existingBySku.put(item.getProductSku().value(), item));
        target.getItems().removeIf(item -> !mapped.getItems().stream()
            .anyMatch(newItem -> newItem.getProductSku().value().equals(item.getProductSku().value())));

        for (OrderItemEntity mappedItem : mapped.getItems()) {
            OrderItemEntity item = existingBySku.get(mappedItem.getProductSku().value());
            if (item == null) {
                mappedItem.setOrder(target);
                target.getItems().add(mappedItem);
                continue;
            }
            item.setProductSku(mappedItem.getProductSku());
            item.setProductTitle(mappedItem.getProductTitle());
            item.setQuantity(mappedItem.getQuantity());
            item.setUnitPrice(mappedItem.getUnitPrice());
            item.setOrder(target);
        }
    }

    @Override
    public Optional<OrderAggregate> findById(OrderId orderId) {
        return springDataOrderRepository.findById(orderId.value())
            .map(entity -> OrderEntityMapper.toDomain(entity, vat));
    }

    @Override
    public Optional<OrderAggregate> findByIdForUpdate(OrderId orderId) {
        return springDataOrderRepository.findByIdForUpdate(orderId.value())
            .map(entity -> OrderEntityMapper.toDomain(entity, vat));
    }

    @Override
    public List<OrderAggregate> findByUserId(UserId userId) {
        return springDataOrderRepository.findByUserId(userId).stream()
            .map(entity -> OrderEntityMapper.toDomain(entity, vat))
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
            .map(entity -> OrderEntityMapper.toDomain(entity, vat))
            .toList();

        return new PageImpl<>(orders, orderIdPage.getPageable(), orderIdPage.getTotalElements());
    }

    @Override
    public void delete(OrderId orderId) {
        throw new UnsupportedOperationException("Delete operation is not supported for orders.");
    }
}

package com.metao.book.product.application.service;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.infrastructure.factory.handler.RemoteOrderService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagerService {

    private final ProductRepository productRepository;
    private final RemoteOrderService remoteOrderService;

    private static boolean availableInStock(OrderEvent order, ProductEntity productEntity) {
        return productEntity.getVolume()
                .subtract(productEntity.getReservedItems())
                .subtract(BigDecimal.valueOf(order.getQuantity())).doubleValue() >= 0;
    }

    public void reserve(OrderEvent order) {
        var productEntity = productRepository.findByAsin(order.getProductId()).orElseThrow();
        if (Objects.requireNonNull(order.getStatus()) == Status.NEW) {
            if (availableInStock(order, productEntity)) {
                acceptOrder(order, productEntity);
            } else {
                order.setStatus(Status.REJECT);
            }
        }
        try {
            productRepository.save(productEntity);
            remoteOrderService.handle(order);
        } catch (Exception ex) {
            log.error("Could not save product entity, {}", ex.getMessage());
        }
    }

    public void confirm(OrderEvent order) {
        var productEntity = productRepository.findByAsin(order.getProductId()).orElseThrow();
        switch (order.getStatus()) {
            case CONFIRM -> confirmOrder(order, productEntity);
            case ROLLBACK -> rejectOrder(order, productEntity);
            default -> throw new RuntimeException("status is not defined");
        }
    }

    void rollbackOrder(OrderEvent order, ProductEntity productEntity) {
        var volume = productEntity.getVolume();
        var reserved = productEntity.getReservedItems();
        var quantity = BigDecimal.valueOf(order.getQuantity());
        productEntity.setReservedItems(reserved.add(quantity));
        productEntity.setVolume(volume.add(quantity));
    }

    void rejectOrder(OrderEvent order, ProductEntity productEntity) {
        rollbackOrder(order, productEntity);
    }

    void acceptOrder(OrderEvent order, ProductEntity productEntity) {
        var reserved = productEntity.getReservedItems();
        var quantity = BigDecimal.valueOf(order.getQuantity());
        productEntity.setReservedItems(reserved.add(quantity));
        order.setStatus(Status.ACCEPT);
    }

    public void confirmOrder(OrderEvent order, ProductEntity productEntity) {
        var volume = productEntity.getVolume();
        var quantity = BigDecimal.valueOf(order.getQuantity());
        productEntity.setVolume(volume.add(quantity));
        order.setStatus(Status.CONFIRM);
    }
}

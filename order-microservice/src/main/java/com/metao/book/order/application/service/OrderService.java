package com.metao.book.order.application.service;

import java.util.List;
import java.util.Optional;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;

import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

        private final KafkaTemplate<OrderId, OrderEntity> kafkaTemplate;
        private final KafkaOrderService kafkaOrderService;

        @Value("${kafka.stream.topic.order}")
        private String orderTopic;

        @Override
        public void saveOrder(OrderEntity orderEntity) {
                kafkaTemplate.send(orderTopic, orderEntity.id(), orderEntity);
        }

        @Override
        public Optional<OrderEntity> getOrderByProductId(ProductId productId) {
                return kafkaOrderService.getOrder(new OrderId(productId.toString()));
        }

        @Override
        public Optional<List<OrderEntity>> getAllOrdersPageble(int from, int to) {
                throw new NotYetImplementedException();
        }

}

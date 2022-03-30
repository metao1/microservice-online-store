package com.metao.book.order.application.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.infrastructure.KafkaListenableCallback;
import com.metao.book.order.infrastructure.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

        private final KafkaTemplate<OrderId, OrderEntity> kafkaTemplate;
        private final OrderRepository orderRepository;

        @Value("${stream.topic.order}")
        private String orderTopic;

        @Override

        public void saveOrder(OrderEntity orderEntity) {
                kafkaTemplate.send(orderTopic, orderEntity.id(), orderEntity);
        }

        @Override
        public Optional<OrderEntity> getOrderByProductId(OrderId orderId) {
                return orderRepository.getOrder(orderId);
        }

        @Override
        public Optional<List<OrderEntity>> getAllOrdersPageble(int from, int to) {
                return orderRepository.getOrders(from, to);
        }

}

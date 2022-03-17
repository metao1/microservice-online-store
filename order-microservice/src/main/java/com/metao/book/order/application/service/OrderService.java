package com.metao.book.order.application.service;

import java.util.List;
import java.util.Optional;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderRepository;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.infrastructure.KafkaListenableCallback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService implements OrderServiceInterface {

        private KafkaTemplate<OrderId, OrderEntity> kafkaTemplate;
        private OrderRepository orderRepository;        
        private KafkaListenableCallback<OrderId, OrderEntity> kafkaListenerHandler;
        
        @Value("${stream.topic.order}")
        private String orderTopic;

        @Override
        public void saveOrder(OrderEntity orderEntity) {
                kafkaTemplate.send(orderTopic, orderEntity.id(), orderEntity).addCallback(kafkaListenerHandler);                
        }

        @Override
        public Optional<OrderEntity> getOrderByProductId(ProductId productId) {
                return orderRepository.getOrderByProductId(productId);
        }

        @Override
        public Optional<List<OrderEntity>> getAllOrdersPageble(int limit, int offset) {                
                return null;
        }
        
}

package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.OrderMapperInterface;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
        
        private OrderServiceInterface orderService;
        private OrderMapperInterface mapper;

        @PostMapping
        public ResponseEntity<OrderId> createOrder(OrderDTO orderDto){
                return orderService.getOrderByProductId(orderDto.getProductId())
                        .map(OrderEntity::id)                        
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        }


}

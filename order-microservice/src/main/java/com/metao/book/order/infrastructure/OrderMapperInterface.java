package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.OrderDTO;
import com.order.microservice.avro.Currency;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Status;

public interface OrderMapperInterface {

        default OrderAvro toAvro(OrderDTO dto) {
                return OrderAvro.newBuilder()
                                .setStatus(Status.NEW)
                                .setCurrency(Currency.dlr)
                                .setPrice(dto.getPrice())
                                .setQuantity(dto.getQuantity())
                                .setProductId(dto.getProductId())
                                .setCustomerId(dto.getCustomerId())
                                .setOrderId(dto.getOrderId())                                
                                .build();
        }

        default OrderDTO toDto(OrderAvro order) {
                return new OrderDTO(order.getOrderId(),
                                order.getProductId(),
                                order.getCustomerId(),
                                com.metao.book.order.domain.Status.NEW,
                                order.getQuantity(),
                                com.metao.book.order.domain.Currency.DLR,
                                order.getPrice());
        }

        default Status convertToStatus(Status status) {
                return Status.valueOf(status.toString());
        }

        default Currency convertToCurrency(Currency currency) {
                return Currency.valueOf(currency.toString());
        }
}

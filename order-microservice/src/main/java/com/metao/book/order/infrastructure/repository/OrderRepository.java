package com.metao.book.order.infrastructure.repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderRepository {

        private static final String ORDERS = "orders";

        private final StreamsBuilderFactoryBean kafkaStreamsFactory;

        public Optional<OrderEntity> getOrder(@NotNull OrderId orderId) {
                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
                if (kStream == null) {
                        return Optional.empty();
                }
                var store = kStream.<ReadOnlyKeyValueStore<OrderId, OrderEntity>>store(
                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
                return Optional.ofNullable(store.get(orderId));
        }

        public List<OrderEntity> getOrders(@NotNull OrderId from, @NotNull OrderId to) {
                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
                if (kStream == null) {
                        return Collections.emptyList();
                }
                var store = kStream.<ReadOnlyKeyValueStore<OrderId, OrderEntity>>store(
                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
                KeyValueIterator<OrderId, OrderEntity> iterator = store.range(from, to);
                List<OrderEntity> orders = new LinkedList<>();
                while (iterator.hasNext()) {
                        orders.add(iterator.next().value);
                }
                return orders;
        }
}

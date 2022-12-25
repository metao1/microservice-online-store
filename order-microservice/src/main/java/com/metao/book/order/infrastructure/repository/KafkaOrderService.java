package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.infrastructure.OrderMapperInterface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private static final String ORDERS = "ORDERS";
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final OrderMapperInterface conversionService;

    public Optional<OrderDTO> getOrder(@NotNull String orderId) {
        KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
        if (kStream == null) {
            return Optional.empty();
        }

        var store = kStream.<ReadOnlyKeyValueStore<String, OrderEvent>>store(
                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
        return Optional.ofNullable(store.get(orderId))
                .map(val -> Objects.requireNonNull(conversionService.toDto(val)));
    }

    public List<OrderEvent> getOrders(@NotNull String from, @NotNull String to) {
        KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
        if (kStream == null) {
            return Collections.emptyList();
        }
        var store = kStream.<ReadOnlyKeyValueStore<String, OrderEvent>>store(
            StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
        KeyValueIterator<String, OrderEvent> iterator = store.range(from, to);
        List<OrderEvent> orders = new LinkedList<>();
        while (iterator.hasNext()) {
            orders.add(iterator.next().value);
        }
        return orders;

    }
}

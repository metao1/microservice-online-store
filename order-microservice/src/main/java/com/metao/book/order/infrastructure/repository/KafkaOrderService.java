package com.metao.book.order.infrastructure.repository;

import com.metao.book.shared.OrderAvro;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private static final String ORDERS = "order";

    //private final StreamsBuilderFactoryBean kafkaStreamsFactory;

    public Optional<OrderAvro> getOrder(@NotNull String orderId) {
//                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
//                if (kStream == null) {
//                        return Optional.empty();
//                }
//                var store = kStream.<ReadOnlyKeyValueStore<String, OrderAvro>>store(
//                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
//                return Optional.ofNullable(store.get(orderId));
        return Optional.empty();
    }

    public List<OrderAvro> getOrders(@NotNull String from, @NotNull String to) {
//                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
//                if (kStream == null) {
//                        return Collections.emptyList();
//                }
//                var store = kStream.<ReadOnlyKeyValueStore<String, OrderAvro>>store(
//                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
//                KeyValueIterator<String, OrderAvro> iterator = store.range(from, to);
//                List<OrderAvro> orders = new LinkedList<>();
//                while (iterator.hasNext()) {
//                        orders.add(iterator.next().value);
//                }
//                return orders;
        return Collections.emptyList();
    }
}

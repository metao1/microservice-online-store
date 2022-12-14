package com.metao.book.order.infrastructure.repository;

import com.metao.book.shared.OrderEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private static final String ORDERS = "order";

    //private final StreamsBuilderFactoryBean kafkaStreamsFactory;

    public Optional<OrderEvent> getOrder(@NotNull String orderId) {
//                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
//                if (kStream == null) {
//                        return Optional.empty();
//                }
//                var store = kStream.<ReadOnlyKeyValueStore<String, OrderEvent>>store(
//                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
//                return Optional.ofNullable(store.get(orderId));
        return Optional.empty();
    }

    public List<OrderEvent> getOrders(@NotNull String from, @NotNull String to) {
//                KafkaStreams kStream = kafkaStreamsFactory.getKafkaStreams();
//                if (kStream == null) {
//                        return Collections.emptyList();
//                }
//                var store = kStream.<ReadOnlyKeyValueStore<String, OrderEvent>>store(
//                                StoreQueryParameters.fromNameAndType(ORDERS, QueryableStoreTypes.keyValueStore()));
//                KeyValueIterator<String, OrderEvent> iterator = store.range(from, to);
//                List<OrderEvent> orders = new LinkedList<>();
//                while (iterator.hasNext()) {
//                        orders.add(iterator.next().value);
//                }
//                return orders;
        return Collections.emptyList();
    }
}

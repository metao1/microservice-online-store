package com.metao.book.order.infrastructure.repository;

import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final OrderServiceInterface orderService;
    private final OrderMapperInterface mapper;

    @GetMapping
    public ResponseEntity<OrderDTO> getOrderByProductId(@RequestParam(name = "product_id", value = "product_id") String productId) {
        return orderService
                .getOrderByProductId(productId)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@Valid @RequestBody OrderDTO orderDto) {
        return Optional.of(orderDto)
                .map(mapper::toAvro)
                .stream()
                .<OrderAvro>mapMulti((order, stream) -> {
                    if (order != null) {
                        stream.accept(order);
                    }
                })
                .peek(orderService::saveOrder)
                .map(OrderAvro::getOrderId)
                .map(ResponseEntity::ok)
                .findAny()
                .orElseThrow(() -> new RuntimeException("could not crate order"));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<OrderDTO>> allOrders(@Valid @RequestParam("limit") Integer limit,
                                                    @Valid @RequestParam("offset") Integer offset) {
        var list = new LinkedList<OrderDTO>();
        kafkaStreamsFactory.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType("order", QueryableStoreTypes.keyValueStore()))
                .all()
                .forEachRemaining(kv -> {
                    var orderAvro = (OrderAvro) kv.value;
                    var orderDto = mapper.toDto(orderAvro);
                    list.add(orderDto);
                });
        return ResponseEntity.ok().body(list);
    }
//
//    @GetMapping("/counts")
//    public Map<Long, Set<String>> count() {
//        ReadOnlyKeyValueStore<String, Long> queryableStore = interactiveQueryService
//                .getQueryableStore("order", QueryableStoreTypes.keyValueStore());
//        KeyValueIterator<String, Long> allKeyValues = queryableStore.all();
//        Map<String, Long> allKeyValueMaps = new ConcurrentHashMap<>();
//        while (allKeyValues != null && allKeyValues.hasNext()) {
//            KeyValue<String, Long> next = allKeyValues.next();
//            allKeyValueMaps.put(next.key, next.value);
//        }
//
//        return allKeyValueMaps.entrySet()
//                .parallelStream()
//                .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new,
//                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
//    }
}

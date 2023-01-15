package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderServiceInterface orderService;

    @GetMapping
    public ResponseEntity<OrderDTO> getOrderByOrderId(
        @RequestParam(name = "order_id", value = "order_id") String orderId
    ) {
        return orderService
            .getOrderByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@Valid @RequestBody OrderDTO orderDto) {
        return orderService.createOrder(orderDto)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new RuntimeException("could not create order"));
    }

    //
    // @GetMapping("/counts")
    // public Map<Long, Set<String>> count() {
    // ReadOnlyKeyValueStore<String, Long> queryableStore = interactiveQueryService
    // .getQueryableStore("order", QueryableStoreTypes.keyValueStore());
    // KeyValueIterator<String, Long> allKeyValues = queryableStore.all();
    // Map<String, Long> allKeyValueMaps = new ConcurrentHashMap<>();
    // while (allKeyValues != null && allKeyValues.hasNext()) {
    // KeyValue<String, Long> next = allKeyValues.next();
    // allKeyValueMaps.put(next.key, next.value);
    // }
    //
    // return allKeyValueMaps.entrySet()
    // .parallelStream()
    // .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new,
    // Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
    // }
}

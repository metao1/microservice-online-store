package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

        private final OrderServiceInterface orderService;
        private final OrderMapperInterface mapper;
        //private final StreamsBuilderFactoryBean kafkaStreamsFactory;

        @GetMapping
        public ResponseEntity<OrderDTO> getOrderByProductId(@RequestParam(name="product_id", value = "product_id") String productId) {
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

        @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<OrderDTO>> allOrders(@Valid @RequestParam("limit") Integer limit,
                                                        @Valid @RequestParam("offset") Integer offset) {
                var list = new LinkedList<OrderDTO>();
//                kafkaStreamsFactory.getKafkaStreams()
//                        .store(StoreQueryParameters.fromNameAndType("output",
//                                QueryableStoreTypes.keyValueStore()))
//                        .range(offset, limit + offset)
//                        .forEachRemaining(kv -> list.add(((OrderDTO) kv.value)));
                return ResponseEntity.ok().body(list);
        }
}

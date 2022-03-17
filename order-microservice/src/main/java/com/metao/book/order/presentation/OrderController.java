package com.metao.book.order.presentation;

import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.ProductId;
import com.metao.book.order.infrastructure.OrderMapperInterface;

import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

        private final OrderServiceInterface orderService;
        private final OrderMapperInterface mapper;
        private final StreamsBuilderFactoryBean kafkaStreamsFactory;

        @ResponseBody
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<OrderDTO> getOrderByProductId(@RequestParam("product_id") ProductId productId) {
                return orderService.getOrderByProductId(productId)
                                .map(mapper::toDto)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<OrderId> createOrder(@Valid OrderDTO orderDto) {
                Optional.ofNullable(orderDto)
                                .map(mapper::toEntity)
                                .ifPresentOrElse(orderService::saveOrder, () -> {
                                        throw new RuntimeException("can't save order");
                                });

                return ResponseEntity.ok().body(orderDto.getOrderId());
        }

        @ResponseBody
        @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<OrderDTO>> allOrders(@Valid @RequestParam("limit") Integer limit,
                        @Valid @RequestParam("offset") Integer offset) {
                var list = new LinkedList<OrderDTO>();
                // Flux<OrderDTO> flux = Flux.<OrderDTO>create(sink -> {
                kafkaStreamsFactory.getKafkaStreams()
                                .store(StoreQueryParameters.fromNameAndType("output",
                                                QueryableStoreTypes.keyValueStore()))
                                .range(offset, limit + offset)
                                .forEachRemaining(kv -> list.add(((OrderDTO) kv.value)));
                // });
                return ResponseEntity.ok().body(list);
        }
}

package com.metao.book.cart.service.config;

import com.metao.book.cart.service.ShoppingCartService;
import com.metao.book.cart.service.mapper.CartMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableKafka
@Profile({"!test"})
@RequiredArgsConstructor
@ImportAutoConfiguration(value = {KafkaSerdesConfig.class, OrderEventValidator.class})
public class KafkaCardListener {

    private final ShoppingCartService shoppingCartService;
    private final CartMapperService.ToShoppingCartEntity shoppingCartMapper;
    private final OrderEventValidator orderEventValidator;

    @Transactional(KAFKA_TRANSACTION_MANAGER)
    @KafkaListener(id = "${kafka.topic.order}",
        topics = "${kafka.topic.order}",
        groupId = "cart-order-grp"
    )
    public void onOrderListener(ConsumerRecord<String, OrderEvent> record) {
        orderEventValidator.validate(record.value());
        log.info("Received order event: {}", record.value());
        final var shoppingCart = shoppingCartMapper.mapToShoppingCart(record.value());
        shoppingCartService.addOrderToShoppingCart(shoppingCart);
    }
}

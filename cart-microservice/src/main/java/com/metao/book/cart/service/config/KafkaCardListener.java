package com.metao.book.cart.service.config;

import static com.metao.book.shared.kafka.Constants.KAFKA_TRANSACTION_MANAGER;

import com.metao.book.cart.service.ShoppingCartService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.kafka.StreamCustomExceptionHandlerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableKafka
@Profile({"!test"})
@RequiredArgsConstructor
@Transactional(KAFKA_TRANSACTION_MANAGER)
@ImportAutoConfiguration(value = {KafkaSerdesConfig.class})
@ComponentScan(basePackageClasses = StreamCustomExceptionHandlerConfig.class)
public class KafkaCardListener {

    private final ShoppingCartService shoppingCartService;

    @KafkaListener(id = "${kafka.topic.order}",
        topics = "${kafka.topic.order}",
        groupId = "${kafka.topic.order}" + "-grp"
    )
    void onOrder(ConsumerRecord<String, OrderEvent> record) {
        var order = record.value();
        var userId = order.getCustomerId();
        var asin = order.getProductId();
        shoppingCartService.addProductToShoppingCart(userId, asin);
    }
}


package com.metao.book.cart.service;

import static com.metao.book.shared.kafka.Constants.KAFKA_TRANSACTION_MANAGER;
import static com.metao.book.shared.kafka.Constants.TRANSACTION_MANAGER;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.repository.ShoppingCartRepository;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.application.service.order.OrderEventValidator;
import com.metao.book.shared.kafka.RemoteKafkaService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(TRANSACTION_MANAGER)
@ComponentScan(basePackageClasses = RemoteKafkaService.class)
public class ShoppingCartFactory implements ShoppingCartService {

    private final RemoteKafkaService<String, OrderEvent> orderTemplate;
    private final CartMapperService.ToEventMapper cartMapper;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderEventValidator orderEventValidator;
    private final NewTopic orderTopic;

    @Override
    public void addOrderToShoppingCart(ShoppingCart shoppingCart) {
        log.info("Adding product: " + shoppingCart.getAsin());
        ShoppingCartKey currentKey = new ShoppingCartKey(shoppingCart.getUserId(), shoppingCart.getAsin());
        var shoppingCartOptional = shoppingCartRepository.findById(currentKey);
        shoppingCartOptional.ifPresent(sc -> shoppingCart.increaseQuantity());
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public void updateOrdersInCart(ShoppingCart shopping) {

    }

    @Override
    public Set<ShoppingCart> getProductsInCartByUserId(String userId) {
        return shoppingCartRepository.findProductsInCartByUserId(userId);
    }

    @Override
    public void removeProductFromCart(String userId, String asin) {
        ShoppingCartKey currentKey = new ShoppingCartKey(userId, asin);
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(currentKey);
        shoppingCartOptional.ifPresent(shoppingCart -> {
            if (shoppingCart.getQuantity().compareTo(BigDecimal.ONE) > 0) {
                shoppingCart.decreaseQuantity();
                shoppingCartRepository.save(shoppingCart);
                log.info("Decremented product: " + asin);
            } else {
                shoppingCartRepository.delete(shoppingCart);
                log.info("Deleted product: " + asin);
            }
        });
    }

    @Override
    public int clearCart(String userId) {
        var productsInCartByUserId = shoppingCartRepository.findProductsInCartByUserId(userId);
        if (productsInCartByUserId != null) {
            shoppingCartRepository.deleteAll(productsInCartByUserId);
            log.info("Clearing all products in cart for user: {} was successful.", userId);
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    @Transactional(value = KAFKA_TRANSACTION_MANAGER, propagation = Propagation.REQUIRED)
    public boolean submitProducts(String userId) {
        var products = shoppingCartRepository.findProductsInCartByUserId(userId);
        if (CollectionUtils.isEmpty(products)) {
            return false;
        }
        return products.stream()
            .map(cartMapper::mapToOrderEvent)
            .filter(order -> Status.NEW.equals(order.getStatus()))
            .sorted((p1, p2) -> Math.toIntExact(p1.getCreatedOn() - p2.getCreatedOn()))
            .peek(orderEventValidator::validate)
            .filter(orderEvent -> Status.NEW.equals(orderEvent.getStatus()))
            .peek(orderEvent -> orderTemplate.sendToTopic(orderTopic.name(), orderEvent.getProductId(), orderEvent))
            .peek(orderEvent -> orderEvent.setStatus(Status.SUBMITTED))
            .reduce((o1, o2) -> o2)
            .isPresent();
    }
}

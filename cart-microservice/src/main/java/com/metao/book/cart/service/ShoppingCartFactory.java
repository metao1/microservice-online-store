
package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.domain.dto.ShoppingCartDto;
import com.metao.book.cart.repository.ShoppingCartRepository;
import com.metao.book.cart.service.mapper.CartMapperService;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.kafka.RemoteKafkaService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@ComponentScan(basePackageClasses = RemoteKafkaService.class)
public class ShoppingCartFactory implements ShoppingCartService {

    private final RemoteKafkaService<String, OrderEvent> orderTemplate;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartMapperService cartMapper;
    private final NewTopic orderTopic;

    @Override
    public void addProductToShoppingCart(String userId, String asin) {
        ShoppingCartKey currentKey = new ShoppingCartKey(userId, asin);
        shoppingCartRepository.findById(currentKey)
            .ifPresentOrElse(
                shoppingCart -> updateProduct(asin, shoppingCart),
                () -> createProduct(asin, currentKey)
            );
    }

    @Transactional
    void createProduct(String asin, ShoppingCartKey currentKey) {
        log.info("Adding product: " + asin);
        ShoppingCart shoppingCart = ShoppingCart.createCart(currentKey);
        shoppingCartRepository.save(shoppingCart);
    }

    @Transactional
    void updateProduct(String asin, ShoppingCart shoppingCart) {
        log.info("Updating product: " + asin);
        shoppingCart.increaseQuantity();
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public Map<String, List<ShoppingCartDto>> getProductsInCartByUserId(String userId) {
        return shoppingCartRepository.findProductsInCartByUserId(userId)
            .stream()
            .map(cartMapper::mapToShoppingCartDto)
            .collect(Collectors.groupingBy(ShoppingCartDto::getAsin));
    }

    @Override
    @Transactional
    public void removeProductFromCart(String userId, String asin) {
        ShoppingCartKey currentKey = new ShoppingCartKey(userId, asin);
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(currentKey);
        shoppingCartOptional.ifPresent(shoppingCart -> {
            if (shoppingCart.getQuantity() > 1) {
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
    @Transactional
    public String submitProducts(String userId) {
        var products = shoppingCartRepository.findProductsInCartByUserId(userId);
        if (CollectionUtils.isEmpty(products)) {
            return "cart is empty";
        } else {
            products.stream()
                .map(cartMapper::mapToOrderEvent)
                .sorted((p1, p2) -> (int) (p1.getCreatedOn() - p2.getCreatedOn()))
                .forEachOrdered(product ->
                    orderTemplate.sendToTopic(orderTopic.name(), product.getProductId(), product));
            return userId;
        }
    }
}

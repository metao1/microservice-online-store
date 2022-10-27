package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCartCartFactory implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    public void addProductToShoppingCart(String userId, String asin) {
        ShoppingCartKey currentKey = new ShoppingCartKey(userId, asin);
        shoppingCartRepository.findById(currentKey)
                .ifPresentOrElse(shoppingCart -> updateProduct(asin, shoppingCart), () -> createProduct(asin, currentKey));
    }

    private void createProduct(String asin, ShoppingCartKey currentKey) {
        log.info("Adding product: " + asin);
        ShoppingCart shoppingCart = ShoppingCart.createCart(currentKey);
        shoppingCartRepository.save(shoppingCart);
    }

    private void updateProduct(String asin, ShoppingCart shoppingCart) {
        log.info("Updating product: " + asin);
        shoppingCart.increaseQuantity();
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public Map<String, List<ShoppingCart>> getProductsInCartByUserId(String userId) {
        var productsInCartAsin = new HashMap<String, List<ShoppingCart>>();
        var items = shoppingCartRepository.findProductsInCartByUserId(userId);
        productsInCartAsin.putIfAbsent(userId, items);
        return productsInCartAsin;
    }

    @Override
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
        if (shoppingCartRepository.findProductsInCartByUserId(userId) != null) {
            int deletedRow = shoppingCartRepository.findProductsInCartByUserId(userId).size();
            log.info("Deleted all products for user: {} with code: {} since checkout was successful.", userId,
                    deletedRow);
            return 0;
        } else {
            return -1;
        }
    }
}

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
        String ShoppingCartKeyStr = userId + "-" + asin;
        if (shoppingCartRepository.findById(ShoppingCartKeyStr).isPresent()) {
            shoppingCartRepository.updateQuantityForShoppingCart(userId, asin);
            log.info("Updating product: " + asin);
        } else {
            ShoppingCart currentShoppingCart = createCartObject(currentKey);
            shoppingCartRepository.save(currentShoppingCart);
            log.info("Adding product: " + asin);
        }
    }

    @Override
    public Map<String, Integer> getProductsInCart(String userId) {
        Map<String, Integer> productsInCartAsin = new HashMap<>();
        if (shoppingCartRepository.findProductsInCartByUserId(userId).isPresent()) {
            List<ShoppingCart> productsInCart = shoppingCartRepository.findProductsInCartByUserId(userId).get();
            for (ShoppingCart item : productsInCart) {
                productsInCartAsin.put(item.getAsin(), item.getQuantity());
            }
        }
        return productsInCartAsin;
    }

    @Override
    public void removeProductFromCart(String userId, String asin) {
        String shoppingCartKeyStr = userId + "-" + asin;
        if (shoppingCartRepository.findById(shoppingCartKeyStr).isPresent()) {
            if (shoppingCartRepository.findById(shoppingCartKeyStr).get().getQuantity() > 1) {
                shoppingCartRepository.decrementQuantityForShoppingCart(userId, asin);
                log.info("Decrementing product: " + asin + " quantity");
            } else if (shoppingCartRepository.findById(shoppingCartKeyStr).get().getQuantity() == 1) {
                shoppingCartRepository.deleteById(shoppingCartKeyStr);
                log.info("Removing product: " + asin + " since it was qty 1");
            }
        }
    }

    @Override
    public int clearCart(String userId) {
        if (shoppingCartRepository.findProductsInCartByUserId(userId).isPresent()) {
            int deletedRow = shoppingCartRepository.deleteProductsInCartByUserId(userId);
            log.info("Deleted all products for user: {} with code: {} since checkout was successful.", userId,
                    deletedRow);
            return 0;
        } else {
            return -1;
        }
    }
}

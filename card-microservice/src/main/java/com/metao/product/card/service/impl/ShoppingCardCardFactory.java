package com.metao.product.card.service.impl;

import com.metao.product.card.domain.ShoppingCard;
import com.metao.product.card.domain.ShoppingCardKey;
import com.metao.product.card.repository.ShoppingCardRepository;
import com.metao.product.card.service.ShoppingCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShoppingCardCardFactory implements ShoppingCardService {

    private final ShoppingCardRepository shoppingCardRepository;

    @Override
    public void addProductToShoppingCard(String userId, String asin) {
        ShoppingCardKey currentKey = new ShoppingCardKey(userId, asin);
        String ShoppingCardKeyStr = userId + "-" + asin;
        if (shoppingCardRepository.findById(ShoppingCardKeyStr).isPresent()) {
            shoppingCardRepository.updateQuantityForShoppingCard(userId, asin);
            System.out.println("Adding product: " + asin);
        } else {
            ShoppingCard currentShoppingCard = createCardObject(currentKey);
            shoppingCardRepository.save(currentShoppingCard);
            System.out.println("Adding product: " + asin);
        }
    }

    @Override
    public Map<String, Integer> getProductsInCard(String userId) {
        Map<String, Integer> productsInCartAsin = new HashMap<>();
        if (shoppingCardRepository.findProductsInCardByUserId(userId).isPresent()) {
            List<ShoppingCard> productsInCart = shoppingCardRepository.findProductsInCardByUserId(userId).get();
            for (ShoppingCard item : productsInCart) {
                productsInCartAsin.put(item.getAsin(), item.getQuantity());
            }
        }
        return productsInCartAsin;
    }

    @Override
    public void removeProductFromCard(String userId, String asin) {
        String shoppingCardKeyStr = userId + "-" + asin;
        if (shoppingCardRepository.findById(shoppingCardKeyStr).isPresent()) {
            if (shoppingCardRepository.findById(shoppingCardKeyStr).get().getQuantity() > 1) {
                shoppingCardRepository.decrementQuantityForShoppingCard(userId, asin);
                System.out.println("Decrementing product: " + asin + " quantity");
            } else if (shoppingCardRepository.findById(shoppingCardKeyStr).get().getQuantity() == 1) {
                shoppingCardRepository.deleteById(shoppingCardKeyStr);
                System.out.println("Removing product: " + asin + " since it was qty 1");
            }
        }
    }

    @Override
    public void clearCard(String userId) {
        if (shoppingCardRepository.findProductsInCardByUserId(userId).isPresent()) {
            shoppingCardRepository.deleteProductsInCardByUserId(userId);
            System.out.println("Deleting all products for user: " + userId + " since checkout was successful");
        }
    }
}

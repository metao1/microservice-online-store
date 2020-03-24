package com.metao.product.card.service;

import com.metao.product.card.domain.ShoppingCard;
import com.metao.product.card.domain.ShoppingCardKey;

import java.time.LocalDateTime;
import java.util.Map;

public interface ShoppingCardService {

    int DEFAULT_QUANTITY = 1;

    public void addProductToShoppingCard(String userId, String asin);

    public Map<String, Integer> getProductsInCard(String userId);

    public void removeProductFromCard(String userId, String asin);

    default ShoppingCard createCardObject(ShoppingCardKey currentKey) {
        ShoppingCard currentShoppingCard = new ShoppingCard();
        currentShoppingCard.setCartKey(currentKey.getId() + "-" + currentKey.getAsin());
        currentShoppingCard.setUserId(currentKey.getId());
        currentShoppingCard.setAsin(currentKey.getAsin());
        LocalDateTime currentTime = LocalDateTime.now();
        currentShoppingCard.setTime_added(currentTime.toString());
        currentShoppingCard.setQuantity(DEFAULT_QUANTITY);

        return currentShoppingCard;
    }

    public void clearCard(String userId);
}

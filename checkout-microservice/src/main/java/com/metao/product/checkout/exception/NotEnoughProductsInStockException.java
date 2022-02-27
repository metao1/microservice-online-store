package com.metao.product.checkout.exception;

public class NotEnoughProductsInStockException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Not enough products in stock";

    public NotEnoughProductsInStockException() {
        super(DEFAULT_MESSAGE);
    }

    public NotEnoughProductsInStockException(String title, Integer quantity) {
        super(String.format("Not enough %s products in stock. Only %d left", title, quantity));
    }

    public NotEnoughProductsInStockException(String title) {
        super(String.format("No %s product in stock defined.", title));
    }


}

package com.metao.product.checkout.exception;

import lombok.NonNull;

public class CartIsEmptyException extends Exception {

    public CartIsEmptyException(@NonNull final String userId) {
        super(String.format("There is nothing in the card for user %s", userId));
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

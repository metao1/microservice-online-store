package com.metao.book.checkout.application.exception;

import lombok.NonNull;

public class CartIsEmptyException extends RuntimeException {

    public CartIsEmptyException(@NonNull final String userId) {
        super(String.format("There is nothing in the card for user %s", userId));
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

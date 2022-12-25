package com.metao.book.product.application.service;

import com.metao.book.product.application.model.User;
import com.metao.book.product.application.model.UserAware;
import java.util.Optional;
import java.util.stream.Stream;

public interface UserHandler {

    default Optional<User> handleUser(UserAware... values) {
        return Stream.of(values)
            .map(UserAware::getUser)
            .map(Optional::ofNullable)
            .flatMap(Optional::stream)
            .distinct()
            .findFirst();
    }
}

package com.metao.book.product.domain.exception;

import com.metao.book.product.domain.model.valueobject.CategoryName;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a category is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(CategoryName message) {
        super("Category %s not found".formatted(message));
    }
}

package com.metao.book.product.domain.exception;

import com.metao.book.product.domain.model.valueobject.CategoryName;

/**
 * Exception thrown when a category is not found
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(CategoryName message) {
        super("Category %s not found".formatted(message));
    }
}

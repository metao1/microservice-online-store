package com.metao.book.product.domain.exception;

import com.metao.book.product.domain.model.valueobject.ProductSku;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(ProductSku id) {
        super(String.format("Product with id %s not found", id));
    }
}

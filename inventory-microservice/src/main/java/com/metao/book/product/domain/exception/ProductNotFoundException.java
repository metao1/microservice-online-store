package com.metao.book.product.domain.exception;

import com.metao.book.shared.domain.product.ProductSku;
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(ProductSku id) {
        super(String.format("Product with id %s not found", id));
    }
}

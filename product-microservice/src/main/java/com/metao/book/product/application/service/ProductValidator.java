package com.metao.book.product.application.service;

import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.application.service.Validator;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ProductValidator implements Validator<ProductEvent> {

    @Override
    public void validate(ProductEvent product) throws RuntimeException {
        Objects.requireNonNull(product, "product can't be null");
        Objects.requireNonNull(product.getCreatedOn(), "product create_on can't be null");
        Objects.requireNonNull(product.getCurrency(), "product currency can't be null");
        Objects.requireNonNull(product.getImageUrl(), "product image_url can't be null");
        Objects.requireNonNull(product.getProductId(), "product product_id can't be null");
        Objects.requireNonNull(product.getTitle(), "product title can't be null");
        Objects.requireNonNull(product.getVolume(), "product volume can't be null");
    }
}

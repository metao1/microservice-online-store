package com.metao.book.product.infrastructure.util;

import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.product.ProductSku;

public class ProductConstant {

    public static final ProductSku SKU = ProductSku.of("0594287995");
    public static final CategoryName CATEGORY = CategoryName.of("book");
}

package com.metao.book.product.domain.category;

import com.metao.book.shared.domain.base.ValueObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class Category implements ValueObject {

    String value;
}

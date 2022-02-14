package com.metao.product.retails.domain.image;

import com.metao.ddd.base.ValueObject;
import lombok.Getter;

@Getter
public class Image implements ValueObject {
    private final String url;

    Image(String url) {
        this.url = url;
    }
}

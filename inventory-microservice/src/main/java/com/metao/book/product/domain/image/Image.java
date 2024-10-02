package com.metao.book.product.domain.image;

import com.metao.book.shared.domain.base.ValueObject;
import java.util.Objects;

public record Image(String url) implements ValueObject {

    public Image(String url) {
        this.url = Objects.requireNonNull(url, "uuid must not be null");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Image image = (Image) obj;
        return image.url().equals(url);
    }

}

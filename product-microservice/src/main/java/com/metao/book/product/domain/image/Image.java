package com.metao.book.product.domain.image;

import com.metao.book.shared.domain.base.ValueObject;

import java.util.Objects;

public class Image implements ValueObject {

    private String url;

    public Image(String url) {
        this.url = Objects.requireNonNull(url, "uuid must not be null");
    }

    public String url() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass())
            return false;
        Image image = (Image) obj;
        return image.url().equals(url);
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        return 31 * result;
    }
}

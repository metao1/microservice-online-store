package com.metao.book.product.domain.category;

import com.metao.book.shared.domain.base.ValueObject;

public class Category implements ValueObject {

    private final String value;

    public Category(String category) {
        this.value = category;
    }

    public String category() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj != this)
            return false;
        Category cat = (Category) obj;
        return cat.category().equals(value);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        return 31 * result;
    }
}

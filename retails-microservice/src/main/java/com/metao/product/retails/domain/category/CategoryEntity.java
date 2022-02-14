package com.metao.product.retails.domain.category;

import com.metao.ddd.base.AbstractEntity;

public class CategoryEntity extends AbstractEntity<CategoryId> {

    private final String category;

    public CategoryEntity(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}

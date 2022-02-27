package com.metao.product.domain.category;

import com.metao.ddd.base.AbstractEntity;
import com.metao.ddd.base.DomainObjectId;

public class CategoryEntity extends AbstractEntity<CategoryId> {

    private final String category;

    public CategoryEntity(String category) {
        super(DomainObjectId.randomId(CategoryId.class));
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        return category.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CategoryEntity)){
            return false;
        }
        var ce = (CategoryEntity) obj;
        return this.category.equals(ce.getCategory());
    }
}

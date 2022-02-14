package com.metao.product.retails.domain.product;

import com.metao.ddd.base.AbstractEntity;
import com.metao.ddd.base.DomainObjectId;
import com.metao.product.retails.domain.category.CategoryEntity;
import lombok.NonNull;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    private final Set<CategoryEntity> categories;

    public ProductCategoryEntity() {
        super(DomainObjectId.randomId(ProductCategoryId.class));
        this.categories = new TreeSet<>(Comparator.comparing(CategoryEntity::getCategory));
    }

    public void addCategoryEntity(@NonNull CategoryEntity category) {
        categories.add(category);
    }

    public void removeCategoryEntity(@NonNull CategoryEntity categoryId) {
        categories.remove(categoryId);
    }

    public Set<CategoryEntity> getCategories() {
        return categories;
    }
}

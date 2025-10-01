package com.metao.book.product.domain.model.entity;

import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.base.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Product category domain entity
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends Entity<CategoryId> {

    private CategoryName name;

    // For reconstruction from persistence
    protected ProductCategory() {
        super();
    }

    // For reconstruction from persistence
    public static ProductCategory of(CategoryId categoryId, CategoryName name) {
        var category = new ProductCategory();
        category.setId(categoryId);
        category.name = name;
        return category;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}

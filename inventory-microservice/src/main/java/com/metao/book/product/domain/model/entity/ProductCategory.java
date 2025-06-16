package com.metao.book.product.domain.model.entity;

import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.base.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

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

    public ProductCategory(@NonNull CategoryId categoryId, @NonNull CategoryName name) {
        super(categoryId);
        this.name = name;
    }

    // For reconstruction from persistence
    public static ProductCategory reconstruct(CategoryId categoryId, CategoryName name) {
        ProductCategory category = new ProductCategory();
        category.setId(categoryId);
        category.name = name;
        return category;
    }

    public void updateName(@NonNull CategoryName newName) {
        if (!this.name.equals(newName)) {
            this.name = newName;
        }
    }

    @Override
    public String toString() {
        return name.toString();
    }
}

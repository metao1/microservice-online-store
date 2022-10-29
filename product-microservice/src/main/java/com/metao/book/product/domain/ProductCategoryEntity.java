package com.metao.book.product.domain;

import com.metao.book.product.domain.category.Category;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.DomainObjectId;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "product_category")
public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    @Column(name = "category", nullable = false)
    private Category category;

    public ProductCategoryEntity(@NotNull Category category) {
        super(DomainObjectId.randomId(ProductCategoryId.class));
        this.category = category;
    }

    @SuppressWarnings("unused")
    public ProductCategoryEntity() {
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProductCategoryEntity cat = (ProductCategoryEntity) obj;
        return (this.category != null ? !this.category.equals(cat.getCategory()) : cat.getCategory() != null);
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.category().hashCode() : 0;
        return 31 * result;
    }
}

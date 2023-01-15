package com.metao.book.product.domain;

import com.metao.book.product.domain.category.Category;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.DomainObjectId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "product_category")
public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    @Column(name = "category", nullable = false)
    private Category category;

    @ManyToMany(mappedBy = "categories")
    private Set<ProductEntity> productEntities;

    @SuppressWarnings("unused")
    public ProductCategoryEntity() {
    }

    public ProductCategoryEntity(@NotNull Category category) {
        super(DomainObjectId.randomId(ProductCategoryId.class));
        this.category = category;
        this.productEntities = new HashSet<>();
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProductCategoryEntity cat = (ProductCategoryEntity) obj;
        return (this.category != null ? !this.category.equals(cat.getCategory()) : cat.getCategory() != null);
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.category().hashCode() : 0;
        return 31 * result;
    }
}

package com.metao.book.product.domain.category;

import com.metao.book.product.domain.ProductEntity;
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
@Entity(name = "product_category")
@Table(name = "product_category")
public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    @Column(name = "category", nullable = false)
    private String category;

    @ManyToMany(mappedBy = "categories")
    private Set<ProductEntity> productEntities;

    @SuppressWarnings("unused")
    public ProductCategoryEntity() {
    }

    public ProductCategoryEntity(@NotNull String category) {
        super(DomainObjectId.randomId(ProductCategoryId.class));
        this.category = category;
        this.productEntities = new HashSet<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        ProductCategoryEntity other = (ProductCategoryEntity) obj;
        return other.category.equals(category);
    }

    @Override
    public int hashCode() {
        int result = category.hashCode();
        return 31 * result;
    }

}

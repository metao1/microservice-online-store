package com.metao.book.product.domain;

import com.metao.book.product.domain.category.Category;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.DomainObjectId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "product_category")
@Table(name = "product_category")
public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    @Getter
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

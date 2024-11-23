package com.metao.book.product.domain.category;

import com.metao.book.product.domain.ProductEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Setter
@Getter
@Cacheable
@Entity(name = "product_category")
@Table(name = "product_category")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, unique = true)
    private String category;

    @ManyToMany(mappedBy = "categories")
    private Set<ProductEntity> productEntities;

    @SuppressWarnings("unused")
    public ProductCategoryEntity() {
    }

    public ProductCategoryEntity(@NotNull String category) {
        this.category = category;
        this.productEntities = new HashSet<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        ProductCategoryEntity other = (ProductCategoryEntity) obj;
        return other.category.equals(category);
    }

}

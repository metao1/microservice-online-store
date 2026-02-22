package com.metao.book.product.infrastructure.persistence.entity;

import com.metao.book.product.domain.model.valueobject.CategoryName;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;


/**
 * JPA entity for ProductCategory persistence
 */
@Setter
@Getter
@Cacheable
@NoArgsConstructor
@Table(name = "product_category")
@Entity(name = "product_category")
@NaturalIdCache(region = "CategoryEntity_NaturalId")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CategoryEntity implements Serializable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @NaturalId
    @Column(name = "category", nullable = false, unique = true)
    private String category;

    @ManyToMany(mappedBy = "categories")
    private Set<ProductEntity> products = new HashSet<>();

    public CategoryEntity(@NotNull CategoryName category) {
        this.category = category.value();
        this.products = new HashSet<>();
    }

    public CategoryEntity(@NotNull String category) {
        this.category = CategoryName.of(category).value();
        this.products = new HashSet<>();
    }

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
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
        CategoryEntity other = (CategoryEntity) obj;
        return other.category.equals(category);
    }
}

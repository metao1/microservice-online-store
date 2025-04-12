package com.metao.book.product.domain.category;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.shared.domain.base.AbstractEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.springframework.cache.annotation.EnableCaching;

@Setter
@Getter
//@Cacheable
@NaturalIdCache
//@EnableCaching
@Entity(name = "product_category")
@Table(name = "product_category")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductCategoryEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @SequenceGenerators(
        @SequenceGenerator(
            name = "product_sequence"
        )
    )
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NaturalId
    @BatchSize(size = 50)
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

package com.metao.book.product.infrastructure.persistence.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.validator.constraints.Length;

/**
 * JPA entity for Product persistence
 */
@Getter
@Cacheable
@NaturalIdCache
@NoArgsConstructor
@Table(name = "product")
@Entity(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @SequenceGenerator(name = "product_sequence")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull
    @NaturalId
    @Column(name = "sku", nullable = false, unique = true, length = 10)
    private String sku;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(nullable = false)
    private BigDecimal volume;

    @Length(min = 3)
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Basic(fetch = LAZY)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money unitPrice;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updateTime;

    @BatchSize(size = 50)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final Set<CategoryEntity> categories = new HashSet<>();

    public ProductEntity(
        String sku,
        String title,
        String description,
        BigDecimal volume,
        Money price,
        String imageUrl,
        LocalDateTime createdTime,
        LocalDateTime updateTime
    ) {
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.unitPrice = price;
        this.imageUrl = imageUrl;
        this.createdTime = createdTime;
        this.updateTime = updateTime;
    }

    public void addCategory(CategoryEntity categoryEntity) {
        categories.add(categoryEntity);
    }

    public void removeCategory(CategoryEntity categoryEntity) {
        categories.remove(categoryEntity);
    }
}

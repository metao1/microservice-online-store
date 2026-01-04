package com.metao.book.product.infrastructure.persistence.entity;

import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

/**
 * JPA entity for Product persistence
 */
@Getter
@Cacheable
@NaturalIdCache
@NoArgsConstructor
@Entity(name = "product")
@Table(name = "product_table")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductEntity implements Serializable {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, unique = true, length = 10))
    private ProductSku sku;

    @Version
    @Column(name = "version")
    private Long version;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "volume", nullable = false))
    private ProductVolume volume;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title", nullable = false))
    private ProductTitle title;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", columnDefinition = "TEXT"))
    private ProductDescription description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "image_url", nullable = false))
    private ImageUrl imageUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_value")),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(name = "created_time", nullable = false)
    private Instant createdTime;

    @Column(name = "updated_time", nullable = false)
    private Instant updateTime;

    @BatchSize(size = 50)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(
        name = "product_category_map",
        joinColumns = @JoinColumn(name = "product_sku"),
        inverseJoinColumns = @JoinColumn(name = "product_category_id")
    )
    private final Set<CategoryEntity> categories = new HashSet<>();

    public ProductEntity(
        ProductSku sku,
        ProductTitle title,
        ProductDescription description,
        ProductVolume volume,
        Money price,
        ImageUrl imageUrl,
        Instant createdTime,
        Instant updateTime
    ) {
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.price = price;
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

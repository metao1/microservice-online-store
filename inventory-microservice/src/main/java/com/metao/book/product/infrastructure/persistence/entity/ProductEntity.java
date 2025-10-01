package com.metao.book.product.infrastructure.persistence.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Setter
@NoArgsConstructor
@Cacheable
@NaturalIdCache
@Entity(name = "product")
@Table(name = "product")
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

    @Column(name = "price_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceValue;

    @Valid
    @Column(name = "price_currency", nullable = false)
    private Currency priceCurrency;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updateTime;

    @BatchSize(size = 50)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<CategoryEntity> categories = new HashSet<>();

    public ProductEntity(
        String sku,
        String title,
        String description,
        BigDecimal volume,
        Money price,
        String imageUrl
    ) {
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.priceValue = price.doubleAmount();
        this.priceCurrency = price.currency();
        this.imageUrl = imageUrl;
        this.createdTime = LocalDateTime.now();
        this.updateTime = this.createdTime;
        this.categories = new HashSet<>();
    }

    public void addCategory(CategoryEntity productCategory) {
        categories.add(productCategory);
    }

    @PreUpdate
    public void onPreUpdate() {
        updateTime = LocalDateTime.now();
    }
}

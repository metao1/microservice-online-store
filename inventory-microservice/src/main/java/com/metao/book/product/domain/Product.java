package com.metao.book.product.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.metao.book.product.domain.category.ProductCategory;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.validator.constraints.Length;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

@Getter
@Setter
@ToString
@Cacheable
@NaturalIdCache
@EnableCaching
@NoArgsConstructor
@Entity(name = "product")
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @SequenceGenerators(
        @SequenceGenerator(
            name = "product_sequence"
        )
    )
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull
    @NaturalId
    @Column(name = "asin", nullable = false, unique = true, length = 10)
    private String asin;

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
    @Size(max = 10_485_760, message = "Content exceeds 10MB limit")
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

    @Exclude
    @BatchSize(size = 50)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategory> categories;

    public Product(
        @NonNull String asin,
        @NonNull String title,
        @NonNull String description,
        @NonNull BigDecimal volume,
        @NonNull Money money,
        @NonNull String imageUrl
    ) {
        this.asin = asin;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.imageUrl = imageUrl;
        this.createdTime = LocalDateTime.now();
        this.updateTime = createdTime;
    }

    public void addCategory(@NonNull ProductCategory category) {
        if (categories == null) {
            categories = new HashSet<>();
        }
        categories.add(category);
        this.updateTime = LocalDateTime.now();
        category.getProductEntities().add(this);
    }

    public void addCategories(@NonNull Set<ProductCategory> categories) {
        categories.forEach(this::addCategory);
        this.updateTime = LocalDateTime.now();
    }

    public void removeCategory(@NonNull ProductCategory category) {
        if (!CollectionUtils.isEmpty(category.getProductEntities())) {
            categories.remove(category);
            category.getProductEntities().remove(this);
            this.updateTime = LocalDateTime.now();
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(asin);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return true;
        }
        Class<?> oEffectiveClass =
            o instanceof HibernateProxy hibernateproxy ? hibernateproxy.getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy hibernateproxy ? hibernateproxy.getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        Product that = (Product) o;
        return getAsin() != null && Objects.equals(getAsin(), that.getAsin());
    }
}

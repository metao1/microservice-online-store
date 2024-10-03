package com.metao.book.product.domain;

import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.shared.domain.base.ConcurrencySafeDomainObject;
import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.lang.NonNull;

@Getter
@Setter
@ToString
@NaturalIdCache // fetch the entity without hitting the database
@NoArgsConstructor
@Entity(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductEntity implements ConcurrencySafeDomainObject {

    @Version
    private Long version;

    @Id
    @NaturalId
    @Column(name = "asin", nullable = false)
    private String asin;

    @Column(nullable = false)
    private BigDecimal volume;

    @Length(min = 3, max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Length(min = 3, max = 1200)
    @Column(name = "description", length = 1200)
    private String description;

    @Column(name = "image", nullable = false)
    private String imageUrl;

    @Column(name = "price_value", nullable = false)
    private BigDecimal priceValue;

    @Valid
    @Column(name = "price_currency", nullable = false)
    private Currency priceCurrency;

    @Column(name = "bought_together")
    private String boughtTogether;

    @Exclude
    @BatchSize(size = 20)
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "product_category_map", joinColumns = {@JoinColumn(name = "asin")}, inverseJoinColumns = {
        @JoinColumn(name = "product_category_id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategoryEntity> categories;

    public ProductEntity(
        @NonNull String asin,
        @NonNull String title,
        @NonNull String description,
        @NonNull BigDecimal volume,
        @NonNull Money money,
        @NonNull String imageUrl
    ) {
        this.version = 1L;
        this.asin = asin;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.imageUrl = imageUrl;
        this.categories = new HashSet<>();
    }

    public void addCategory(@NonNull ProductCategoryEntity category) {
        categories.add(category);
        category.getProductEntities().add(this);
    }

    public void addBoughtTogether(@NonNull List<String> asin) {
        this.boughtTogether = asin.stream().reduce("", (a, b) -> a + "," + b);
    }

    public void removeCategory(@NonNull ProductCategoryEntity category) {
        categories.remove(category);
        category.getProductEntities().remove(this);
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
        ProductEntity that = (ProductEntity) o;
        return getAsin() != null && Objects.equals(getAsin(), that.getAsin());
    }
}

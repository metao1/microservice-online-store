package com.metao.book.product.domain;

import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.ConcurrencySafeDomainObject;
import com.metao.book.shared.domain.base.DomainObjectId;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache // fetch the entity without hitting the database
public class ProductEntity extends AbstractEntity<ProductId> implements ConcurrencySafeDomainObject {

    @Version
    Long version;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String asin;

    @Column
    private BigDecimal volume;

    @Length(min = 3, max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Length(min = 3, max = 1200)
    @Column(name = "description", length = 1200)
    private String description;

    @Column(name = "image", nullable = false)
    private Image image;

    @Column(name = "price_value", nullable = false)
    private BigDecimal priceValue;

    @Valid
    @Column(name = "price_currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency priceCurrency;

    @Column(name = "reserved_items")
    private BigDecimal reservedItems;

    @BatchSize(size = 20)
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "product_category_map", joinColumns = { @JoinColumn(name = "product_id") }, inverseJoinColumns = {
            @JoinColumn(name = "product_category_id") })
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategoryEntity> categories;

    public ProductEntity(
        @NonNull String asin,
        @NonNull String title,
        @NonNull String description,
        @NonNull BigDecimal volume,
        @NonNull Money money,
        @NonNull Image image
    ) {
        super(DomainObjectId.randomId(ProductId.class));
        this.asin = asin;
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.reservedItems = BigDecimal.ZERO;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.image = image;
        this.categories = new HashSet<>();
    }

    public void addCategory(@NonNull ProductCategoryEntity category) {
        categories.add(category);
        category.getProductEntities().add(this);
    }

    public void removeCategory(@NonNull ProductCategoryEntity category) {
        categories.remove(category);
        category.getProductEntities().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ProductEntity that = (ProductEntity) o;
        return Objects.equals(getAsin(), that.getAsin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getAsin());
    }

}

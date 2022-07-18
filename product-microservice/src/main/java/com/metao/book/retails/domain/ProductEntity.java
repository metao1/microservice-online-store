package com.metao.book.retails.domain;

import com.metao.book.retails.domain.image.Image;
import com.metao.book.shared.domain.base.AbstractAggregateRoot;
import com.metao.book.shared.domain.base.ConcurrencySafeDomainObject;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Table(name = "product")
public class ProductEntity extends AbstractAggregateRoot<ProductId> implements ConcurrencySafeDomainObject {

    @Version
    Long version;

    @Length(min = 3, max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Length(min = 3, max = 1200)
    @Column(name = "description", length = 1200)
    private String description;

    @Column(name = "image", nullable = false)
    private Image image;

    @Column(name = "price_value", nullable = false)
    private Double priceValue;

    @Valid
    @Column(name = "price_currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency priceCurrency;

    private int availableItems;

    private int reservedItems;

    @BatchSize(size = 20)
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "product_category_map", joinColumns = {@JoinColumn(name = "product_id")}, inverseJoinColumns = {
            @JoinColumn(name = "product_category_id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategoryEntity> productCategory;

    public ProductEntity(
            @NonNull String id,
            @NonNull String title,
            @NonNull String description,
            @NonNull Money money,
            @NonNull Image image) {
        super(new ProductId(id));
        this.title = title;
        this.description = description;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.image = image;
        this.productCategory = new HashSet<>();
    }

    public String getTitle() {
        return title;
    }

    public Double getPriceValue() {
        return priceValue;
    }

    public String getDescription() {
        return description;
    }

    public void addCategory(@NonNull ProductCategoryEntity category) {
        productCategory.add(category);
    }

    public Currency getPriceCurrency() {
        return priceCurrency;
    }

    public Set<ProductCategoryEntity> getProductCategory() {
        return productCategory;
    }

    public Image getImage() {
        return image;
    }

    public int getAvailableItems() {
        return availableItems;
    }

    public void setAvailableItems(int availableItems) {
        this.availableItems = availableItems;
    }

    public int getReservedItems() {
        return reservedItems;
    }

    public void setReservedItems(int reservedItems) {
        this.reservedItems = reservedItems;
    }

    @Override
    public Long version() {
        return version;
    }
}

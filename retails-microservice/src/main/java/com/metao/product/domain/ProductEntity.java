package com.metao.product.domain;

import com.metao.ddd.base.AbstractAggregateRoot;
import com.metao.ddd.base.ConcurrencySafeDomainObject;
import com.metao.ddd.base.DomainObjectId;
import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.domain.image.Image;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;

@Entity
@Table(name = "product")
public class ProductEntity extends AbstractAggregateRoot<ProductId> implements ConcurrencySafeDomainObject {

    @Version
    private Long version;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title", nullable = false)
    private String description;

    @Column(name = "title", nullable = false)
    private Image image;

    @NonNull
    private Double priceValue;

    @Valid
    @Enumerated(EnumType.STRING)
    private Currency priceCurrency;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategoryEntity> productCategory;

    public ProductEntity(
            @NonNull String title,
            @NonNull String description,
            @NonNull Money money,
            @NonNull Image image) {
        super(DomainObjectId.randomId(ProductId.class));
        this.title = title;
        this.description = description;
        this.priceValue = money.getAmount();
        this.priceCurrency = money.getCurrency();
        this.image = image;
        this.productCategory = new HashSet<>();
    }

    @SuppressWarnings("unused")
    private ProductEntity() {

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

    @Nullable
    @Override
    public Long version() {
        return version;
    }
}

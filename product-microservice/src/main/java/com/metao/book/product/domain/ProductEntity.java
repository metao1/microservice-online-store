package com.metao.book.product.domain;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.ConcurrencySafeDomainObject;
import com.metao.book.shared.domain.base.DomainObjectId;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@NoArgsConstructor
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache// fetch the entity without hitting the database
public class ProductEntity extends AbstractEntity<ProductId> implements ConcurrencySafeDomainObject {

    @NaturalId
    @Column(nullable = false, unique = true)
    private String isin;

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
    private BigDecimal priceValue;

    @Valid
    @Column(name = "price_currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency priceCurrency;

    private double availableItems;

    private double reservedItems;

    @BatchSize(size = 20)
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "product_category_map",
        joinColumns =
            {@JoinColumn(name = "product_id")},
        inverseJoinColumns = {
            @JoinColumn(name = "product_category_id")}
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategoryEntity> categories;

    public ProductEntity(
            @NonNull String asin,
            @NonNull String title,
            @NonNull String description,
            @NonNull Money money,
            @NonNull Image image) {
        super(DomainObjectId.randomId(ProductId.class));
        this.isin = asin;
        this.title = title;
        this.description = description;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.image = image;
        this.categories = new HashSet<>();
    }

    public static ProductDTO toDto(@Valid ProductEntity pr) {
        return ProductDTO.builder().description(pr.getDescription()).title(pr.getTitle()).asin(pr.getIsin())
            .currency(pr.getPriceCurrency())
            .price(pr.getPriceValue())
            .categories(mapCategoryEntitiesToDTOs(pr.getCategories()))
            .imageUrl(pr.getImage().url())
            .build();
    }

    private static Set<CategoryDTO> mapCategoryEntitiesToDTOs(@NonNull Set<ProductCategoryEntity> source) {
        return source
            .stream()
            .map(ProductCategoryEntity::getCategory)
            .map(Category::category)
            .map(CategoryDTO::of)
            .collect(Collectors.toSet());
    }

    public void addCategory(@NonNull ProductCategoryEntity category) {
        categories.add(category);
        category.getProductEntities().add(this);
    }

    public void removeCategory(@NonNull ProductCategoryEntity category) {
        categories.remove(category);
        category.getProductEntities().remove(this);
    }
}

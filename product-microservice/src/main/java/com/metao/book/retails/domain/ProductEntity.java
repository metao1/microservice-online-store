package com.metao.book.retails.domain;

import com.metao.book.retails.application.dto.CategoryDTO;
import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.category.Category;
import com.metao.book.retails.domain.image.Image;
import com.metao.book.shared.domain.base.AbstractAggregateRoot;
import com.metao.book.shared.domain.base.ConcurrencySafeDomainObject;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class ProductEntity extends AbstractAggregateRoot<ProductId> implements ConcurrencySafeDomainObject {

    @Id
    ProductId id;

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

    private int availableItems;

    private int reservedItems;

    @BatchSize(size = 20)
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "product_category_map", joinColumns = {@JoinColumn(name = "product_id")}, inverseJoinColumns = {
            @JoinColumn(name = "product_category_id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ProductCategoryEntity> productCategory;

    public ProductEntity(
            @NonNull String asin,
            @NonNull String title,
            @NonNull String description,
            @NonNull Money money,
            @NonNull Image image) {
        super(new ProductId(asin));
        this.title = title;
        this.description = description;
        this.priceValue = money.doubleAmount();
        this.priceCurrency = money.currency();
        this.image = image;
        this.productCategory = new HashSet<>();
    }

    public static ProductDTO toDto(@Valid ProductEntity pr) {
        return ProductDTO.builder()
                .description(pr.getDescription())
                .title(pr.getTitle())
                .asin(pr.id().toUUID())
                .currency(pr.getPriceCurrency())
                .price(pr.getPriceValue())
                .categories(mapCategoryEntitiesToDTOs(pr.getProductCategory()))
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
        productCategory.add(category);
    }
}

package com.metao.book.product.infrastructure.persistence.entity;

import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.infrastructure.persistence.converter.ImageUrlAttributeConverter;
import com.metao.book.product.infrastructure.persistence.converter.ProductDescriptionAttributeConverter;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.spring.persistence.MoneyEmbeddable;
import com.metao.book.shared.spring.persistence.ProductTitleAttributeConverter;
import com.metao.book.shared.spring.persistence.QuantityAttributeConverter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

/**
 * JPA entity for Product persistence
 */
@Getter
@NoArgsConstructor
@Entity(name = "product")
@Table(name = "product_table")
public class ProductEntity implements Serializable {

    @Id
    @Column(name = "sku", nullable = false, unique = true, length = 10)
    private String sku;

    @Setter
    @Version
    @Column(name = "version")
    private Long version;

    @Convert(converter = QuantityAttributeConverter.class)
    @Column(name = "volume", nullable = false)
    private Quantity volume;

    @Convert(converter = ProductTitleAttributeConverter.class)
    @Column(name = "title", nullable = false)
    private ProductTitle title;

    @Convert(converter = ProductDescriptionAttributeConverter.class)
    @Column(name = "description", columnDefinition = "TEXT")
    private ProductDescription description;

    @Convert(converter = ImageUrlAttributeConverter.class)
    @Column(name = "image_url", nullable = false)
    private ImageUrl imageUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_value")),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private MoneyEmbeddable price;

    @Column(name = "created_time", nullable = false)
    private Instant createdTime;

    @Column(name = "updated_time", nullable = false)
    private Instant updateTime;

    @BatchSize(size = 50)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
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
        Quantity volume,
        Money price,
        ImageUrl imageUrl,
        Instant createdTime,
        Instant updateTime
    ) {
        this.sku = sku.value();
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.price = MoneyEmbeddable.from(price);
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

    public void updateFrom(ProductEntity source) {
        this.volume = source.getVolume();
        this.title = source.getTitle();
        this.description = source.getDescription();
        this.imageUrl = source.getImageUrl();
        this.price = source.getPrice();
        this.createdTime = source.getCreatedTime();
        this.updateTime = source.getUpdateTime();

        this.categories.clear();
        source.getCategories().forEach(this::addCategory);
    }

}

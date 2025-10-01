package com.metao.book.product.domain.model.aggregate;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.event.ProductCreatedEvent;
import com.metao.book.product.domain.model.event.ProductUpdatedEvent;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

/**
 * Product aggregate root - contains all business logic for product management
 */
@Getter
public class Product extends AggregateRoot<ProductSku> {

    private ProductTitle title;
    private ProductDescription description;
    private ProductVolume volume;
    private Money price;
    private ImageUrl imageUrl;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Set<ProductCategory> categories;

    // For reconstruction from persistence
    protected Product() {
        super();
        this.categories = new HashSet<>();
    }

    // Constructor for new products
    public Product(
        @NonNull ProductSku productSku,
        @NonNull ProductTitle title,
        @NonNull ProductDescription description,
        @NonNull ProductVolume volume,
        @NonNull Money price,
        @NonNull ImageUrl imageUrl
    ) {
        super(productSku);
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.price = price;
        this.imageUrl = imageUrl;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = this.createdTime;
        this.categories = new HashSet<>();

        // Raise domain event
        addDomainEvent(new ProductCreatedEvent(this.getId(), this.title, this.price));
    }

    // For reconstruction from persistence
    public static Product reconstruct(
        ProductSku productSku,
        ProductTitle title,
        ProductDescription description,
        ProductVolume volume,
        Money price,
        ImageUrl imageUrl,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        Set<ProductCategory> categories
    ) {
        Product product = new Product();
        product.setId(productSku);
        product.title = title;
        product.description = description;
        product.volume = volume;
        product.price = price;
        product.imageUrl = imageUrl;
        product.createdTime = createdTime;
        product.updatedTime = updatedTime;
        product.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();
        return product;
    }

    // Business methods
    public void updatePrice(@NonNull Money newPrice) {
        if (newPrice.fixedPointAmount().compareTo(this.price.fixedPointAmount()) != 0) {
            Money oldPrice = this.price;
            this.price = newPrice;
            this.updatedTime = LocalDateTime.now();

            addDomainEvent(new ProductUpdatedEvent(this.getId(), this.title, oldPrice, newPrice));
        }
    }

    public void updateTitle(@NonNull ProductTitle newTitle) {
        if (!this.title.equals(newTitle)) {
            this.title = newTitle;
            this.updatedTime = LocalDateTime.now();
        }
    }

    public void updateDescription(@NonNull ProductDescription newDescription) {
        if (!this.description.equals(newDescription)) {
            this.description = newDescription;
            this.updatedTime = LocalDateTime.now();
        }
    }

    public void addCategory(@NonNull ProductCategory category) {
        if (this.categories.add(category)) {
            this.updatedTime = LocalDateTime.now();
        }
    }

    public boolean isInStock() {
        return this.volume.value().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    public void reduceVolume(@NonNull ProductVolume reduction) {
        if (reduction.value().compareTo(this.volume.value()) > 0) {
            throw new IllegalArgumentException("Cannot reduce volume by more than available");
        }
        this.volume = new ProductVolume(this.volume.value().subtract(reduction.value()));
        this.updatedTime = LocalDateTime.now();
    }

    public void increaseVolume(@NonNull ProductVolume increase) {
        this.volume = new ProductVolume(this.volume.value().add(increase.value()));
        this.updatedTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

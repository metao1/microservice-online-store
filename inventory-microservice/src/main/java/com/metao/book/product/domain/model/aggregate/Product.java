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
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product aggregate root - contains all business logic for product management
 */
@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class Product extends AggregateRoot<ProductSku> {

    @NonNull
    private ProductTitle title;
    @NonNull
    private ProductDescription description;
    @NonNull
    private ProductVolume volume;
    @NonNull
    private Money money;
    @NonNull
    private ImageUrl imageUrl;
    @NonNull
    private final Instant createdTime;
    @NonNull
    private Set<ProductCategory> categories;
    private Instant updatedTime;

    // Constructor for new products
    public Product(
            @NotNull ProductSku productSku,
            @NotNull ProductTitle title,
            @NotNull ProductDescription description,
            @NotNull ProductVolume volume,
            @NonNull Money money,
            @NonNull Instant createdTime,
            @NonNull ImageUrl imageUrl,
            Set<ProductCategory> categories
    ) {
        super(productSku);
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.money = money;
        this.imageUrl = imageUrl;
        this.createdTime = createdTime;
        this.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();

        // Raise domain event
        addDomainEvent(new ProductCreatedEvent(this.getId(), this.title, this.money, this.createdTime));
    }

    // Business methods
    public void updatePrice(@NonNull Money newPrice) {
        if (newPrice.fixedPointAmount().compareTo(this.money.fixedPointAmount()) != 0) {
            Money oldPrice = this.money;
            this.money = newPrice;
            this.updatedTime = Instant.now();

            addDomainEvent(new ProductUpdatedEvent(this.getId(), this.title, oldPrice, newPrice, this.createdTime));
        }
    }

    public void updateTitle(@NonNull ProductTitle newTitle) {
        if (!this.title.equals(newTitle)) {
            this.title = newTitle;
            this.updatedTime = Instant.now();
        }
    }

    public void updateDescription(@NonNull ProductDescription newDescription) {
        if (!this.description.equals(newDescription)) {
            this.description = newDescription;
            this.updatedTime = Instant.now();
        }
    }

    public void addCategory(@NonNull ProductCategory category) {
        if (this.categories.add(category)) {
            this.updatedTime = Instant.now();
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
        this.updatedTime = Instant.now();
    }

    public void increaseVolume(@NonNull ProductVolume increase) {
        this.volume = new ProductVolume(this.volume.value().add(increase.value()));
        this.updatedTime = Instant.now();
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

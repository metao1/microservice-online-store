package com.metao.book.product.domain.model.aggregate;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.event.DomainProductCreatedEvent;
import com.metao.book.product.domain.model.event.DomainProductUpdatedEvent;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Product aggregate root - contains all business logic for product management
 */
@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class ProductAggregate extends AggregateRoot<ProductSku> {

    @NotNull
    private ProductTitle title;
    @NotNull
    private ProductDescription description;
    @NotNull
    private Quantity volume;
    @NotNull
    private Money money;
    @NotNull
    private ImageUrl imageUrl;
    @NotNull
    private Set<ProductCategory> categories;
    @NotNull
    private final Instant createdTime;
    private Instant updatedTime;


    // Constructor for new products
    public ProductAggregate(
        @NotNull ProductSku productSku,
        @NotNull ProductTitle title,
        @NotNull ProductDescription description,
        @NotNull Quantity volume,
        @NotNull Money money,
        @NotNull Instant createdTime,
        @NotNull Instant updatedTime,
        @NotNull ImageUrl imageUrl,
        Set<ProductCategory> categories
    ) {
        super(productSku);
        this.title = title;
        this.description = description;
        this.volume = volume;
        this.money = money;
        this.imageUrl = imageUrl;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime != null ? updatedTime : createdTime;
        this.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();

        // Raise domain event
        addDomainEvent(new DomainProductCreatedEvent(
            this.getId(),
            this.title,
            this.description,
            this.imageUrl,
            this.volume,
            this.money,
            this.createdTime));
    }

    // Business methods
    public void updatePrice(@NotNull Money newPrice) {
        if (newPrice.fixedPointAmount().compareTo(this.money.fixedPointAmount()) != 0) {
            Money oldPrice = this.money;
            this.money = newPrice;
            this.updatedTime = Instant.now();
            addDomainEvent(new DomainProductUpdatedEvent(this.getId(), this.title, oldPrice, newPrice, this.updatedTime));
        }
    }

    public void updateTitle(@NotNull ProductTitle newTitle) {
        if (!this.title.equals(newTitle)) {
            this.title = newTitle;
            // Title change should update timestamp but not raise domain event
            this.updatedTime = Instant.now();
        }
    }

    public void updateDescription(@NotNull ProductDescription newDescription) {
        if (!this.description.equals(newDescription)) {
            this.description = newDescription;
            // Description change updates timestamp but does not emit event
            this.updatedTime = Instant.now();
        }
    }

    public void addCategory(@NotNull ProductCategory category) {
        Objects.requireNonNull(category, "Category cannot be null");

        if (this.categories.add(category)) {
            this.updatedTime = Instant.now();
            addDomainEvent(new DomainProductUpdatedEvent(
                this.getId(),
                this.title,
                this.money,
                this.money,
                this.updatedTime));
        }
    }

    public boolean isInStock() {
        return this.volume.getValue().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    public void reduceVolume(@NotNull Quantity reduction) {
        if (reduction.getValue().compareTo(this.volume.getValue()) > 0) {
            throw new IllegalArgumentException("Cannot reduce volume by more than available");
        }
        this.volume = new Quantity(this.volume.getValue().subtract(reduction.getValue()));
        this.updatedTime = Instant.now();
        addDomainEvent(new DomainProductUpdatedEvent(
            this.getId(),
            this.title,
            this.money,
            this.money,
            this.updatedTime));
    }

    public void increaseVolume(@NotNull Quantity increase) {
        this.volume = new Quantity(this.volume.getValue().add(increase.getValue()));
        this.updatedTime = Instant.now();
        addDomainEvent(new DomainProductUpdatedEvent(
            this.getId(),
            this.title,
            this.money,
            this.money,
            this.updatedTime));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductAggregate product = (ProductAggregate) o;
        return Objects.equals(getId(), product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

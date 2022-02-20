package com.metao.product.domain;

import com.metao.ddd.base.AbstractAggregateRoot;
import com.metao.ddd.base.DomainObjectId;
import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.domain.category.CategoryEntity;
import com.metao.product.domain.image.Image;
import org.springframework.lang.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class ProductEntity extends AbstractAggregateRoot<ProductId> {

    @Min(10)
    @Max(120)
    private String title;

    @Min(90)
    @Max(1200)
    private String description;

    private Image image;

    private Double priceValue;

    private Currency priceCurrency;

    private ProductCategoryEntity productCategory;

    public ProductEntity(@NonNull String title,
                         @NonNull String description,
                         @NonNull Money price,
                         @NonNull Image image) {
        super(DomainObjectId.randomId(ProductId.class));
        setTitle(title);
        setPrice(price);
        setDescription(description);
        setImage(image);
        productCategory = new ProductCategoryEntity();
    }

    private void setImage(Image image) {
        this.image = image;
    }

    @SuppressWarnings("unused")
    private ProductEntity() {
        //used by JPA only
    }

    private void setDescription(@NonNull String description) {
        this.description = description;
    }

    private void setTitle(@NonNull String title) {
        this.title = title;
    }

    private void setPrice(@NonNull Money price) {
        this.priceValue = price.getAmount();
        this.priceCurrency = price.getCurrency();
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

    public void addCategory(@NonNull CategoryEntity category) {
        productCategory.addCategoryEntity(category);
    }

    public Currency getPriceCurrency() {
        return priceCurrency;
    }

    public ProductCategoryEntity getProductCategory() {
        return productCategory;
    }

    public Image getImage() {
        return image;
    }
}

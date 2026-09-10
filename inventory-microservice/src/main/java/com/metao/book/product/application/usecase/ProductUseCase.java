package com.metao.book.product.application.usecase;

import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.application.service.CreateProductResult;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@ApplicationUseCase
public interface ProductUseCase {

    CreateProductResult createProduct(@Valid CreateProductCommand command);

    CreateProductResult createProduct(@Valid CreateProductCommand command, String idempotencyKey);

    ProductAggregate updateProduct(@Valid UpdateProductCommand command);

    ProductAggregate getProductBySku(@NotNull String sku);

    List<ProductAggregate> getProductsBySkus(List<String> skus);

    List<ProductAggregate> searchProducts(String keyword, int offset, int limit);

    List<ProductAggregate> getProductsByCategory(CategoryName categoryName, int offset, int limit);

    List<ProductAggregate> getRelatedProducts(ProductSku sku, int limit);

    void assignProductToCategory(ProductSku productSku, CategoryName categoryName);

    void reduceProductVolume(ProductSku sku, Quantity quantity);

    boolean reduceProductVolumeAtomically(String sku, BigDecimal quantity);

    void increaseProductVolume(ProductSku sku, Quantity quantity);

    Set<ProductCategory> getCategories(int offset, int limit);
}

package com.metao.product.retails.application.service;

import com.metao.product.retails.application.exception.ProductNotFoundException;
import com.metao.product.retails.domain.product.ProductEntity;
import com.metao.product.retails.domain.product.ProductId;
import com.metao.product.retails.domain.product.ProductRepository;
import com.metao.product.retails.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productService")
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductEntity getProductById(ProductId productId) {
        return productRepository.findProductEntityById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toUUID()));
    }

    @Override
    public List<ProductEntity> getAllProductsPageable(int limit, int offset) {
//        var pageable = new OffsetBasedPageRequest(limit, offset);
//        List<ProductEntity> productEntities = productRepository.findAllProductsWithOffset(productId, pageable);
//        return productEntities.stream()
//                .filter(Objects::nonNull)
//                .toList();
        throw new NotImplementedException("getAllProductsPageable not yet implemented");
    }

//    public List<ProductEntity> getAllProductsWithCategory(ProductCategoriesService categoriesService, String category, int limit, int offset) {
//        Pageable pageable = new OffsetBasedPageRequest(limit, offset);
//        categoriesService.getProductCategories()
//        var result = this.productRepository.findAllProductsWithCategoryAndOffset(, category, pageable);
//        return result.stream()
//                .filter(Objects::nonNull)
//                .map(productMapper::mapToDto)
//                .collect(Collectors.toList());
//    }

    @Override
    public void saveProduct(ProductEntity pe) {
        this.productRepository.save(pe);
    }

}

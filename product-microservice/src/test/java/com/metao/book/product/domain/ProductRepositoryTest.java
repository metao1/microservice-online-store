package com.metao.book.product.domain;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import com.metao.book.product.domain.category.Category;
//import com.metao.book.product.util.BasePostgresIntegrationTest;
//import com.metao.book.product.util.ProductTestUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//@DataJpaTest
//class ProductRepositoryTest extends BasePostgresIntegrationTest {
//
//    @Autowired
//    ProductRepository productRepository;
//
//    @Test
//    void findByProductCategory() {
//        productRepository.save(ProductTestUtils.createProductEntity());
//        var optionalProductCategories = productRepository.findProductCategoriesByProductId(new ProductId("id"));
//        assertTrue(optionalProductCategories.isPresent());
//        assertThat(optionalProductCategories.get())
//            .extracting(ProductCategoryEntity::getCategory)
//            .contains(new Category("book"));
//    }
//}
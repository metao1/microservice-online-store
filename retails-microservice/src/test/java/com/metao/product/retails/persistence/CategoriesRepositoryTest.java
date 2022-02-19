package com.metao.product.retails.persistence;

import com.metao.product.retails.BaseTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CategoriesRepositoryTest extends BaseTest {

//    @Autowired
//    CategoriesRepository categoriesRepository;
//
//    @Test
//    void findProductEntities() {
//        categoriesRepository.save(productEntity);
//        List<ProductCategoryEntity> productEntities = categoriesRepository
//                .findProductEntities();
//        Assertions.assertThat(productEntities)
//                .isNotNull()
//                .hasSize(1)
//                .contains(productCategoryEntity)
//                .extracting(ProductCategoryEntity::getId)
//                .contains(productCategoryEntity.getId());
//    }
}

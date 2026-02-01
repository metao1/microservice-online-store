package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import com.metao.shared.test.KafkaContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CategoryEntityMapperIT extends KafkaContainer {

    @Autowired
    private CategoryEntityMapper categoryEntityMapper;

    @Test
    @Transactional
    void toEntity_WorksInSpringContext() {
        // Given
        CategoryName categoryName = CategoryName.of("Books");
        ProductCategory productCategory = ProductCategory.of(CategoryId.of("cat-1"), categoryName);

        // When
        CategoryEntity result = categoryEntityMapper.toEntity(productCategory);

        // Then
        assertNotNull(result);
        assertEquals("Books", result.getCategory());
    }
}

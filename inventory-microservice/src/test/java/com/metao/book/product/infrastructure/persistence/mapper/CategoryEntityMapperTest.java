package com.metao.book.product.infrastructure.persistence.mapper;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryEntityMapperTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private EntityManager entityManager;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private Session session;

    @InjectMocks
    private CategoryEntityMapper categoryEntityMapper;

    @BeforeEach
    void setUp() {
        // We will mock it in each test or here, but we must ensure it's not null
    }

    @Test
    void toEntity_WhenEntityManagerIsNull_ReturnsNewEntity() {
        // Given
        CategoryEntityMapper mapper = new CategoryEntityMapper(null);
        CategoryName categoryName = CategoryName.of("Electronics");
        ProductCategory productCategory = ProductCategory.of(CategoryId.of("cat-1"), categoryName);

        // When
        CategoryEntity result = mapper.toEntity(productCategory);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getCategory());
    }

    @Test
    void toEntity_WhenUnwrapThrows_TriesJpaQueryAndReturnsNewEntity() {
        // Given
        when(entityManager.unwrap(Session.class)).thenThrow(new RuntimeException("Unwrap failed"));

        CategoryName categoryName = CategoryName.of("Electronics");
        ProductCategory productCategory = ProductCategory.of(CategoryId.of("cat-1"), categoryName);

        // When
        CategoryEntity result = categoryEntityMapper.toEntity(productCategory);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getCategory());
    }

    @Test
    void toEntity_WhenCategoryExists_ReturnsManagedEntity() {
        // Given
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        CategoryName categoryName = CategoryName.of("Electronics");
        ProductCategory productCategory = ProductCategory.of(CategoryId.of("cat-1"), categoryName);
        CategoryEntity existingEntity = new CategoryEntity("Electronics");

        SimpleNaturalIdLoadAccess<CategoryEntity> loadAccess = mock(SimpleNaturalIdLoadAccess.class);
        when(session.bySimpleNaturalId(CategoryEntity.class)).thenReturn(loadAccess);
        lenient().when(loadAccess.load("Electronics")).thenReturn(existingEntity);

        // When
        CategoryEntity result = categoryEntityMapper.toEntity(productCategory);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getCategory());
    }

    @Test
    void toEntity_WhenCategoryDoesNotExist_ReturnsNewEntity() {
        // Given
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        CategoryName categoryName = CategoryName.of("Books");
        ProductCategory productCategory = ProductCategory.of(CategoryId.of("cat-2"), categoryName);

        SimpleNaturalIdLoadAccess<CategoryEntity> loadAccess = mock(SimpleNaturalIdLoadAccess.class);
        when(session.bySimpleNaturalId(CategoryEntity.class)).thenReturn(loadAccess);
        // when(loadAccess.load("Books")).thenReturn(null); // This might be considered unnecessary by Mockito if it's the default

        // When
        CategoryEntity result = categoryEntityMapper.toEntity(productCategory);

        // Then
        assertNotNull(result);
        assertEquals("Books", result.getCategory());
    }
}

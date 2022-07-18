package com.metao.book.retails.service;
/*
 * package com.metao.book.retail.retails.service;
 *
 * import com.metao.book.retail.retails.BaseTest;
 * import com.metao.book.retail.retails.domain.ProductEntity;
 * import com.metao.book.retail.retails.exception.ProductNotFoundException;
 * import com.metao.book.retail.retails.mapper.ProductMapper;
 * import com.metao.book.retail.retails.model.ProductDTO;
 * import com.metao.book.retail.retails.persistence.OffsetBasedPageRequest;
 * import com.metao.book.retail.retails.persistence.ProductRepository;
 * import
 * com.metao.book.retail.retails.service.impl.ProductServiceImplementation;
 * import org.assertj.core.api.Assertions;
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import org.mockito.InjectMocks;
 * import org.mockito.Mock;
 * import org.mockito.junit.jupiter.MockitoExtension;
 * import org.springframework.data.domain.Pageable;
 *
 * import java.util.List;
 *
 * import static java.util.List.of;
 * import static org.junit.jupiter.api.Assertions.assertSame;
 * import static org.mockito.Mockito.when;
 *
 * @ExtendWith(MockitoExtension.class)
 * class ProductServiceTest extends BaseTest {
 *
 * @Mock
 * ProductRepository productRepository;
 *
 * @Mock
 * ProductMapper productMapper;
 *
 * @InjectMocks
 * ProductServiceImplementation productService;
 *
 *
 * @Test
 * void getAllProductsPageable() {
 * when(productMapper.mapToDto(productEntity))
 * .thenReturn(productDTO);
 * Pageable pageable = new OffsetBasedPageRequest(1, 0);
 * List<ProductEntity> productEntities = of(productEntity);
 * when(productRepository.findAllProductsWithOffset(pageable))
 * .thenReturn(productEntities);
 * List<ProductDTO> dtoList = productService.getAllProductsPageable(1, 0);
 * Assertions.assertThat(dtoList)
 * .isNotNull()
 * .hasSize(1)
 * .contains(productDTO);
 * }
 *
 * @Test
 * void getProductById() {
 * ProductNotFoundException productNotFoundException =
 * Assertions.catchThrowableOfType(() ->
 * productService.getProductById(PRODUCT_ID), ProductNotFoundException.class);
 * assertSame(ProductNotFoundException.class,
 * productNotFoundException.getClass());
 * assertSame(PRODUCT_ID, productNotFoundException.getProductId());
 * }
 *
 * @Test
 * void saveProduct() {
 * when(productRepository.save(productEntity))
 * .thenReturn(productEntity);
 * when(productMapper.mapToEntity(productDTO))
 * .thenReturn(productEntity);
 * productService.saveProduct(productDTO);
 * }
 * }
 */

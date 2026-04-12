package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.shared.domain.product.ProductSku;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for ProductEntity
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, ProductSku> {

    interface ProductCategoryRow {
        ProductSku getSku();
        String getCategoryId();
        String getCategoryName();
    }

    @Query("""
        select p.sku
        from product p
        join p.categories c
        where p.volume.value > 0
          and c.id = :categoryId
        """)
    List<ProductSku> findSkusByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

    @Query("""
        select distinct p.sku
        from product p
        join p.categories c
        where p.volume.value > 0
          and c.id in :categoryIds
        """)
    List<ProductSku> findSkusByCategoryIds(@Param("categoryIds") List<String> categoryIds, Pageable pageable);

    @Query("""
        select p.sku
        from product p
        where p.volume.value > 0
          and (
            lower(p.title.value) like lower(concat('%', :keyword, '%'))
            or lower(p.description.value) like lower(concat('%', :keyword, '%'))
          )
        """)
    List<ProductSku> searchSkusByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        select distinct p
        from product p
        left join fetch p.categories
        where p.sku in :skus
        """)
    List<ProductEntity> findAllWithCategoriesBySkuIn(@Param("skus") List<ProductSku> skus);

    @Query("""
        select p.sku as sku, c.id as categoryId, c.category as categoryName
        from product p
        join p.categories c
        where p.sku in :skus
        """)
    List<ProductCategoryRow> findCategoryRowsBySkuIn(@Param("skus") List<ProductSku> skus);

    @Modifying
    @Query(
        value = """
            INSERT INTO product_table
                (sku, version, volume, title, description, image_url, price_value, price_currency, created_time, updated_time)
            VALUES
                (:#{#product.sku.value},
                 0,
                 :#{#product.volume.value},
                 :#{#product.title.value},
                 :#{#product.description.value},
                 :#{#product.imageUrl.value},
                 :#{#product.price.fixedPointAmount()},
                 :#{#product.price.currency().currencyCode},
                 :#{#product.createdTime},
                 :#{#product.updateTime})
            ON CONFLICT (sku) DO NOTHING
            """,
        nativeQuery = true
    )
    int insertIfAbsent(@Param("product") ProductEntity product);

    @Modifying
    @Query(
        value = """
            update product p
               set p.volume.value = p.volume.value - :quantity,
                   p.version = p.version + 1,
                   p.updateTime = current_timestamp
             where p.sku.value = :sku
               and p.volume.value >= :quantity
            """
    )
    int decrementVolumeIfEnough(@Param("sku") String sku, @Param("quantity") BigDecimal quantity);

    boolean existsBySku(ProductSku sku);
}

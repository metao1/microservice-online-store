package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.shared.architecture.OutboundAdapter;
import io.micrometer.core.annotation.Timed;
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
@OutboundAdapter
public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {

    interface ProductCategoryRow {
        String getSku();
        String getCategoryId();
        String getCategoryName();
    }

    @Timed(value = "inventory.db.product.find-skus-by-category-id")
    @Query("""
        select p.sku
        from product p
        join p.categories c
        where p.volume > 0
          and c.id = :categoryId
        """)
    List<String> findSkusByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

    @Timed(value = "inventory.db.product.find-skus-by-category-ids")
    @Query("""
        select distinct p.sku
        from product p
        join p.categories c
        where p.volume > 0
          and c.id in :categoryIds
        """)
    List<String> findSkusByCategoryIds(@Param("categoryIds") List<String> categoryIds, Pageable pageable);

    @Timed(value = "inventory.db.product.search-skus-by-keyword")
    @Query("""
        select p.sku
        from product p
        where p.volume > 0
          and (
            lower(p.title) like lower(concat('%', :keyword, '%'))
            or lower(p.description) like lower(concat('%', :keyword, '%'))
          )
        """)
    List<String> searchSkusByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Timed(value = "inventory.db.product.find-all-by-id")
    List<ProductEntity> findAllById(Iterable<String> skus);

    @Timed(value = "inventory.db.product.find-all-with-categories-by-sku-in")
    @Query("""
        select distinct p
        from product p
        left join fetch p.categories
        where p.sku in :skus
        """)
    List<ProductEntity> findAllWithCategoriesBySkuIn(@Param("skus") List<String> skus);

    @Timed(value = "inventory.db.product.find-category-rows-by-sku-in")
    @Query("""
        select p.sku as sku, c.id as categoryId, c.category as categoryName
        from product p
        join p.categories c
        where p.sku in :skus
        """)
    List<ProductCategoryRow> findCategoryRowsBySkuIn(@Param("skus") List<String> skus);

    @Timed(value = "inventory.db.product.insert-if-absent")
    @Modifying
    @Query(
        value = """
            INSERT INTO product_table
                (sku, version, volume, title, description, image_url, price_value, price_currency, created_time, updated_time)
            VALUES
                (:#{#product.sku},
                 0,
                 :#{#product.volume.value},
                 :#{#product.title.value},
                 :#{#product.description.value},
                 :#{#product.imageUrl.value},
                 :#{#product.price.amount},
                 :#{#product.price.currency.currencyCode},
                 :#{#product.createdTime},
                 :#{#product.updateTime})
            ON CONFLICT (sku) DO NOTHING
            """,
        nativeQuery = true
    )
    int insertIfAbsent(@Param("product") ProductEntity product);

    @Timed(value = "inventory.db.product.decrement-volume-if-enough")
    @Modifying
    @Query(
        value = """
            UPDATE product_table
               SET volume = volume - :quantity,
                   version = version + 1,
                   updated_time = CURRENT_TIMESTAMP
             WHERE sku = :sku
               AND volume >= :quantity
            """,
        nativeQuery = true
    )
    int decrementVolumeIfEnough(@Param("sku") String sku, @Param("quantity") BigDecimal quantity);

    @Timed(value = "inventory.db.product.exists-by-sku")
    boolean existsBySku(String sku);
}

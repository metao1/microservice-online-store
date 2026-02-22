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

    @Query("SELECT p FROM product p JOIN p.categories c WHERE LOWER(c.category) = LOWER(:categoryName)")
    List<ProductEntity> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT p FROM product p JOIN p.categories c WHERE LOWER(c.category) IN :categoryNames")
    List<ProductEntity> findByCategories(@Param("categoryNames") List<String> categoryNames, Pageable pageable);

    @Query("SELECT p FROM product p WHERE " +
        "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProductEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

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
            UPDATE product_table
               SET volume = volume - :quantity,
                   version = version + 1,
                   updated_time = now()
             WHERE sku = :sku
               AND volume >= :quantity
            """,
        nativeQuery = true
    )
    int decrementVolumeIfEnough(@Param("sku") ProductSku sku, @Param("quantity") BigDecimal quantity);

    boolean existsBySku(ProductSku sku);
}

package com.metao.book.product.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCreateIdempotencyRepository {

    private static final String INSERT_IF_ABSENT_SQL = """
        INSERT INTO product_create_request(idempotency_key, sku, created_at)
        VALUES (?, ?, now())
        ON CONFLICT (idempotency_key) DO NOTHING
        """;

    private static final String FIND_SKU_BY_KEY_SQL = """
        SELECT sku
          FROM product_create_request
         WHERE idempotency_key = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public ClaimResult claim(String idempotencyKey, String sku) {
        int inserted = jdbcTemplate.update(INSERT_IF_ABSENT_SQL, idempotencyKey, sku);
        if (inserted > 0) {
            return ClaimResult.CLAIMED;
        }

        String existingSku = jdbcTemplate.query(
            FIND_SKU_BY_KEY_SQL,
            rs -> rs.next() ? rs.getString("sku") : null,
            idempotencyKey
        );

        if (sku.equals(existingSku)) {
            return ClaimResult.REPLAY;
        }
        return ClaimResult.CONFLICT;
    }

    public enum ClaimResult {
        CLAIMED,
        REPLAY,
        CONFLICT
    }
}

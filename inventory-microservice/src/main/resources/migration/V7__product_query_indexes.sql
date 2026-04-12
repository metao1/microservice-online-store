CREATE INDEX IF NOT EXISTS idx_product_category_map_product_sku_category_id
  ON product_category_map (product_sku, product_category_id);

CREATE INDEX IF NOT EXISTS idx_product_table_in_stock_sku
  ON product_table (sku)
  WHERE volume > 0;

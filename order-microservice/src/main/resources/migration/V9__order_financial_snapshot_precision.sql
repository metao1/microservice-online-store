ALTER TABLE orders
    ALTER COLUMN subtotal_amount TYPE DECIMAL(19, 2),
    ALTER COLUMN tax_amount TYPE DECIMAL(19, 2),
    ALTER COLUMN total_amount TYPE DECIMAL(19, 2);

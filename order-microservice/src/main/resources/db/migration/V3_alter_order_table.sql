ALTER TABLE order_table
    ADD COLUMN order_id NOT NULL INDEX order_id_idx ON order_table (order_id)
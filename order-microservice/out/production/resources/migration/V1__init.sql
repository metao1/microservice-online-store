-- Create orders table
CREATE TABLE IF NOT EXISTS orders
(
  id
  VARCHAR(255) NOT NULL,
  customer_id VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_orders PRIMARY KEY(id)
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items
(
  id BIGSERIAL NOT NULL,
  product_id VARCHAR(255) NOT NULL,
  product_name VARCHAR(255),
  quantity DECIMAL NOT NULL,
  unit_price DECIMAL NOT NULL,
  currency VARCHAR(3) NOT NULL,
  order_id VARCHAR(255) NOT NULL,
  CONSTRAINT pk_order_items PRIMARY KEY(id),
  CONSTRAINT fk_order_items_order FOREIGN KEY(order_id) REFERENCES orders(id)
);

-- Create shopping_cart table
CREATE TABLE IF NOT EXISTS shopping_cart
(
  user_id VARCHAR(255) NOT NULL,
  sku VARCHAR(255) NOT NULL,
  quantity DECIMAL NOT NULL,
  updated_time BIGINT,
  created_time BIGINT,
  buy_price DECIMAL,
  sell_price DECIMAL,
  currency VARCHAR(3),
  CONSTRAINT pk_shopping_cart PRIMARY KEY(user_id,sku)
);

-- Create indexes
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
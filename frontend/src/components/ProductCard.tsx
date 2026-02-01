import React, { FC } from 'react';
import { Link } from 'react-router-dom';
import { Card, Button } from 'react-bootstrap';
import { Product } from '../types';
import './ProductCard.css';

interface ProductCardProps {
  product: Product;
}

const ProductCard: FC<ProductCardProps> = ({ product }) => {
  return (
    <Card className="product-card h-100" data-testid={`product-card-${product.sku}`}>
      <Card.Img
        variant="top"
        src={product.imageUrl}
        alt={product.title}
        className="product-image"
      />
      <Card.Body className="d-flex flex-column">
        <Card.Title className="flex-grow-1">{product.title}</Card.Title>
        
        {product.rating && (
          <div className="rating mb-2">
            <span>{product.rating} ⭐</span>
          </div>
        )}

        <Card.Text className="price mb-3">
          {product.currency} {product.price.toFixed(2)}
        </Card.Text>

        <div className="mt-auto">
          <Link to={`/products/${product.sku}`} className="w-100">
            <Button variant="primary" className="w-100 mb-2" data-testid={`view-button-${product.sku}`}>
              View Details
            </Button>
          </Link>
          <Button
            variant="success"
            className="w-100"
            disabled={!product.inStock}
            data-testid={`add-button-${product.sku}`}
          >
            {product.inStock ? 'Add to Cart' : 'Out of Stock'}
          </Button>
        </div>
      </Card.Body>
    </Card>
  );
};

export default ProductCard;

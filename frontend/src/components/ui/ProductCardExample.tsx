/**
 * Product Card Example
 * Example of how to use the foundational UI components in a product card
 * Based on requirements 3.1, 3.2, 3.3, 3.4
 */

import React, { useState } from 'react';
import { Button, Badge, Skeleton } from './index';
import './ProductCardExample.css';

interface Product {
  id: string;
  title: string;
  brand: string;
  price: number;
  originalPrice?: number;
  image: string;
  rating: number;
  reviewCount: number;
  isNew?: boolean;
  isSale?: boolean;
  salePercentage?: number;
  inStock: boolean;
  colors?: string[];
  sizes?: string[];
}

interface ProductCardExampleProps {
  product?: Product;
  loading?: boolean;
  onAddToCart?: (productId: string) => void;
  onToggleWishlist?: (productId: string) => void;
  onQuickView?: (productId: string) => void;
}

const ProductCardExample: React.FC<ProductCardExampleProps> = ({
  product,
  loading = false,
  onAddToCart,
  onToggleWishlist,
  onQuickView,
}) => {
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  const handleWishlistToggle = () => {
    if (product) {
      setIsWishlisted(!isWishlisted);
      onToggleWishlist?.(product.id);
    }
  };

  const handleAddToCart = () => {
    if (product && product.inStock) {
      onAddToCart?.(product.id);
    }
  };

  const handleQuickView = () => {
    if (product) {
      onQuickView?.(product.id);
    }
  };

  if (loading || !product) {
    return (
      <div className="product-card-example loading">
        <div className="product-image-container">
          <Skeleton variant="rectangular" width="100%" aspectRatio="4/5" />
        </div>
        <div className="product-info">
          <Skeleton variant="text" width="60%" height="14px" />
          <Skeleton variant="text" width="90%" height="16px" />
          <Skeleton variant="text" width="40%" height="18px" />
          <div className="product-rating">
            <Skeleton variant="rectangular" width="80px" height="12px" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div 
      className="product-card-example"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Product Image */}
      <div className="product-image-container">
        {!imageLoaded && (
          <Skeleton 
            variant="rectangular" 
            width="100%" 
            aspectRatio="4/5"
            className="image-skeleton"
          />
        )}
        <img
          src={product.image}
          alt={product.title}
          className={`product-image ${imageLoaded ? 'loaded' : 'loading'}`}
          onLoad={() => setImageLoaded(true)}
          onError={() => setImageLoaded(true)}
        />
        
        {/* Product Badges */}
        <div className="product-badges">
          {product.isNew && (
            <Badge variant="info" size="sm">New</Badge>
          )}
          {product.isSale && product.salePercentage && (
            <Badge variant="sale" size="sm">-{product.salePercentage}%</Badge>
          )}
          {!product.inStock && (
            <Badge variant="error" size="sm">Sold Out</Badge>
          )}
        </div>

        {/* Wishlist Button */}
        <Button
          variant="ghost"
          size="sm"
          className={`wishlist-btn ${isWishlisted ? 'wishlisted' : ''}`}
          onClick={handleWishlistToggle}
          aria-label={isWishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill={isWishlisted ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2">
            <path d="20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
        </Button>

        {/* Quick Actions (shown on hover) */}
        <div className={`quick-actions ${isHovered ? 'visible' : ''}`}>
          <Button
            variant="primary"
            size="sm"
            onClick={handleAddToCart}
            disabled={!product.inStock}
            className="quick-add-btn"
          >
            {product.inStock ? 'Quick Add' : 'Sold Out'}
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={handleQuickView}
            className="quick-view-btn"
          >
            Quick View
          </Button>
        </div>
      </div>

      {/* Product Information */}
      <div className="product-info">
        {/* Brand */}
        <div className="product-brand">{product.brand}</div>
        
        {/* Title */}
        <h3 className="product-title">{product.title}</h3>
        
        {/* Price */}
        <div className="product-price">
          {product.originalPrice && product.originalPrice > product.price && (
            <span className="original-price">${product.originalPrice.toFixed(2)}</span>
          )}
          <span className={`current-price ${product.originalPrice && product.originalPrice > product.price ? 'sale-price' : ''}`}>
            ${product.price.toFixed(2)}
          </span>
        </div>
        
        {/* Rating */}
        <div className="product-rating">
          <div className="stars">
            {Array.from({ length: 5 }, (_, i) => (
              <span key={i} className={`star ${i < Math.floor(product.rating) ? 'filled' : ''}`}>
                ★
              </span>
            ))}
          </div>
          <span className="review-count">({product.reviewCount})</span>
        </div>
        
        {/* Color Options */}
        {product.colors && product.colors.length > 0 && (
          <div className="color-options">
            {product.colors.slice(0, 4).map((color, index) => (
              <div
                key={index}
                className="color-dot"
                style={{ backgroundColor: color }}
                title={color}
              />
            ))}
            {product.colors.length > 4 && (
              <span className="more-colors">+{product.colors.length - 4}</span>
            )}
          </div>
        )}
        
        {/* Size Information */}
        {product.sizes && product.sizes.length > 0 && (
          <div className="size-info">
            <span className="size-label">Sizes:</span>
            <span className="size-range">
              {product.sizes[0]} - {product.sizes[product.sizes.length - 1]}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductCardExample;
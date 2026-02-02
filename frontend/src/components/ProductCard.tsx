import { FC, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Product } from '../types';
import { useCartContext } from '../context/CartContext';
import './ProductCard.css';

interface ProductCardProps {
  product: Product;
  variant?: 'default' | 'compact' | 'featured';
  showQuickActions?: boolean;
  onAddToCart?: (product: Product) => void;
  onToggleWishlist?: (productId: string) => void;
  onQuickView?: (product: Product) => void;
}

const ProductCard: FC<ProductCardProps> = ({ 
  product, 
  variant = 'default',
  showQuickActions = true,
  onAddToCart,
  onToggleWishlist,
  onQuickView 
}) => {
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [imageError, setImageError] = useState(false);
  const [imageLoading, setImageLoading] = useState(true);
  const { addToCart } = useCartContext();
  const navigate = useNavigate();
  
  // Enhanced product data with better logic
  const hasDiscount = product.price < (product.price * 1.3); // More realistic discount logic
  const originalPrice = hasDiscount ? Math.round(product.price * 1.3 * 100) / 100 : null;
  const discountPercentage = originalPrice ? Math.round(((originalPrice - product.price) / originalPrice) * 100) : 0;
  
  // Mock enhanced product features - in real app these would come from product data
  const availableColors = ['#000000', '#8B4513', '#4169E1', '#DC143C'].slice(0, Math.floor(Math.random() * 3) + 1);
  const availableSizes = ['XS', 'S', 'M', 'L', 'XL'].slice(0, Math.floor(Math.random() * 4) + 2);
  const brandName = product.title.split(' ')[0]; // Extract brand from title
  const isBestseller = product.rating && product.rating >= 4.5;

  // Fallback placeholder image
  const getPlaceholderImage = () => {
    // Use a more reliable placeholder service with better error handling
    const colors = ['4A90E2', '7ED321', 'F5A623', 'D0021B', '9013FE', '50E3C2'];
    const bgColor = colors[Math.abs(product.sku.charCodeAt(0)) % colors.length];
    const cleanTitle = encodeURIComponent(product.title.substring(0, 15).replace(/[^a-zA-Z0-9\s]/g, ''));
    return `https://via.placeholder.com/400x500/${bgColor}/FFFFFF?text=${cleanTitle}`;
  };

  // Log the image URL being used for debugging
  console.log(`Product ${product.sku} using image URL:`, product.imageUrl);

  const handleImageError = () => {
    console.log('Image failed to load, using fallback for:', product.title);
    setImageError(true);
    setImageLoading(false);
  };

  const handleImageLoad = () => {
    console.log('Image loaded successfully for:', product.title);
    setImageError(false);
    setImageLoading(false);
  };

  const handleWishlistToggle = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    const newWishlistState = !isWishlisted;
    setIsWishlisted(newWishlistState);
    onToggleWishlist?.(product.sku);
  }, [isWishlisted, onToggleWishlist, product.sku]);

  const handleQuickAdd = useCallback(async (e: React.MouseEvent) => {
    console.log('Add to cart button clicked for product:', product.sku);
    e.preventDefault();
    e.stopPropagation();
    
    if (!product.inStock) {
      console.log('Product is out of stock, not adding to cart');
      return;
    }
    
    setIsLoading(true);
    try {
      console.log('Calling addToCart with product:', product);
      await addToCart(product, 1);
      console.log('Product added to cart successfully:', product.title);
      onAddToCart?.(product);
    } catch (error) {
      console.error('Error adding to cart:', error);
    } finally {
      setIsLoading(false);
    }
  }, [product, addToCart, onAddToCart]);

  const handleQuickView = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    onQuickView?.(product);
  }, [onQuickView, product]);

  const handleCardClick = useCallback((e: React.MouseEvent) => {
    // Don't navigate if clicking on action buttons or if loading
    const target = e.target as HTMLElement;
    if (target.closest('.action-buttons') || target.closest('.wishlist-btn') || isLoading) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }
    
    // Allow navigation to product detail page
    navigate(`/products/${product.sku}`);
  }, [navigate, product.sku, isLoading]);

  const handleMouseEnter = useCallback(() => {
    setIsHovered(true);
  }, []);

  const handleMouseLeave = useCallback(() => {
    setIsHovered(false);
  }, []);

  return (
    <div 
      className={`product-card ${variant} ${isHovered ? 'hovered' : ''} ${!product.inStock ? 'out-of-stock' : ''}`}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      onClick={handleCardClick}
      data-testid={`product-card-${product.sku}`}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleCardClick(e as any);
        }
      }}
      aria-label={`View details for ${product.title}`}>
      {/* Product Image Container */}
      <div className="product-image-container">
        {imageLoading && !imageError && (
          <div className="image-loading-placeholder">
            <div className="loading-spinner"></div>
          </div>
        )}
        <img
          src={imageError ? getPlaceholderImage() : product.imageUrl}
          alt={product.title}
          className={`product-image ${imageLoading ? 'loading' : ''}`}
          onError={handleImageError}
          onLoad={handleImageLoad}
          loading="lazy"
        />
        
        {/* Wishlist Button */}
        <button 
          className={`wishlist-btn ${isWishlisted ? 'active' : ''}`}
          onClick={handleWishlistToggle}
          aria-label={isWishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
          data-testid={`wishlist-button-${product.sku}`}
        >
          <svg 
            width="16" 
            height="16" 
            viewBox="0 0 24 24" 
            fill={isWishlisted ? 'currentColor' : 'none'} 
            stroke="currentColor" 
            strokeWidth="2"
          >
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        </button>

        {/* Status Badges */}
        {!product.inStock && (
          <div className="status-badge sold-out">
            Sold Out
          </div>
        )}

        {isBestseller && product.inStock && (
          <div className="status-badge bestseller">
            Bestseller
          </div>
        )}

        {hasDiscount && product.inStock && (
          <div className="status-badge discount">
            -{discountPercentage}%
          </div>
        )}

        {/* Quick Action Buttons - Show on Hover */}
        {showQuickActions && (
          <div className={`action-buttons ${isHovered ? 'visible' : ''}`}>
            <button 
              className="action-btn view-details-btn" 
              onClick={handleQuickView}
              data-testid={`view-button-${product.sku}`}
              aria-label="Quick view product details"
            >
              View Details
            </button>
            <button
              className={`action-btn quick-add-btn ${isLoading ? 'loading' : ''}`}
              disabled={!product.inStock || isLoading}
              onClick={handleQuickAdd}
              data-testid={`add-button-${product.sku}`}
              aria-label="Quick add to cart"
            >
              {isLoading ? 'Adding...' : product.inStock ? 'Quick Add' : 'Sold Out'}
            </button>
          </div>
        )}
      </div>
    
      {/* Product Information */}
      <div className="product-info">
        {/* Brand Name */}
        <div className="brand-name">{brandName}</div>
        
        {/* Product Title */}
        <h3 className="product-title">{product.title}</h3>
        
        {/* Rating */}
        {product.rating && (
          <div className="rating-section">
            <div className="stars" aria-label={`Rating: ${product.rating} out of 5 stars`}>
              {[...Array(5)].map((_, i) => (
                <span 
                  key={i} 
                  className={`star ${i < Math.floor(product.rating!) ? 'filled' : ''}`}
                  aria-hidden="true"
                >
                  ★
                </span>
              ))}
            </div>
            <span className="rating-text">
              ({product.reviews || Math.floor(Math.random() * 500) + 10})
            </span>
          </div>
        )}

        {/* Available Colors */}
        {availableColors.length > 1 && (
          <div className="colors-available" aria-label="Available colors">
            {availableColors.map((color, index) => (
              <div 
                key={index}
                className={`color-dot ${index === 0 ? 'active' : ''}`}
                style={{ backgroundColor: color }}
                aria-label={`Color option ${index + 1}`}
                role="button"
                tabIndex={0}
              />
            ))}
          </div>
        )}

        {/* Available Sizes */}
        {availableSizes.length > 0 && (
          <div className="sizes-available">
            Sizes: {availableSizes.join(', ')}
          </div>
        )}

        {/* Price Section */}
        <div className="price-section">
          <div className="price-container">
            {originalPrice && (
              <span className="original-price" aria-label="Original price">
                {product.currency} {originalPrice.toFixed(2)}
              </span>
            )}
            <span className="current-price" aria-label="Current price">
              {product.currency} {product.price.toFixed(2)}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;

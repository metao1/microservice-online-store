import React, { FC, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useProduct } from '@hooks/useProducts';
import { useCartContext } from '@context/CartContext';
import './ProductDetailPage.css';

const ProductDetailPage: FC = () => {
  const { sku } = useParams<{ sku: string }>();
  const { product, loading, error } = useProduct(sku!);
  const { addToCart } = useCartContext();
  const [quantity, setQuantity] = useState(1);
  const [selectedSize, setSelectedSize] = useState('');
  const [selectedColor, setSelectedColor] = useState(0);
  const [addedToCart, setAddedToCart] = useState(false);
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [activeImageIndex, setActiveImageIndex] = useState(0);

  // Mock data for enhanced product details (in real app, this would come from API)
  const availableSizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];
  const availableColors = [
    { name: 'Black', value: '#000000', image: product?.imageUrl },
    { name: 'Navy', value: '#1a237e', image: product?.imageUrl },
    { name: 'Gray', value: '#757575', image: product?.imageUrl },
  ];
  const productImages = [
    product?.imageUrl,
    product?.imageUrl,
    product?.imageUrl,
    product?.imageUrl,
  ].filter(Boolean);

  const handleAddToCart = async () => {
    if (product && selectedSize) {
      try {
        await addToCart(product, quantity);
        setAddedToCart(true);
        setTimeout(() => setAddedToCart(false), 3000);
      } catch (error) {
        console.error('Failed to add to cart:', error);
      }
    }
  };

  const handleWishlistToggle = () => {
    setIsWishlisted(!isWishlisted);
  };

  if (loading) {
    return (
      <div className="product-detail-loading">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading product details...</p>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="product-detail-error">
        <div className="error-container">
          <h2>Product Not Found</h2>
          <p>{error || `The product with SKU "${sku}" could not be found.`}</p>
          <Link to="/products" className="back-to-products-btn">
            Back to Products
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="product-detail-page">
      {/* Breadcrumb Navigation */}
      <div className="breadcrumb-container">
        <nav className="breadcrumb-nav">
          <Link to="/" className="breadcrumb-link">Home</Link>
          <span className="breadcrumb-separator">/</span>
          <Link to="/products" className="breadcrumb-link">Products</Link>
          <span className="breadcrumb-separator">/</span>
          <span className="breadcrumb-current">{product.title}</span>
        </nav>
      </div>

      {/* Success Message */}
      {addedToCart && (
        <div className="success-banner">
          <div className="success-content">
            <span className="success-icon">✓</span>
            <span>Added to bag</span>
            <Link to="/cart" className="view-bag-link">View bag</Link>
          </div>
        </div>
      )}

      <div className="product-detail-container">
        {/* Product Images Section */}
        <div className="product-images-section">
          <div className="image-thumbnails">
            {productImages.map((image, index) => (
              <button
                key={index}
                className={`thumbnail ${index === activeImageIndex ? 'active' : ''}`}
                onClick={() => setActiveImageIndex(index)}
              >
                <img src={image} alt={`${product.title} view ${index + 1}`} />
              </button>
            ))}
          </div>
          <div className="main-image-container">
            <img
              src={productImages[activeImageIndex]}
              alt={product.title}
              className="main-product-image"
              data-testid="product-image"
            />
            <button
              className={`wishlist-button ${isWishlisted ? 'active' : ''}`}
              onClick={handleWishlistToggle}
              aria-label={isWishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill={isWishlisted ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
              </svg>
            </button>
          </div>
        </div>

        {/* Product Information Section */}
        <div className="product-info-section">
          <div className="product-header">
            <div className="brand-name">ModernStore</div>
            <h1 className="product-title" data-testid="product-title">
              {product.title}
            </h1>
            
            {product.rating && (
              <div className="rating-section">
                <div className="stars">
                  {[...Array(5)].map((_, i) => (
                    <span 
                      key={i} 
                      className={`star ${i < Math.floor(product.rating!) ? 'filled' : ''}`}
                    >
                      ★
                    </span>
                  ))}
                </div>
                <span className="rating-value">{product.rating}</span>
                {product.reviews && (
                  <span className="reviews-count">({product.reviews} reviews)</span>
                )}
              </div>
            )}
          </div>

          <div className="price-section">
            <div className="current-price" data-testid="product-price">
              {product.currency} {product.price.toFixed(2)}
            </div>
            <div className="price-note">VAT included</div>
          </div>

          {/* Color Selection */}
          <div className="selection-section">
            <div className="color-selection">
              <label className="selection-label">
                Colour: <span className="selected-value">{availableColors[selectedColor].name}</span>
              </label>
              <div className="color-options">
                {availableColors.map((color, index) => (
                  <button
                    key={index}
                    className={`color-option ${index === selectedColor ? 'selected' : ''}`}
                    style={{ backgroundColor: color.value }}
                    onClick={() => setSelectedColor(index)}
                    aria-label={`Select ${color.name} color`}
                  />
                ))}
              </div>
            </div>

            {/* Size Selection */}
            <div className="size-selection">
              <div className="size-header">
                <label className="selection-label">Choose your size</label>
                <button className="size-guide-btn">Size guide</button>
              </div>
              <div className="size-options">
                {availableSizes.map((size) => (
                  <button
                    key={size}
                    className={`size-option ${selectedSize === size ? 'selected' : ''}`}
                    onClick={() => setSelectedSize(size)}
                  >
                    {size}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Add to Cart Section */}
          <div className="add-to-cart-section">
            <div className="quantity-selector">
              <button 
                className="quantity-btn"
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                disabled={quantity <= 1}
              >
                -
              </button>
              <span className="quantity-display">{quantity}</span>
              <button 
                className="quantity-btn"
                onClick={() => setQuantity(Math.min((product.quantity || 99), quantity + 1))}
                disabled={quantity >= (product.quantity || 99)}
              >
                +
              </button>
            </div>

            <button
              className={`add-to-bag-btn ${!selectedSize ? 'disabled' : ''}`}
              onClick={handleAddToCart}
              disabled={!product.inStock || !selectedSize}
              data-testid="add-to-cart-button"
            >
              {!selectedSize ? 'Please select a size' : 'Add to bag'}
            </button>
          </div>

          {/* Stock Information */}
          <div className="stock-info">
            {product.inStock ? (
              <div className="in-stock">
                <span className="stock-icon">✓</span>
                <span>In stock - Ready to ship</span>
              </div>
            ) : (
              <div className="out-of-stock">
                <span className="stock-icon">✗</span>
                <span>Currently out of stock</span>
              </div>
            )}
          </div>

          {/* Product Details */}
          <div className="product-details">
            <div className="details-section">
              <h3>Product details</h3>
              <p className="product-description">{product.description}</p>
            </div>

            <div className="details-section">
              <h3>Delivery & returns</h3>
              <ul className="delivery-info">
                <li>Free delivery on orders over €50</li>
                <li>Standard delivery: 2-4 working days</li>
                <li>Express delivery: Next working day</li>
                <li>Free returns within 100 days</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;

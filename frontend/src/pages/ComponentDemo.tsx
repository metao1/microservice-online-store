/**
 * Component Demo Page
 * Demonstrates the foundational UI components
 * Based on requirements 1.5, 3.2, 8.2
 */

import React, { useState } from 'react';
import { Button, Input, Badge, Skeleton, SkeletonProductCard } from '../components/ui';
import ProductCardExample from '../components/ui/ProductCardExample';
import './ComponentDemo.css';

// Demo icons (simple SVG icons)
const SearchIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="11" cy="11" r="8"/>
    <path d="21 21l-4.35-4.35"/>
  </svg>
);

const HeartIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
  </svg>
);

const ShoppingCartIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="9" cy="21" r="1"/>
    <circle cx="20" cy="21" r="1"/>
    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
  </svg>
);

const ComponentDemo: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [cartCount, setCartCount] = useState(3);
  const [inputValue, setInputValue] = useState('');
  const [showSkeletons, setShowSkeletons] = useState(false);

  const handleLoadingDemo = () => {
    setLoading(true);
    setTimeout(() => setLoading(false), 2000);
  };

  const handleAddToCart = () => {
    setCartCount(prev => prev + 1);
  };

  const handleSkeletonToggle = () => {
    setShowSkeletons(!showSkeletons);
  };

  return (
    <div className="component-demo">
      <div className="demo-container">
        <header className="demo-header">
          <h1>UI Components Demo</h1>
          <p>Foundational UI components for the e-commerce redesign</p>
        </header>

        {/* Button Component Demo */}
        <section className="demo-section">
          <h2>Button Component</h2>
          <div className="demo-grid">
            <div className="demo-item">
              <h3>Variants</h3>
              <div className="button-group">
                <Button variant="primary">Primary</Button>
                <Button variant="secondary">Secondary</Button>
                <Button variant="ghost">Ghost</Button>
                <Button variant="outline">Outline</Button>
              </div>
            </div>

            <div className="demo-item">
              <h3>Sizes</h3>
              <div className="button-group">
                <Button size="sm">Small</Button>
                <Button size="md">Medium</Button>
                <Button size="lg">Large</Button>
              </div>
            </div>

            <div className="demo-item">
              <h3>With Icons</h3>
              <div className="button-group">
                <Button startIcon={<HeartIcon />}>Add to Wishlist</Button>
                <Button endIcon={<ShoppingCartIcon />} onClick={handleAddToCart}>
                  Add to Cart
                </Button>
              </div>
            </div>

            <div className="demo-item">
              <h3>States</h3>
              <div className="button-group">
                <Button loading={loading} onClick={handleLoadingDemo}>
                  {loading ? 'Loading...' : 'Click to Load'}
                </Button>
                <Button disabled>Disabled</Button>
                <Button fullWidth>Full Width Button</Button>
              </div>
            </div>
          </div>
        </section>

        {/* Input Component Demo */}
        <section className="demo-section">
          <h2>Input Component</h2>
          <div className="demo-grid">
            <div className="demo-item">
              <h3>Basic Inputs</h3>
              <div className="input-group">
                <Input 
                  label="Default Input"
                  placeholder="Enter text..."
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                />
                <Input 
                  label="Search Input"
                  variant="search"
                  placeholder="Search products..."
                  startIcon={<SearchIcon />}
                />
                <Input 
                  label="Filter Input"
                  variant="filter"
                  placeholder="Filter by..."
                />
              </div>
            </div>

            <div className="demo-item">
              <h3>Sizes</h3>
              <div className="input-group">
                <Input size="sm" placeholder="Small input" />
                <Input size="md" placeholder="Medium input" />
                <Input size="lg" placeholder="Large input" />
              </div>
            </div>

            <div className="demo-item">
              <h3>States</h3>
              <div className="input-group">
                <Input 
                  label="Error State"
                  error 
                  errorMessage="This field is required"
                  placeholder="Invalid input"
                />
                <Input 
                  label="With Helper Text"
                  helperText="Enter a valid email address"
                  placeholder="user@example.com"
                />
                <Input 
                  label="Loading State"
                  loading
                  placeholder="Loading..."
                />
              </div>
            </div>
          </div>
        </section>

        {/* Badge Component Demo */}
        <section className="demo-section">
          <h2>Badge Component</h2>
          <div className="demo-grid">
            <div className="demo-item">
              <h3>Variants</h3>
              <div className="badge-group">
                <Badge variant="default">Default</Badge>
                <Badge variant="primary">Primary</Badge>
                <Badge variant="secondary">Secondary</Badge>
                <Badge variant="success">Success</Badge>
                <Badge variant="error">Error</Badge>
                <Badge variant="warning">Warning</Badge>
                <Badge variant="info">Info</Badge>
                <Badge variant="sale">SALE</Badge>
              </div>
            </div>

            <div className="demo-item">
              <h3>Sizes</h3>
              <div className="badge-group">
                <Badge size="sm">Small</Badge>
                <Badge size="md">Medium</Badge>
                <Badge size="lg">Large</Badge>
              </div>
            </div>

            <div className="demo-item">
              <h3>Count Badges</h3>
              <div className="badge-group">
                <div className="badge-demo-item">
                  <ShoppingCartIcon />
                  <Badge variant="primary" count={cartCount} />
                </div>
                <Badge count={0} showZero>0</Badge>
                <Badge count={99}>99</Badge>
                <Badge count={150}>150 (shows 99+)</Badge>
              </div>
            </div>

            <div className="demo-item">
              <h3>Dot Badges</h3>
              <div className="badge-group">
                <div className="badge-demo-item">
                  <span>Notification</span>
                  <Badge dot variant="error" />
                </div>
                <div className="badge-demo-item">
                  <span>Online Status</span>
                  <Badge dot variant="success" />
                </div>
              </div>
            </div>

            <div className="demo-item">
              <h3>E-commerce Use Cases</h3>
              <div className="badge-group">
                <Badge variant="sale">-20%</Badge>
                <Badge variant="success">In Stock</Badge>
                <Badge variant="error">Out of Stock</Badge>
                <Badge variant="warning">Low Stock</Badge>
                <Badge variant="info">New</Badge>
              </div>
            </div>
          </div>
        </section>

        {/* Skeleton Component Demo */}
        <section className="demo-section">
          <h2>Skeleton Component</h2>
          <div className="demo-grid">
            <div className="demo-item">
              <h3>Basic Skeletons</h3>
              <div className="skeleton-group">
                <Skeleton variant="text" width="80%" />
                <Skeleton variant="text" width="60%" />
                <Skeleton variant="rectangular" width="100%" height="200px" />
                <Skeleton variant="circular" width="50px" height="50px" />
                <Skeleton variant="rounded" width="120px" height="40px" />
              </div>
            </div>

            <div className="demo-item">
              <h3>Multi-line Text</h3>
              <div className="skeleton-group">
                <Skeleton variant="text" lines={3} />
              </div>
            </div>

            <div className="demo-item">
              <h3>Animation Types</h3>
              <div className="skeleton-group">
                <Skeleton animation="pulse" width="100%" height="20px" />
                <Skeleton animation="wave" width="100%" height="20px" />
                <Skeleton animation="none" width="100%" height="20px" />
              </div>
            </div>

            <div className="demo-item">
              <h3>Product Card Skeleton</h3>
              <div className="skeleton-demo-toggle">
                <Button onClick={handleSkeletonToggle}>
                  {showSkeletons ? 'Hide' : 'Show'} Product Skeletons
                </Button>
              </div>
              {showSkeletons && (
                <div className="product-skeleton-grid">
                  <SkeletonProductCard />
                  <SkeletonProductCard />
                  <SkeletonProductCard />
                  <SkeletonProductCard />
                </div>
              )}
            </div>
          </div>
        </section>

        {/* Integration Demo */}
        <section className="demo-section">
          <h2>Integration Example</h2>
          <div className="integration-demo">
            <div className="demo-product-card">
              <div className="product-image-placeholder">
                <Skeleton variant="rectangular" width="100%" aspectRatio="4/5" />
              </div>
              <div className="product-info">
                <div className="product-badges">
                  <Badge variant="sale">-25%</Badge>
                  <Badge variant="info">New</Badge>
                </div>
                <h3>Product Title</h3>
                <p className="product-price">
                  <span className="original-price">$99.99</span>
                  <span className="sale-price">$74.99</span>
                </p>
                <div className="product-actions">
                  <Button 
                    variant="primary" 
                    startIcon={<ShoppingCartIcon />}
                    onClick={handleAddToCart}
                  >
                    Add to Cart
                  </Button>
                  <Button variant="ghost" startIcon={<HeartIcon />}>
                    <Badge variant="primary" count={cartCount} />
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Product Card Example */}
        <section className="demo-section">
          <h2>Complete Product Card Example</h2>
          <p>This example shows how all the foundational UI components work together in a real product card:</p>
          <div className="product-card-demo">
            <div className="product-card-grid">
              <ProductCardExample
                product={{
                  id: '1',
                  title: 'Premium Wireless Headphones',
                  brand: 'AudioTech',
                  price: 149.99,
                  originalPrice: 199.99,
                  image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=500&fit=crop',
                  rating: 4.5,
                  reviewCount: 128,
                  isNew: false,
                  isSale: true,
                  salePercentage: 25,
                  inStock: true,
                  colors: ['#000000', '#FFFFFF', '#FF0000', '#0000FF', '#00FF00'],
                  sizes: ['S', 'M', 'L', 'XL']
                }}
                onAddToCart={(id) => {
                  handleAddToCart();
                  console.log('Added to cart:', id);
                }}
                onToggleWishlist={(id) => console.log('Toggled wishlist:', id)}
                onQuickView={(id) => console.log('Quick view:', id)}
              />
              
              <ProductCardExample
                product={{
                  id: '2',
                  title: 'Organic Cotton T-Shirt',
                  brand: 'EcoWear',
                  price: 29.99,
                  image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=500&fit=crop',
                  rating: 4.8,
                  reviewCount: 89,
                  isNew: true,
                  isSale: false,
                  inStock: true,
                  colors: ['#000000', '#FFFFFF', '#808080'],
                  sizes: ['XS', 'S', 'M', 'L', 'XL', 'XXL']
                }}
                onAddToCart={(id) => {
                  handleAddToCart();
                  console.log('Added to cart:', id);
                }}
                onToggleWishlist={(id) => console.log('Toggled wishlist:', id)}
                onQuickView={(id) => console.log('Quick view:', id)}
              />
              
              <ProductCardExample
                product={{
                  id: '3',
                  title: 'Vintage Leather Jacket',
                  brand: 'ClassicStyle',
                  price: 299.99,
                  image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=500&fit=crop',
                  rating: 4.2,
                  reviewCount: 45,
                  isNew: false,
                  isSale: false,
                  inStock: false,
                  colors: ['#8B4513', '#000000'],
                  sizes: ['S', 'M', 'L']
                }}
                onAddToCart={(id) => console.log('Added to cart:', id)}
                onToggleWishlist={(id) => console.log('Toggled wishlist:', id)}
                onQuickView={(id) => console.log('Quick view:', id)}
              />
              
              <ProductCardExample loading />
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default ComponentDemo;
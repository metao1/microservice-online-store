import { FC } from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';

const HomePage: FC = () => {
  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section" data-testid="hero-section">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">
              Discover Your
              <span className="hero-title-accent"> Perfect Style</span>
            </h1>
            <p className="hero-description">
              Curated fashion for the modern individual. Explore our latest collection 
              of premium clothing and accessories designed to elevate your wardrobe.
            </p>
            <div className="hero-actions">
              <Link to="/products" className="btn-primary" data-testid="shop-now-button">
                Shop Collection
              </Link>
              <Link to="/products?category=sale" className="btn-secondary">
                View Sale
              </Link>
            </div>
          </div>
          <div className="hero-image">
            <div className="hero-image-container">
              <img 
                src="https://images.unsplash.com/photo-1441986300917-64674bd600d8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
                alt="Fashion Collection" 
                className="hero-img"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="features-container">
          <div className="features-grid">
            <div className="feature-item">
              <div className="feature-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                </svg>
              </div>
              <h3 className="feature-title">Fast Delivery</h3>
              <p className="feature-description">
                Free shipping on orders over €50. Express delivery available.
              </p>
            </div>
            
            <div className="feature-item">
              <div className="feature-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                  <path d="M12 17h.01"/>
                </svg>
              </div>
              <h3 className="feature-title">Premium Quality</h3>
              <p className="feature-description">
                Carefully selected materials and craftsmanship in every piece.
              </p>
            </div>
            
            <div className="feature-item">
              <div className="feature-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M9 12l2 2 4-4"/>
                  <path d="M21 12c-1 0-3-1-3-3s2-3 3-3 3 1 3 3-2 3-3 3"/>
                  <path d="M3 12c1 0 3-1 3-3s-2-3-3-3-3 1-3 3 2 3 3 3"/>
                  <path d="M13 12h3"/>
                  <path d="M8 12H5"/>
                </svg>
              </div>
              <h3 className="feature-title">Easy Returns</h3>
              <p className="feature-description">
                100-day return policy. Free returns on all orders.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="categories-section">
        <div className="categories-container">
          <h2 className="section-title">Shop by Category</h2>
          <div className="categories-grid">
            <Link to="/products?category=clothing" className="category-card">
              <div className="category-image">
                <img 
                  src="https://images.unsplash.com/photo-1445205170230-053b83016050?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" 
                  alt="Women's Clothing"
                />
              </div>
              <div className="category-content">
                <h3>Women's Clothing</h3>
                <p>Discover the latest trends</p>
              </div>
            </Link>
            
            <Link to="/products?category=accessories" className="category-card">
              <div className="category-image">
                <img 
                  src="https://images.unsplash.com/photo-1553062407-98eeb64c6a62?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" 
                  alt="Accessories"
                />
              </div>
              <div className="category-content">
                <h3>Accessories</h3>
                <p>Complete your look</p>
              </div>
            </Link>
            
            <Link to="/products?category=shoes" className="category-card">
              <div className="category-image">
                <img 
                  src="https://images.unsplash.com/photo-1549298916-b41d501d3772?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" 
                  alt="Shoes"
                />
              </div>
              <div className="category-content">
                <h3>Shoes</h3>
                <p>Step up your style</p>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* Newsletter Section */}
      <section className="newsletter-section">
        <div className="newsletter-container">
          <div className="newsletter-content">
            <h2 className="newsletter-title">Stay in Style</h2>
            <p className="newsletter-description">
              Subscribe to our newsletter and be the first to know about new arrivals, 
              exclusive offers, and style tips.
            </p>
            <div className="newsletter-form">
              <input 
                type="email" 
                placeholder="Enter your email address" 
                className="newsletter-input"
              />
              <button className="newsletter-button">Subscribe</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;

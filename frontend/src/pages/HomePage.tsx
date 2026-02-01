import React, { FC } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import './HomePage.css';

const HomePage: FC = () => {
  return (
    <Container fluid className="home-page">
      {/* Hero Section */}
      <section className="hero-section py-5 bg-light" data-testid="hero-section">
        <Container className="py-5">
          <Row>
            <Col md={6} className="d-flex flex-column justify-content-center">
              <h1 className="display-4 fw-bold mb-4">Welcome to Our Store</h1>
              <p className="lead mb-4">
                Discover amazing products at unbeatable prices. Shop now and enjoy fast delivery!
              </p>
              <div>
                <Link to="/products">
                  <Button variant="primary" size="lg" data-testid="shop-now-button">
                    Shop Now
                  </Button>
                </Link>
              </div>
            </Col>
            <Col md={6}>
              <div 
                className="hero-image-placeholder d-flex align-items-center justify-content-center bg-secondary text-white"
                style={{ height: '400px', borderRadius: '8px' }}
              >
                <h3>Hero Image</h3>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

      {/* Features Section */}
      <section className="features-section py-5">
        <Container>
          <h2 className="text-center mb-5">Why Shop With Us?</h2>
          <Row>
            <Col md={4} className="mb-4">
              <div className="feature-card p-4">
                <h4>Fast Delivery</h4>
                <p>Get your orders delivered quickly and safely.</p>
              </div>
            </Col>
            <Col md={4} className="mb-4">
              <div className="feature-card p-4">
                <h4>Best Prices</h4>
                <p>Competitive pricing on all products.</p>
              </div>
            </Col>
            <Col md={4} className="mb-4">
              <div className="feature-card p-4">
                <h4>24/7 Support</h4>
                <p>Customer support available round the clock.</p>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

      {/* CTA Section */}
      <section className="cta-section py-5 bg-primary text-white">
        <Container className="text-center">
          <h2 className="mb-4">Ready to Shop?</h2>
          <Link to="/products">
            <Button variant="light" size="lg" data-testid="browse-products-button">
              Browse Products
            </Button>
          </Link>
        </Container>
      </section>
    </Container>
  );
};

export default HomePage;

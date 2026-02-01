import React, { FC } from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import './Footer.css';

const Footer: FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-dark text-white py-5 mt-5">
      <Container>
        <Row>
          <Col md={4} className="mb-4">
            <h5>About Us</h5>
            <p>
              Your trusted online store for quality products and exceptional customer service.
            </p>
          </Col>
          <Col md={4} className="mb-4">
            <h5>Quick Links</h5>
            <ul className="list-unstyled">
              <li><a href="/" className="text-decoration-none text-white">Home</a></li>
              <li><a href="/products" className="text-decoration-none text-white">Products</a></li>
              <li><a href="/cart" className="text-decoration-none text-white">Cart</a></li>
            </ul>
          </Col>
          <Col md={4} className="mb-4">
            <h5>Contact</h5>
            <p>Email: support@onlinestore.com</p>
            <p>Phone: +1 (555) 123-4567</p>
          </Col>
        </Row>
        <Row>
          <Col className="text-center border-top pt-3">
            <p>&copy; {currentYear} Online Store. All rights reserved.</p>
          </Col>
        </Row>
      </Container>
    </footer>
  );
};

export default Footer;

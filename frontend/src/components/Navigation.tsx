import React, { FC } from 'react';
import { Navbar, Nav, Container, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useCartContext } from '../context/CartContext';
import './Navigation.css';

const Navigation: FC = () => {
  const { getCartItemCount } = useCartContext();
  const itemCount = getCartItemCount();

  return (
    <Navbar bg="dark" expand="lg" sticky="top" data-testid="navbar">
      <Container>
        <Navbar.Brand as={Link} to="/" className="fw-bold" data-testid="brand-logo">
          Online Store
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto">
            <Nav.Link as={Link} to="/" data-testid="home-link">
              Home
            </Nav.Link>
            <Nav.Link as={Link} to="/products" data-testid="products-link">
              Products
            </Nav.Link>
            <Nav.Link as={Link} to="/cart" data-testid="cart-link">
              Cart
              {itemCount > 0 && (
                <Badge bg="danger" className="ms-2" data-testid="cart-badge">
                  {itemCount}
                </Badge>
              )}
            </Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;

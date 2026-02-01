import React, { FC } from 'react';
import { useParams } from 'react-router-dom';
import { useProduct } from '../hooks/useProducts';
import { useCartContext } from '../context/CartContext';
import { Container, Row, Col, Button, Spinner, Alert } from 'react-bootstrap';
import './ProductDetailPage.css';

const ProductDetailPage: FC = () => {
  const { id } = useParams<{ id: string }>();
  const { product, loading, error } = useProduct(id!);
  const { addToCart } = useCartContext();
  const [quantity, setQuantity] = React.useState(1);
  const [addedToCart, setAddedToCart] = React.useState(false);

  const handleAddToCart = async () => {
    if (product) {
      await addToCart(product, quantity);
      setAddedToCart(true);
      setTimeout(() => setAddedToCart(false), 3000);
    }
  };

  if (loading) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
      </Container>
    );
  }

  if (error || !product) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          {error || 'Product not found'}
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="product-detail-page py-5">
      {addedToCart && (
        <Alert variant="success" dismissible onClose={() => setAddedToCart(false)}>
          Product added to cart!
        </Alert>
      )}

      <Row>
        <Col md={6}>
          <img
            src={product.image}
            alt={product.title}
            className="img-fluid"
            data-testid="product-image"
          />
        </Col>
        <Col md={6}>
          <h1 data-testid="product-title">{product.title}</h1>
          
          {product.rating && (
            <div className="rating mb-3">
              <span>{product.rating} ⭐</span>
              {product.reviews && <span> ({product.reviews} reviews)</span>}
            </div>
          )}

          <h3 className="price mb-4" data-testid="product-price">
            {product.currency} {product.price.toFixed(2)}
          </h3>

          <p className="description mb-4">{product.description}</p>

          {product.inStock ? (
            <div className="add-to-cart-section">
              <div className="quantity-selector mb-3">
                <label htmlFor="quantity">Quantity:</label>
                <input
                  id="quantity"
                  type="number"
                  min="1"
                  max="99"
                  value={quantity}
                  onChange={(e) => setQuantity(parseInt(e.target.value))}
                  data-testid="quantity-selector"
                />
              </div>
              <Button
                variant="primary"
                size="lg"
                onClick={handleAddToCart}
                data-testid="add-to-cart-button"
              >
                Add to Cart
              </Button>
            </div>
          ) : (
            <Alert variant="warning">Out of Stock</Alert>
          )}
        </Col>
      </Row>
    </Container>
  );
};

export default ProductDetailPage;

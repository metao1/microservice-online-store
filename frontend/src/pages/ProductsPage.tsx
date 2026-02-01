import React, { FC, useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import { Container, Row, Col, Spinner, Alert } from 'react-bootstrap';
import ProductCard from '../components/ProductCard';
import './ProductsPage.css';

interface ProductsPageProps {
  category?: string;
}

const ProductsPage: FC<ProductsPageProps> = ({ category }) => {
  const { products, loading, error, fetchProducts, searchProducts } = useProducts();
  const [searchQuery, setSearchQuery] = useState('');
  const [limit] = useState(12);
  const [offset, setOffset] = useState(0);

  React.useEffect(() => {
    // Default to 'books' category if no category is specified
    const defaultCategory = category || 'books';
    fetchProducts(defaultCategory, limit, offset);
  }, [category, fetchProducts, limit, offset]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      await searchProducts(searchQuery);
    } else {
      // Default to 'books' category if no category is specified
      const defaultCategory = category || 'books';
      await fetchProducts(defaultCategory, limit, 0);
    }
  };

  return (
    <Container className="products-page py-5">
      <div className="search-bar mb-4">
        <form onSubmit={handleSearch}>
          <div className="input-group">
            <input
              type="text"
              className="form-control"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              data-testid="search-input"
            />
            <button className="btn btn-primary" type="submit" data-testid="search-button">
              Search
            </button>
          </div>
        </form>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      {loading ? (
        <div className="text-center">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      ) : (
        <Row className="g-4">
          {products.map((product) => (
            <Col key={product.sku} xs={12} sm={6} md={4} lg={3}>
              <ProductCard product={product} />
            </Col>
          ))}
        </Row>
      )}

      {!loading && products.length === 0 && (
        <Alert variant="info">No products found</Alert>
      )}
    </Container>
  );
};

export default ProductsPage;

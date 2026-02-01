import React, { FC } from 'react';
import { useCartContext } from '../context/CartContext';
import { Container, Row, Col, Table, Button, Alert } from 'react-bootstrap';
import './CartPage.css';

const CartPage: FC = () => {
  const { cart, removeFromCart, updateCartItem, getCartTotal } = useCartContext();

  const handleRemoveItem = async (productId: string) => {
    await removeFromCart(productId);
  };

  const handleQuantityChange = async (productId: string, quantity: number) => {
    if (quantity > 0) {
      await updateCartItem(productId, quantity);
    }
  };

  if (cart.items.length === 0) {
    return (
      <Container className="cart-page py-5">
        <Alert variant="info">Your cart is empty</Alert>
      </Container>
    );
  }

  return (
    <Container className="cart-page py-5">
      <h1>Shopping Cart</h1>
      
      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>Product</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody data-testid="cart-items">
          {cart.items.map((item) => (
            <tr key={item.id}>
              <td>{item.title}</td>
              <td>{item.currency} {item.price.toFixed(2)}</td>
              <td>
                <input
                  type="number"
                  min="1"
                  max="99"
                  value={item.cartQuantity}
                  onChange={(e) => handleQuantityChange(item.id, parseInt(e.target.value))}
                  data-testid={`quantity-input-${item.id}`}
                />
              </td>
              <td>{item.currency} {(item.price * item.cartQuantity).toFixed(2)}</td>
              <td>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => handleRemoveItem(item.id)}
                  data-testid={`remove-button-${item.id}`}
                >
                  Remove
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>

      <Row className="mt-4">
        <Col md={6} className="ms-auto">
          <div className="cart-summary">
            <h3>Order Summary</h3>
            <p>Total Items: {cart.items.reduce((acc, item) => acc + item.cartQuantity, 0)}</p>
            <h4>Total: {cart.items[0]?.currency} {getCartTotal().toFixed(2)}</h4>
            <Button variant="success" size="lg" className="w-100" data-testid="checkout-button">
              Proceed to Checkout
            </Button>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default CartPage;

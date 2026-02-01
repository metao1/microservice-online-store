import React, { FC } from 'react';
import { useCartContext } from '../context/CartContext';
import { Container, Button, Alert } from 'react-bootstrap';
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
      <div className="cart-container">
        <Container>
          <Alert variant="info">Your cart is empty</Alert>
        </Container>
      </div>
    );
  }

  return (
    <div className="cart-container">
      <Container>
        <h1 id="cart-title">Shopping Cart</h1>
        
        <div className="cart">
          <div className="cart-items">
            <h3 className="items-title">Items in Cart</h3>
            {cart.items.map((item) => (
              <div key={item.sku} className="cart-item">
                <div className="product-image">
                  <img src={item.imageUrl} alt={item.title} />
                </div>
                <div className="details">
                  <a href={`/products/${item.sku}`}>{item.title}</a>
                  <p className="order-details">{item.description}</p>
                </div>
                <div className="pricing">
                  <h6>Price: {item.currency} {item.price.toFixed(2)}</h6>
                  <h6>Qty: 
                    <input
                      type="number"
                      min="1"
                      max="99"
                      value={item.cartQuantity}
                      onChange={(e) => handleQuantityChange(item.sku, parseInt(e.target.value))}
                      data-testid={`quantity-input-${item.sku}`}
                      style={{ width: '60px', marginLeft: '10px' }}
                    />
                  </h6>
                  <h6>Total: {item.currency} {(item.price * item.cartQuantity).toFixed(2)}</h6>
                </div>
                <div className="actions">
                  <Button
                    className="btn-cart-remove"
                    size="sm"
                    onClick={() => handleRemoveItem(item.sku)}
                    data-testid={`remove-button-${item.sku}`}
                  >
                    Remove
                  </Button>
                </div>
              </div>
            ))}
          </div>

          <div className="total-price">
            <div className="total">
              <div className="details">
                <h3>Order Summary</h3>
                <p className="order-details">
                  Total Items: {cart.items.reduce((acc, item) => acc + item.cartQuantity, 0)}
                </p>
              </div>
              <div className="pricing">
                <h6 id="total-price">
                  Total: {cart.items[0]?.currency} {getCartTotal().toFixed(2)}
                </h6>
              </div>
              <div className="actions">
                <Button variant="success" size="lg" data-testid="checkout-button">
                  Checkout
                </Button>
              </div>
            </div>
          </div>
        </div>
      </Container>
    </div>
  );
};

export default CartPage;

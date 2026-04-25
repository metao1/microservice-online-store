import { FC, useState } from 'react';
import { useCartContext } from '@context/CartContext';
import { useAuthContext } from '@context/AuthContext';
import { useCheckout } from '@hooks/useCheckout';
import { Link, useNavigate } from 'react-router-dom';
import { PaymentMethodType } from '@types';
import './CartPage.css';

const CartPage: FC = () => {
  const { cart, removeFromCart, updateCartItem, getCartTotal } = useCartContext();
  const { user } = useAuthContext();
  const { isProcessing, processCheckout } = useCheckout();
  const navigate = useNavigate();
  const [promoCode, setPromoCode] = useState('');
  const [isPromoExpanded, setIsPromoExpanded] = useState(false);
  const vatRate = 0.21;

  const paymentMethod: PaymentMethodType = 'CREDIT_CARD';
  const paymentDetails = '**** **** **** 4242';

  const handleRemoveItem = async (sku: string) => {
    await removeFromCart(sku);
  };

  const handleQuantityChange = async (sku: string, quantity: number) => {
    if (quantity > 0) {
      const cartItem = cart.items.find((item) => item.sku === sku);
      if (cartItem) {
        const product = {
          sku: cartItem.sku,
          title: cartItem.title,
          price: cartItem.price,
          currency: cartItem.currency,
          imageUrl: cartItem.imageUrl,
          description: cartItem.description,
          rating: cartItem.rating,
          reviews: cartItem.reviews,
          inStock: cartItem.inStock,
          quantity: cartItem.quantity
        };
        await updateCartItem(product, quantity);
      }
    }
  };

  const handleQuantityStep = async (sku: string, current: number, delta: number, maxAllowed: number) => {
    const next = Math.max(1, Math.min(maxAllowed, current + delta));
    if (next !== current) {
      await handleQuantityChange(sku, next);
    }
  };

  const subtotal = getCartTotal() || 0;
  const vatAmount = subtotal * vatRate;
  const delivery: number = 0; // Free delivery
  const total = subtotal + vatAmount + delivery;
  const totalItems = cart.items.reduce((acc, item) => acc + item.cartQuantity, 0);
  const currency = cart.items.length > 0 ? cart.items[0].currency : 'EUR';

  const handleCheckout = async () => {
    if (!user) {
      navigate('/account');
      return;
    }

    await processCheckout(user.id, {
      payment: {
        method: paymentMethod,
        details: paymentDetails,
        currency,
        amount: total
      },
      onSuccess: () => {
        navigate('/orders');
      }
    });
  };

  if (cart.items.length === 0) {
    return (
      <div className="cart-page">
        <div className="cart-container">
          <div className="empty-cart">
            <div className="empty-cart-content">
              <h2>Your bag is empty</h2>
              <p>Looks like you haven't added anything to your bag yet.</p>
              <Link to="/products" className="continue-shopping-btn">
                Continue shopping
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="cart-container">
        <div className="cart-content">
          {/* Cart Items Section */}
          <section className="cart-items-section">
            <div className="cart-header">
              <h1 className="cart-title">Your bag ({totalItems} item{totalItems !== 1 ? 's' : ''})</h1>
              <div className="shipping-info">
                <span className="shipping-carrier">Parcel shipped by ModernStore</span>
                <span className="shipping-date">Tue, 24/02, 08:00 - 20:00</span>
              </div>
            </div>

            <div className="cart-items-list">
              {cart.items.map((item) => {
                const originalPrice = item.price * 1.25; // assume 20% off for visual parity
                const discountPercentage = Math.round(((originalPrice - item.price) / originalPrice) * 100);
                const maxAllowed = Math.max(1, item.quantity || 99);
                const lineTotal = item.price * item.cartQuantity;

                return (
                  <div key={item.sku} className="cart-item">
                    <div className="item-image">
                      <img src={item.imageUrl} alt={item.title} />
                    </div>
                    <div className="item-details">
                      <div className="item-info">
                        <p className="item-brand">ModernStore</p>
                        <p className="item-title">{item.title}</p>
                        <div className="item-price">
                          <span className="current-price">{item.price.toFixed(2)} {item.currency}</span>
                          <span className="original-price">{originalPrice.toFixed(2)} {item.currency}</span>
                          <span className="discount">-{discountPercentage}%</span>
                        </div>
                        <div className="item-line-total">
                          <span className="line-total-label">Line total</span>
                          <span className="line-total-value">{lineTotal.toFixed(2)} {item.currency}</span>
                        </div>
                        <div className="item-meta">
                          <span>Colour: black</span>
                          <span>Size: 38</span>
                        </div>
                        <div className="item-quantity-summary" aria-label={`Quantity in cart: ${item.cartQuantity}`}>
                          <span className="item-quantity-label">In cart</span>
                          <span className="item-quantity-value">{item.cartQuantity}</span>
                        </div>
                        <button className="wishlist-btn">Move to wish list</button>
                      </div>
                      <div className="item-actions">
                        <div className="quantity-stepper" aria-label={`Quantity controls for ${item.title}`}>
                          <button
                            type="button"
                            className="quantity-step-btn"
                            aria-label={`Decrease quantity for ${item.title}`}
                            disabled={item.cartQuantity <= 1}
                            onClick={() => handleQuantityStep(item.sku, item.cartQuantity, -1, maxAllowed)}
                          >
                            -
                          </button>
                          <span className="quantity-current" aria-live="polite">{item.cartQuantity}</span>
                          <button
                            type="button"
                            className="quantity-step-btn"
                            aria-label={`Increase quantity for ${item.title}`}
                            disabled={item.cartQuantity >= maxAllowed}
                            onClick={() => handleQuantityStep(item.sku, item.cartQuantity, 1, maxAllowed)}
                          >
                            +
                          </button>
                        </div>
                        <button
                          className="remove-btn"
                          aria-label="Remove item"
                          onClick={() => handleRemoveItem(item.sku)}
                        >
                          ×
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="cart-notices">
              <div className="notice">
                <span className="notice-dot">•</span>
                <span>Items placed in this bag are not reserved.</span>
              </div>
              <div className="notice">
                <span className="notice-dot">•</span>
                <span><strong>Pricing:</strong> Originally refers to the price the item was first listed at.</span>
              </div>
            </div>
          </section>

          {/* Order Summary Section */}
          <aside className="order-summary-section">
            <div className="order-summary">
              <button
                className={`vouchers-toggle ${isPromoExpanded ? 'expanded' : ''}`}
                onClick={() => setIsPromoExpanded(!isPromoExpanded)}
              >
                <span>Vouchers and gift cards</span>
                <svg
                  className={`chevron ${isPromoExpanded ? 'rotated' : ''}`}
                  width="12"
                  height="12"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <polyline points="6,9 12,15 18,9"></polyline>
                </svg>
              </button>

              {isPromoExpanded && (
                <div className="promo-code-section">
                  <div className="promo-input-group">
                    <input
                      type="text"
                      placeholder="Enter promo code"
                      value={promoCode}
                      onChange={(e) => setPromoCode(e.target.value)}
                      className="promo-input"
                    />
                    <button className="apply-promo-btn">Apply</button>
                  </div>
                </div>
              )}

              <div className="price-breakdown">
                <div className="price-row">
                  <span>Subtotal (excl. VAT)</span>
                  <span>{subtotal.toFixed(2)} {currency}</span>
                </div>
                <div className="price-row">
                  <span>VAT ({Math.round(vatRate * 100)}%)</span>
                  <span>{vatAmount.toFixed(2)} {currency}</span>
                </div>
                <div className="price-row">
                  <span>Delivery</span>
                  <span>{delivery.toFixed(2)} {currency}</span>
                </div>
                <div className="price-row total-row">
                  <span>Total <span className="vat-note">VAT included</span></span>
                  <span className="total-price">{total.toFixed(2)} {currency}</span>
                </div>
              </div>

              <button
                className="checkout-btn"
                data-testid="checkout-button"
                onClick={handleCheckout}
                disabled={isProcessing}
              >
                {isProcessing ? 'Processing...' : 'Go to checkout'}
              </button>
            </div>
          </aside>
        </div>

      </div>
    </div>
  );
};

export default CartPage;

import { FC, useState } from 'react';
import { useCartContext } from '../context/CartContext';
import { useAuthContext } from '../context/AuthContext';
import { useCheckout } from '../hooks/useCheckout';
import { Link, useNavigate } from 'react-router-dom';
import './CartPage.css';

const CartPage: FC = () => {
  const { cart, removeFromCart, updateCartItem, getCartTotal } = useCartContext();
  const { user } = useAuthContext();
  const { isProcessing, processCheckout } = useCheckout();
  const navigate = useNavigate();
  const [promoCode, setPromoCode] = useState('');
  const [isPromoExpanded, setIsPromoExpanded] = useState(false);

  const handleRemoveItem = async (productId: string) => {
    await removeFromCart(productId);
  };

  const handleQuantityChange = async (productId: string, quantity: number) => {
    if (quantity > 0) {
      const cartItem = cart.items.find(item => item.sku === productId);
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

  const handleCheckout = async () => {
    if (!user) {
      // Redirect to login or show login modal
      navigate('/account');
      return;
    }

    const order = await processCheckout(user.id, (newOrder) => {
      // Redirect to orders page after successful checkout
      navigate('/orders');
    });
  };

  const subtotal = getCartTotal() || 0;
  const delivery = 0; // Free delivery
  const total = subtotal + delivery;
  const totalItems = cart.items.reduce((acc, item) => acc + item.cartQuantity, 0);
  const currency = cart.items.length > 0 ? cart.items[0].currency : 'EUR';

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
          <div className="cart-items-section">
            <div className="cart-header">
              <h1 className="cart-title">Your bag ({totalItems} item{totalItems !== 1 ? 's' : ''})</h1>
              <div className="shipping-info">
                <div className="shipping-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="1" y="3" width="15" height="13"></rect>
                    <polygon points="16,6 22,6 22,18 16,18"></polygon>
                    <circle cx="5.5" cy="18.5" r="2.5"></circle>
                    <circle cx="18.5" cy="18.5" r="2.5"></circle>
                  </svg>
                </div>
                <span>Parcel shipped by ModernStore</span>
                <span className="shipping-date">Wed, 04/02 - Thu, 05/02</span>
              </div>
            </div>

            <div className="cart-items-list">
              {cart.items.map((item) => {
                // Calculate discount percentage
                const originalPrice = item.price * 1.25; // Assuming 20% discount
                const discountPercentage = Math.round(((originalPrice - item.price) / originalPrice) * 100);
                
                return (
                  <div key={item.sku} className="cart-item">
                    <div className="item-image">
                      <img src={item.imageUrl} alt={item.title} />
                      {/* Discount Badge */}
                      {discountPercentage > 0 && (
                        <div className="status-badge discount-badge">
                          -{discountPercentage}%
                        </div>
                      )}
                    </div>
                  
                    <div className="item-details">
                      <div className="item-info">
                        <h3 className="item-brand">ModernStore</h3>
                        <h4 className="item-title">{item.title}</h4>
                        <div className="item-price">
                          <span className="current-price">{item.currency} {item.price.toFixed(2)}</span>
                          <span className="original-price">{item.currency} {originalPrice.toFixed(2)}</span>
                          <span className="discount">-{discountPercentage}%</span>
                        </div>
                        <div className="item-attributes">
                          <span>Colour: Black</span>
                          <span>Size: M</span>
                        </div>
                        <button className="move-to-wishlist">Move to wish list</button>
                      </div>

                      <div className="item-actions">
                        <div className="quantity-selector">
                          <button 
                            className="quantity-btn"
                            onClick={() => handleQuantityChange(item.sku, item.cartQuantity - 1)}
                            disabled={item.cartQuantity <= 1}
                          >
                            -
                          </button>
                          <span className="quantity-display">{item.cartQuantity}</span>
                          <button 
                            className="quantity-btn"
                            onClick={() => handleQuantityChange(item.sku, item.cartQuantity + 1)}
                          >
                            +
                          </button>
                        </div>
                        
                        <button 
                          className="remove-item"
                          onClick={() => handleRemoveItem(item.sku)}
                          aria-label="Remove item"
                        >
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="cart-notices">
              <div className="notice">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>Items placed in this bag are not reserved.</span>
              </div>
              <div className="notice">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>Pricing: Originally refers to the price the item was first listed at.</span>
              </div>
            </div>
          </div>

          {/* Order Summary Section */}
          <div className="order-summary-section">
            <div className="order-summary">
              {/* Vouchers and Gift Cards */}
              <div className="vouchers-section">
                <button 
                  className={`vouchers-toggle ${isPromoExpanded ? 'expanded' : ''}`}
                  onClick={() => setIsPromoExpanded(!isPromoExpanded)}
                >
                  <div className="vouchers-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                      <line x1="8" y1="21" x2="16" y2="21"></line>
                      <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                  </div>
                  <span>Vouchers and gift cards</span>
                  <svg 
                    className={`chevron ${isPromoExpanded ? 'rotated' : ''}`}
                    width="16" 
                    height="16" 
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
              </div>

              {/* Price Breakdown */}
              <div className="price-breakdown">
                <div className="price-row">
                  <span>Subtotal</span>
                  <span>{currency} {subtotal.toFixed(2)}</span>
                </div>
                <div className="price-row">
                  <span>Delivery</span>
                  <span>{delivery === 0 ? 'Free' : `${currency} ${(delivery as number).toFixed(2)}`}</span>
                </div>
                <div className="price-row total-row">
                  <span>Total <span className="vat-note">VAT included</span></span>
                  <span className="total-price">{currency} {total.toFixed(2)}</span>
                </div>
              </div>

              {/* Points */}
              <div className="points-section">
                <div className="points-badge">P Points</div>
              </div>

              {/* Checkout Button */}
              <button 
                className="checkout-btn" 
                data-testid="checkout-button"
                onClick={handleCheckout}
                disabled={isProcessing}
              >
                {isProcessing ? 'Processing...' : 'Go to checkout'}
              </button>

              {/* Payment Methods */}
              <div className="payment-methods">
                <span className="payment-label">We accept</span>
                <div className="payment-icons">
                  <div className="payment-icon mastercard"></div>
                  <div className="payment-icon visa"></div>
                  <div className="payment-icon amex"></div>
                  <div className="payment-icon paypal"></div>
                  <div className="payment-icon applepay"></div>
                  <div className="payment-icon googlepay"></div>
                  <div className="payment-icon klarna"></div>
                  <div className="payment-icon discover"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;

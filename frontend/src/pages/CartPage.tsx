import { FC, useState } from 'react';
import { useCartContext } from '@context/CartContext';
import { useAuthContext } from '@context/AuthContext';
import { useCheckout } from '@hooks/useCheckout';
import { Link, useNavigate } from 'react-router-dom';
import { PaymentMethodType } from '@types';
import './CartPage.css';

const CartPage: FC = () => {
  const recommendedProducts = [
    {
      title: 'Club Pant - Tracksuit bottoms - black',
      brand: 'Nike Sportswear',
      price: 54.95,
      image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
    },
    {
      title: 'Set Bermuda Shorts and T-shirt - sand',
      brand: 'PULL&BEAR',
      price: 24.99,
      image: 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80'
    },
    {
      title: 'Baggy Tracksuit bottoms - light grey',
      brand: 'PULL&BEAR',
      price: 27.99,
      image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
    },
    {
      title: 'Wide Smart Trousers - black',
      brand: 'PULL&BEAR',
      price: 35.99,
      image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
    },
    {
      title: 'Rucksack - black/white',
      brand: 'Nike Sportswear',
      price: 37.95,
      image: 'https://images.unsplash.com/photo-1503342452485-86b7f54527dd?auto=format&fit=crop&w=600&q=80'
    },
    {
      title: 'Chuck Taylor All Star HI - black',
      brand: 'Converse',
      price: 74.95,
      image: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=600&q=80'
    }
  ];

  const { cart, removeFromCart, updateCartItem, getCartTotal } = useCartContext();
  const { user } = useAuthContext();
  const { isProcessing, processCheckout } = useCheckout();
  const navigate = useNavigate();
  const [promoCode, setPromoCode] = useState('');
  const [isPromoExpanded, setIsPromoExpanded] = useState(false);

  const paymentMethod: PaymentMethodType = 'CREDIT_CARD';
  const paymentDetails = '**** **** **** 4242';

  const handleRemoveItem = async (productId: string) => {
    await removeFromCart(productId);
  };

  const handleQuantityChange = async (productId: string, quantity: number) => {
    if (quantity > 0) {
      const cartItem = cart.items.find((item) => item.sku === productId);
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

  const subtotal = getCartTotal() || 0;
  const delivery: number = 0; // Free delivery
  const total = subtotal + delivery;
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
                        <div className="item-meta">
                          <span>Colour: black</span>
                          <span>Size: 38</span>
                        </div>
                        <button className="wishlist-btn">Move to wish list</button>
                      </div>
                      <div className="item-actions">
                        <select
                          className="quantity-select"
                          value={item.cartQuantity}
                          onChange={(e) => handleQuantityChange(item.sku, Number(e.target.value))}
                        >
                          {[1, 2, 3, 4, 5].map((val) => (
                            <option key={val} value={val}>{val}</option>
                          ))}
                        </select>
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
                  <span>Subtotal</span>
                  <span>{total.toFixed(2)} {currency}</span>
                </div>
                <div className="price-row">
                  <span>Delivery</span>
                  <span>{delivery === 0 ? '0,00 €' : `${delivery.toFixed(2)} ${currency}`}</span>
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

              <div className="payment-methods">
                <span className="payment-label">We accept</span>
                <div className="payment-icons">
                  <div className="payment-icon maestro"></div>
                  <div className="payment-icon visa"></div>
                  <div className="payment-icon amex"></div>
                  <div className="payment-icon paypal"></div>
                  <div className="payment-icon applepay"></div>
                  <div className="payment-icon klarna"></div>
                  <div className="payment-icon discover"></div>
                </div>
              </div>
            </div>
          </aside>
        </div>

        {/* Recommendations */}
        <section className="recommendations">
          <div className="recommendations-header">
            <p className="recommendations-title">We think you'll like these</p>
            <p className="recommendations-subtitle">Recommended for you</p>
          </div>
          <div className="recommendations-list">
            {recommendedProducts.map((product, idx) => (
              <div key={idx} className="recommendation-card">
                <div className="rec-image">
                  <img src={product.image} alt={product.title} />
                </div>
                <p className="rec-brand">{product.brand}</p>
                <p className="rec-title">{product.title}</p>
                <p className="rec-price">€{product.price.toFixed(2)}</p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

export default CartPage;

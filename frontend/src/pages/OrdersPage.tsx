/**
 * Orders Page Component
 * Displays user's order history and current orders
 */

import { FC } from 'react';
import { Link } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { useOrders } from '../hooks/useOrders';
import { useCheckout } from '../hooks/useCheckout';
import { useCartContext } from '../context/CartContext';
import { Order } from '../types';
import './OrdersPage.css';

const OrdersPage: FC = () => {
  const { user } = useAuthContext();
  const { orders: apiOrders, loading, error, refetch } = useOrders(user?.id || null);
  const { processCheckout, isProcessing } = useCheckout();
  const { cart, getCartItemCount } = useCartContext();

  const handleCheckout = async () => {
    if (!user) {
      return;
    }

    const order = await processCheckout(user.id, (newOrder) => {
      // Refresh orders list after successful checkout
      refetch();
    });
  };

  // Mock orders data as fallback
  const mockOrders: Order[] = [
    {
      id: 'ORD-2024-001',
      userId: user?.id || 'demo-user-1',
      status: 'DELIVERED',
      total: 129.99,
      createdAt: '2024-01-15T10:30:00Z',
      items: [
        {
          sku: 'classic-white-tshirt',
          title: 'Classic White T-Shirt',
          price: 29.99,
          currency: 'USD',
          imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=200&h=200&fit=crop',
          description: 'Comfortable cotton t-shirt',
          rating: 4.5,
          reviews: 120,
          inStock: true,
          quantity: 10,
          cartQuantity: 2
        },
        {
          sku: 'denim-jeans',
          title: 'Denim Jeans',
          price: 69.99,
          currency: 'USD',
          imageUrl: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=200&h=200&fit=crop',
          description: 'Classic blue denim jeans',
          rating: 4.3,
          reviews: 85,
          inStock: true,
          quantity: 5,
          cartQuantity: 1
        }
      ]
    },
    {
      id: 'ORD-2024-002',
      userId: user?.id || 'demo-user-1',
      status: 'SHIPPED',
      total: 89.99,
      createdAt: '2024-01-20T14:15:00Z',
      items: [
        {
          sku: 'sneakers',
          title: 'Sneakers',
          price: 89.99,
          currency: 'USD',
          imageUrl: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=200&h=200&fit=crop',
          description: 'Comfortable running sneakers',
          rating: 4.7,
          reviews: 200,
          inStock: true,
          quantity: 8,
          cartQuantity: 1
        }
      ]
    }
  ];

  // Use API orders if available, otherwise fall back to mock data
  const orders = apiOrders.length > 0 ? apiOrders : mockOrders;

  const getStatusColor = (status: Order['status']) => {
    switch (status.toLowerCase()) {
      case 'delivered': return '#27ae60';
      case 'shipped': return '#3498db';
      case 'confirmed':
      case 'processing': return '#f39c12';
      case 'pending': return '#f39c12';
      case 'cancelled': return '#e74c3c';
      default: return '#666666';
    }
  };

  const getStatusDisplay = (status: Order['status']) => {
    switch (status.toLowerCase()) {
      case 'delivered': return 'Delivered';
      case 'shipped': return 'Shipped';
      case 'confirmed': return 'Confirmed';
      case 'pending': return 'Processing';
      default: return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className="orders-page">
        <div className="orders-container">
          <div className="orders-header">
            <h1>Your Orders</h1>
            <Link to="/account" className="back-to-account">
              ← Back to Account
            </Link>
          </div>
          <div style={{ padding: '40px', textAlign: 'center' }}>
            <p>Loading your orders...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="orders-page">
      <div className="orders-container">
        <div className="orders-header">
          <h1>Your Orders</h1>
          <Link to="/account" className="back-to-account">
            ← Back to Account
          </Link>
        </div>

        {/* Checkout Section - Show when cart has items */}
        {cart.items.length > 0 && (
          <div className="checkout-section" style={{
            padding: '20px',
            marginBottom: '20px',
            backgroundColor: '#f8f9fa',
            border: '1px solid #e9ecef',
            borderRadius: '8px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ margin: '0 0 8px 0', fontSize: '18px' }}>Ready to checkout?</h3>
                <p style={{ margin: '0', color: '#666' }}>
                  You have {getCartItemCount()} item{getCartItemCount() !== 1 ? 's' : ''} in your cart
                </p>
              </div>
              <div style={{ display: 'flex', gap: '12px' }}>
                <Link 
                  to="/cart" 
                  className="order-action-btn"
                  style={{ textDecoration: 'none' }}
                >
                  View Cart
                </Link>
                <button 
                  className="order-action-btn"
                  onClick={handleCheckout}
                  disabled={isProcessing}
                  style={{
                    backgroundColor: '#000',
                    color: '#fff',
                    border: 'none'
                  }}
                >
                  {isProcessing ? 'Processing...' : 'Checkout Now'}
                </button>
              </div>
            </div>
          </div>
        )}

        {error && (
          <div className="error-message" style={{ 
            padding: '16px', 
            marginBottom: '20px', 
            backgroundColor: '#fee', 
            border: '1px solid #fcc', 
            borderRadius: '4px',
            color: '#c33'
          }}>
            {error} {apiOrders.length === 0 && 'Showing sample orders instead.'}
          </div>
        )}

        {orders.length === 0 ? (
          <div className="no-orders">
            <h2>No orders yet</h2>
            <p>When you place your first order, it will appear here.</p>
            <Link to="/products" className="shop-now-btn">
              Start Shopping
            </Link>
          </div>
        ) : (
          <div className="orders-list">
            {orders.map((order) => (
              <div key={order.id} className="order-card">
                <div className="order-header">
                  <div className="order-info">
                    <h3>Order {order.id}</h3>
                    <p className="order-date">Placed on {formatDate(order.createdAt)}</p>
                  </div>
                  <div className="order-status">
                    <span 
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(order.status) }}
                    >
                      {getStatusDisplay(order.status)}
                    </span>
                    <p className="order-total">${order.total.toFixed(2)}</p>
                  </div>
                </div>

                <div className="order-items">
                  {order.items.map((item) => (
                    <div key={item.sku} className="order-item">
                      <img 
                        src={item.imageUrl} 
                        alt={item.title}
                        className="item-image"
                      />
                      <div className="item-details">
                        <h4>{item.title}</h4>
                        <p className="item-price">
                          ${item.price.toFixed(2)} × {item.cartQuantity}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="order-actions">
                  <button className="order-action-btn">View Details</button>
                  <button className="order-action-btn">Track Package</button>
                  {order.status.toLowerCase() === 'delivered' && (
                    <Link to="/returns" className="order-action-btn">
                      Return Items
                    </Link>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;
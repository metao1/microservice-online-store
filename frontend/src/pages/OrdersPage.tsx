/**
 * Orders Page Component
 * Displays user's order history and current orders
 */

import {FC} from 'react';
import {Link} from 'react-router-dom';
import {useAuthContext} from '@context/AuthContext';
import {useOrders} from '@hooks/useOrders';
import {useCheckout} from '@hooks/useCheckout';
import {useCartContext} from '@context/CartContext';
import {Order} from '@types';
import './OrdersPage.css';

const PAGE_SIZE = 10;

const OrdersPage: FC = () => {
  const { user } = useAuthContext();
  const {
    orders: apiOrders,
    total: apiTotal,
    hasNext,
    hasPrevious,
    currentPage,
    loading,
    error,
    refetch,
    setCurrentPage,
  } = useOrders(user?.id || null, PAGE_SIZE);
  const { processCheckout, isProcessing } = useCheckout();
  const { cart, getCartItemCount } = useCartContext();

  const handleCheckout = async () => {
    if (!user) {
      return;
    }

    await processCheckout(user.id, {
      payment: {
        method: 'CREDIT_CARD',
        currency: cart.items[0]?.currency || 'USD',
        amount: cart.items.reduce((sum, item) => sum + item.price * item.cartQuantity, 0)
      },
      onSuccess: () => {
        // Refresh orders list after successful checkout
        refetch();
      }
    });
  };

  const orders = apiOrders;
  const totalOrders = apiTotal;
  const totalPages = Math.max(1, Math.ceil(totalOrders / PAGE_SIZE));
  const canGoPrevious = currentPage > 1 && hasPrevious;
  const canGoNext = hasNext;

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
            {error}
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
                    <p className="order-total">${order.total}</p>
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
                          ${item.price} × ${item.cartQuantity}
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

        {orders.length > 0 && (
          <div className="orders-pagination">
            <div className="orders-pagination-summary">
              Showing {Math.min((currentPage - 1) * PAGE_SIZE + 1, totalOrders)}-
              {Math.min(currentPage * PAGE_SIZE, totalOrders)} of {totalOrders} orders
            </div>
            <div className="orders-pagination-controls">
              <button
                className="order-action-btn"
                onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                disabled={!canGoPrevious}
              >
                Previous
              </button>
              <span className="orders-pagination-status">
                Page {currentPage} of {totalPages}
              </span>
              <button
                className="order-action-btn"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={!canGoNext}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;

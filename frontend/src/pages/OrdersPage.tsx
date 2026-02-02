/**
 * Orders Page Component
 * Displays user's order history and current orders
 */

import { FC } from 'react';
import { Link } from 'react-router-dom';
import './OrdersPage.css';

interface Order {
  id: string;
  date: string;
  status: 'delivered' | 'shipped' | 'processing' | 'cancelled';
  total: number;
  items: {
    id: string;
    name: string;
    image: string;
    price: number;
    quantity: number;
  }[];
}

const OrdersPage: FC = () => {
  // Mock orders data - this would come from an API
  const orders: Order[] = [
    {
      id: 'ORD-2024-001',
      date: '2024-01-15',
      status: 'delivered',
      total: 129.99,
      items: [
        {
          id: '1',
          name: 'Classic White T-Shirt',
          image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=200&h=200&fit=crop',
          price: 29.99,
          quantity: 2
        },
        {
          id: '2',
          name: 'Denim Jeans',
          image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=200&h=200&fit=crop',
          price: 69.99,
          quantity: 1
        }
      ]
    },
    {
      id: 'ORD-2024-002',
      date: '2024-01-20',
      status: 'shipped',
      total: 89.99,
      items: [
        {
          id: '3',
          name: 'Sneakers',
          image: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=200&h=200&fit=crop',
          price: 89.99,
          quantity: 1
        }
      ]
    }
  ];

  const getStatusColor = (status: Order['status']) => {
    switch (status) {
      case 'delivered': return '#27ae60';
      case 'shipped': return '#3498db';
      case 'processing': return '#f39c12';
      case 'cancelled': return '#e74c3c';
      default: return '#666666';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <div className="orders-page">
      <div className="orders-container">
        <div className="orders-header">
          <h1>Your Orders</h1>
          <Link to="/account" className="back-to-account">
            ← Back to Account
          </Link>
        </div>

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
                    <p className="order-date">Placed on {formatDate(order.date)}</p>
                  </div>
                  <div className="order-status">
                    <span 
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(order.status) }}
                    >
                      {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                    </span>
                    <p className="order-total">${order.total.toFixed(2)}</p>
                  </div>
                </div>

                <div className="order-items">
                  {order.items.map((item) => (
                    <div key={item.id} className="order-item">
                      <img 
                        src={item.image} 
                        alt={item.name}
                        className="item-image"
                      />
                      <div className="item-details">
                        <h4>{item.name}</h4>
                        <p className="item-price">
                          ${item.price.toFixed(2)} × {item.quantity}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="order-actions">
                  <button className="order-action-btn">View Details</button>
                  <button className="order-action-btn">Track Package</button>
                  {order.status === 'delivered' && (
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
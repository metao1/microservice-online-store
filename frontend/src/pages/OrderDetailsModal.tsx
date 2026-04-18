/**
 * Order Details Modal
 * Accessible dialog that renders the full details of a single order,
 * including line-item breakdown and payment status, using data already
 * fetched by OrdersPage (no additional network calls).
 */

import {FC, useEffect, useRef} from 'react';
import {Order, Payment} from '@types';
import './OrderDetailsModal.css';

interface OrderDetailsModalProps {
  order: Order;
  payment: Payment | null | undefined;
  onClose: () => void;
}

const formatCurrency = (amount: number, currency: string): string => {
  const code = (currency || 'USD').toUpperCase();
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: code,
      minimumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${code} ${amount.toFixed(2)}`;
  }
};

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '—';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const formatPaymentMethod = (method?: string): string => {
  if (!method) {
    return '—';
  }
  return method
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join(' ');
};

const getStatusColor = (status: string): string => {
  switch ((status || '').toLowerCase()) {
    case 'delivered':
      return '#27ae60';
    case 'shipped':
      return '#3498db';
    case 'confirmed':
    case 'processing':
    case 'pending':
      return '#f39c12';
    case 'cancelled':
      return '#e74c3c';
    default:
      return '#666666';
  }
};

const getPaymentStatusColor = (status: string): string => {
  const normalized = (status || '').toUpperCase();
  if (normalized === 'COMPLETED' || normalized === 'SUCCESSFUL') {
    return '#27ae60';
  }
  if (normalized === 'FAILED' || normalized === 'CANCELLED') {
    return '#e74c3c';
  }
  if (normalized === 'PROCESSING' || normalized === 'PENDING' || normalized === 'CREATED') {
    return '#f39c12';
  }
  return '#666666';
};

const OrderDetailsModal: FC<OrderDetailsModalProps> = ({ order, payment, onClose }) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  // Close on Escape + focus trap entry point.
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    closeButtonRef.current?.focus();

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = previousOverflow;
    };
  }, [onClose]);

  const handleBackdropClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (event.target === event.currentTarget) {
      onClose();
    }
  };

  const currency = order.items[0]?.currency || payment?.currency || 'USD';
  const itemCount = order.items.reduce((sum, item) => sum + item.cartQuantity, 0);
  const computedSubtotal = order.items.reduce(
    (sum, item) => sum + item.price * item.cartQuantity,
    0,
  );
  const total = Number.isFinite(order.total) ? order.total : computedSubtotal;

  return (
    <div
      className="order-details-backdrop"
      onClick={handleBackdropClick}
      role="presentation"
    >
      <div
        ref={dialogRef}
        className="order-details-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="order-details-title"
      >
        <header className="order-details-header">
          <div className="order-details-heading">
            <p className="order-details-eyebrow">Order Details</p>
            <h2 id="order-details-title">Order {order.id}</h2>
            <p className="order-details-subtitle">
              Placed {formatDateTime(order.createdAt)}
            </p>
          </div>
          <button
            ref={closeButtonRef}
            type="button"
            className="order-details-close"
            onClick={onClose}
            aria-label="Close order details"
          >
            <span aria-hidden="true">×</span>
          </button>
        </header>

        <div className="order-details-body">
          <section className="order-details-section" aria-labelledby="order-summary-heading">
            <h3 id="order-summary-heading" className="order-details-section-title">
              Summary
            </h3>
            <dl className="order-details-grid">
              <div className="order-details-grid-row">
                <dt>Status</dt>
                <dd>
                  <span
                    className="order-details-badge"
                    style={{ backgroundColor: getStatusColor(order.status) }}
                  >
                    {order.status}
                  </span>
                </dd>
              </div>
              <div className="order-details-grid-row">
                <dt>Items</dt>
                <dd>
                  {itemCount} item{itemCount === 1 ? '' : 's'}
                </dd>
              </div>
              <div className="order-details-grid-row">
                <dt>Order total</dt>
                <dd className="order-details-total">{formatCurrency(total, currency)}</dd>
              </div>
            </dl>
          </section>

          <section className="order-details-section" aria-labelledby="order-items-heading">
            <h3 id="order-items-heading" className="order-details-section-title">
              Items
            </h3>
            <ul className="order-details-items">
              {order.items.map((item) => {
                const lineTotal = item.price * item.cartQuantity;
                return (
                  <li key={item.sku} className="order-details-item">
                    <img
                      src={item.imageUrl}
                      alt={item.title}
                      className="order-details-item-image"
                    />
                    <div className="order-details-item-info">
                      <p className="order-details-item-title">{item.title}</p>
                      <p className="order-details-item-meta">SKU: {item.sku}</p>
                      <p className="order-details-item-meta">
                        {formatCurrency(item.price, item.currency)} ×{' '}
                        {item.cartQuantity}
                      </p>
                    </div>
                    <div className="order-details-item-price">
                      {formatCurrency(lineTotal, item.currency)}
                    </div>
                  </li>
                );
              })}
            </ul>
          </section>

          <section className="order-details-section" aria-labelledby="order-payment-heading">
            <h3 id="order-payment-heading" className="order-details-section-title">
              Payment
            </h3>
            {payment ? (
              <dl className="order-details-grid">
                <div className="order-details-grid-row">
                  <dt>Status</dt>
                  <dd>
                    <span
                      className="order-details-badge"
                      style={{ backgroundColor: getPaymentStatusColor(payment.status) }}
                    >
                      {payment.status}
                    </span>
                  </dd>
                </div>
                <div className="order-details-grid-row">
                  <dt>Method</dt>
                  <dd>{formatPaymentMethod(payment.paymentMethodType)}</dd>
                </div>
                <div className="order-details-grid-row">
                  <dt>Amount</dt>
                  <dd>{formatCurrency(payment.amount, payment.currency)}</dd>
                </div>
                <div className="order-details-grid-row">
                  <dt>Processed</dt>
                  <dd>{formatDateTime(payment.processedAt)}</dd>
                </div>
                {payment.failureReason && (
                  <div className="order-details-grid-row">
                    <dt>Reason</dt>
                    <dd className="order-details-failure">{payment.failureReason}</dd>
                  </div>
                )}
              </dl>
            ) : (
              <p className="order-details-empty">
                No payment information is available for this order yet.
              </p>
            )}
          </section>
        </div>

        <footer className="order-details-footer">
          <button
            type="button"
            className="order-details-footer-btn"
            onClick={onClose}
          >
            Close
          </button>
        </footer>
      </div>
    </div>
  );
};

export default OrderDetailsModal;

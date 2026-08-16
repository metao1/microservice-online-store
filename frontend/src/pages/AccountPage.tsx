import { FC } from 'react';
import { Link } from 'react-router-dom';
import ProtectedRoute from '../auth/ProtectedRoute';
import { useAuthContext } from '../context/AuthContext';
import './AccountPage.css';

const AccountPage: FC = () => {
  const { initialized, isAuthenticated, user, login, register, logout } = useAuthContext();

  if (!initialized) {
    return <div className="account-page">Loading account…</div>;
  }

  if (!isAuthenticated) {
    return (
      <div className="account-page">
        <div className="auth-container">
          <div className="auth-header">
            <h1>Your Account</h1>
            <p>Sign in or create an account securely with ModernStore.</p>
          </div>
          <div className="auth-form">
            <button type="button" className="auth-submit-btn" onClick={() => void login('/account')}>
              Sign In
            </button>
            <button type="button" className="toggle-btn" onClick={() => void register('/account')}>
              Create Account
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <ProtectedRoute>
      <div className="account-page">
        <div className="account-container">
          <div className="account-header">
            <h1>Your Account</h1>
            <button type="button" onClick={() => void logout()} className="sign-out-btn">
              Sign Out
            </button>
          </div>

          <div className="account-sections">
            <div className="account-section">
              <h2>Account Overview</h2>
              <div className="account-info">
                <p><strong>Name:</strong> {user?.name ?? 'Not provided'}</p>
                <p><strong>Email:</strong> {user?.email ?? 'Not provided'}</p>
              </div>
            </div>

            <div className="account-section">
              <h2>Quick Actions</h2>
              <div className="quick-actions">
                <Link to="/orders" className="action-card">
                  <h3>Orders</h3>
                  <p>View your order history and track current orders</p>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  );
};

export default AccountPage;

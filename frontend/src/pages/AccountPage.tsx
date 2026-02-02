/**
 * Account Page Component
 * Handles user authentication (sign-in/sign-up) and account management
 */

import { FC, useState } from 'react';
import { Link } from 'react-router-dom';
import './AccountPage.css';

interface AccountPageProps {}

const AccountPage: FC<AccountPageProps> = () => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false); // This would come from auth context
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    confirmPassword: ''
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Add authentication logic here
    console.log('Form submitted:', formData);
    // For demo purposes, simulate login
    setIsLoggedIn(true);
  };

  const handleSignOut = () => {
    setIsLoggedIn(false);
    setFormData({
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      confirmPassword: ''
    });
  };

  if (isLoggedIn) {
    return (
      <div className="account-page">
        <div className="account-container">
          <div className="account-header">
            <h1>Your Account</h1>
            <button onClick={handleSignOut} className="sign-out-btn">
              Sign Out
            </button>
          </div>

          <div className="account-sections">
            <div className="account-section">
              <h2>Account Overview</h2>
              <div className="account-info">
                <p><strong>Email:</strong> {formData.email || 'user@example.com'}</p>
                <p><strong>Name:</strong> {formData.firstName} {formData.lastName}</p>
              </div>
            </div>

            <div className="account-section">
              <h2>Quick Actions</h2>
              <div className="quick-actions">
                <Link to="/orders" className="action-card">
                  <h3>Orders</h3>
                  <p>View your order history and track current orders</p>
                </Link>
                <Link to="/returns" className="action-card">
                  <h3>Returns</h3>
                  <p>Return or exchange items from your orders</p>
                </Link>
                <Link to="/wishlist" className="action-card">
                  <h3>Wishlist</h3>
                  <p>View and manage your saved items</p>
                </Link>
                <Link to="/sizes" className="action-card">
                  <h3>Your Sizes</h3>
                  <p>Manage your size preferences</p>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="account-page">
      <div className="auth-container">
        <div className="auth-header">
          <h1>{isSignUp ? 'Create Account' : 'Sign In'}</h1>
          <p>
            {isSignUp 
              ? 'Join ModernStore to track orders and save your favorites'
              : 'Welcome back! Sign in to your account'
            }
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {isSignUp && (
            <>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstName">First Name</label>
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="lastName">Last Name</label>
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>
            </>
          )}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              required
            />
          </div>

          {isSignUp && (
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                required
              />
            </div>
          )}

          <button type="submit" className="auth-submit-btn">
            {isSignUp ? 'Create Account' : 'Sign In'}
          </button>
        </form>

        <div className="auth-toggle">
          <p>
            {isSignUp ? 'Already have an account?' : "Don't have an account?"}
            <button 
              type="button"
              onClick={() => setIsSignUp(!isSignUp)}
              className="toggle-btn"
            >
              {isSignUp ? 'Sign In' : 'Sign Up'}
            </button>
          </p>
        </div>

        <div className="auth-divider">
          <span>or</span>
        </div>

        <div className="social-auth">
          <button className="social-btn google-btn">
            Continue with Google
          </button>
          <button className="social-btn facebook-btn">
            Continue with Facebook
          </button>
        </div>
      </div>
    </div>
  );
};

export default AccountPage;
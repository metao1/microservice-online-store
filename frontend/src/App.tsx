import { FC, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.css';
import './index.css';
import './App.css';
import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage from './pages/CartPage';
import ComponentDemo from './pages/ComponentDemo';
import AccountPage from './pages/AccountPage';
import OrdersPage from './pages/OrdersPage';
import Navigation from './components/Navigation';
import ErrorBoundary from './components/ErrorBoundary';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { User } from './types';

const LoadingFallback: FC = () => (
  <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
);

// For demo purposes, create a default user
const defaultUser: User = {
  id: 'demo-user-1',
  name: 'Demo User',
  email: 'demo@example.com',
  role: 'USER'
};

const App: FC = () => {
  return (
    <ErrorBoundary>
      <AuthProvider initialUser={defaultUser}>
        <CartProvider userId={defaultUser.id}>
          <BrowserRouter>
            <Navigation />
            <div style={{ paddingTop: '100px' }}>
              <Suspense fallback={<LoadingFallback />}>
                <Routes>
                  <Route path="/" element={<HomePage />} />
                  <Route path="/products" element={<ProductsPage />} />
                  <Route path="/products/:sku" element={<ProductDetailPage />} />
                  <Route path="/cart" element={<CartPage />} />
                  <Route path="/account" element={<AccountPage />} />
                  <Route path="/orders" element={<OrdersPage />} />
                  <Route path="/demo" element={<ComponentDemo />} />
                  <Route path="*" element={<HomePage />} />
                </Routes>
              </Suspense>
            </div>
          </BrowserRouter>
        </CartProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
};

export default App;

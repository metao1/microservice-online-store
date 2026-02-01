import React, { FC, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.css';
import './index.css';
import './App.css';
import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage from './pages/CartPage';
import Navigation from './components/Navigation';
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
    <AuthProvider initialUser={defaultUser}>
      <CartProvider userId={defaultUser.id}>
        <BrowserRouter>
          <Navigation />
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/products" element={<ProductsPage />} />
              <Route path="/products/:id" element={<ProductDetailPage />} />
              <Route path="/cart" element={<CartPage />} />
              <Route path="*" element={<HomePage />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
};

export default App;

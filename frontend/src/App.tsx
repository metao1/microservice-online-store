import { FC, PropsWithChildren, Suspense } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'bootstrap/dist/css/bootstrap.css';
import 'react-toastify/dist/ReactToastify.css';
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
import ProtectedRoute from './auth/ProtectedRoute';
import { AuthProvider, useAuthContext } from '@context/AuthContext';
import { CartProvider } from '@context/CartContext';

const LoadingFallback: FC = () => (
  <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
);

const Protected: FC<PropsWithChildren> = ({ children }) => (
  <ProtectedRoute>{children}</ProtectedRoute>
);

const AuthenticatedCartProvider: FC<PropsWithChildren> = ({ children }) => {
  const { user } = useAuthContext();
  return <CartProvider userId={user?.id ?? ''}>{children}</CartProvider>;
};

const App: FC = () => {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <AuthenticatedCartProvider>
          <BrowserRouter>
            <Navigation />
            <div>
              <Suspense fallback={<LoadingFallback />}>
                <Routes>
                  <Route path="/" element={<HomePage />} />
                  <Route path="/products" element={<ProductsPage />} />
                  <Route path="/products/:sku" element={<ProductDetailPage />} />
                  <Route path="/cart" element={<Protected><CartPage /></Protected>} />
                  <Route path="/account" element={<AccountPage />} />
                  <Route path="/orders" element={<Protected><OrdersPage /></Protected>} />
                  <Route path="/demo" element={<ComponentDemo />} />
                  <Route path="*" element={<HomePage />} />
                </Routes>
              </Suspense>
            </div>
            <ToastContainer
              position="top-right"
              autoClose={3000}
              hideProgressBar={false}
              newestOnTop={false}
              closeOnClick
              rtl={false}
              pauseOnFocusLoss
              draggable
              pauseOnHover
            />
          </BrowserRouter>
        </AuthenticatedCartProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
};

export default App;

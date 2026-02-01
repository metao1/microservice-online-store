/**
 * Main Exports
 * 
 * Central export file for easier imports throughout the application
 */

// Pages
export { 
  HomePage, 
  ProductsPage, 
  ProductDetailPage, 
  CartPage 
} from './pages';

// Components
export { 
  ProductCard, 
  Navigation, 
  Footer 
} from './components';

// Hooks
export { 
  useProducts, 
  useProduct, 
  useCart 
} from './hooks';

// Context
export { 
  CartProvider, 
  useCartContext, 
  AuthProvider, 
  useAuthContext 
} from './context';

// Types
export type {
  Product,
  CartItem,
  Cart,
  ApiResponse,
  User,
  Order,
  Category
} from './types';

// Services
export { apiClient } from './services/api';

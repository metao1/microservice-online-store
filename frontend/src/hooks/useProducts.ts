import { useState, useCallback, useEffect } from 'react';
import { Product } from '@types';
import { apiClient } from '../services/api';

export const useProducts = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchProducts = useCallback(async (category?: string, limit: number = 12, offset: number = 0) => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.getProducts(category, limit, offset);
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch products');
    } finally {
      setLoading(false);
    }
  }, []);

  const searchProducts = useCallback(async (query: string, limit: number = 12, offset: number = 0) => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.searchProducts(query, limit, offset);
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to search products');
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    products,
    loading,
    error,
    fetchProducts,
    searchProducts,
  };
};

export const useProduct = (sku: string) => {
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProduct = async () => {
      if (!sku) {
        setError('No product SKU provided');
        return;
      }

      setLoading(true);
      setError(null);
      setProduct(null);
      
      try {
        const data = await apiClient.getProductById(sku);
        setProduct(data);
      } catch (err) {
        console.error('Error in useProduct:', err);
        setError(err instanceof Error ? err.message : 'Failed to fetch product');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [sku]);

  return {
    product,
    loading,
    error,
  };
};

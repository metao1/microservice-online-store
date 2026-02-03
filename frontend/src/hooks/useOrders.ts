import { useState, useEffect } from 'react';
import { Order } from '../types';
import { apiClient } from '../services/api';

interface UseOrdersResult {
  orders: Order[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const useOrders = (userId: string | null): UseOrdersResult => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = async () => {
    if (!userId) {
      setOrders([]);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const fetchedOrders = await apiClient.getOrders(userId);
      setOrders(fetchedOrders);
    } catch (err) {
      console.error('Failed to fetch orders:', err);
      setError('Failed to load orders. Please try again.');
      // Keep existing orders on error to prevent UI flash
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [userId]);

  return {
    orders,
    loading,
    error,
    refetch: fetchOrders,
  };
};
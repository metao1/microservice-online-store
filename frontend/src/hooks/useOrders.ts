import { useState, useEffect } from 'react';
import { Order, PaginatedResult } from '@types';
import { apiClient } from '../services/api';

interface UseOrdersResult {
  orders: Order[];
  total: number;
  hasNext: boolean;
  hasPrevious: boolean;
  pageSize: number;
  currentPage: number;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  setCurrentPage: (page: number) => void;
}

export const useOrders = (userId: string | null, pageSize: number = 10): UseOrdersResult => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [total, setTotal] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [hasPrevious, setHasPrevious] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = async () => {
    if (!userId) {
      setOrders([]);
      setTotal(0);
      setHasNext(false);
      setHasPrevious(false);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const offset = (currentPage - 1) * pageSize;
      const page: PaginatedResult<Order> = await apiClient.getOrdersPage(userId, pageSize, offset);
      setOrders(page.items);
      setTotal(page.total);
      setHasNext(page.hasNext);
      setHasPrevious(page.hasPrevious);
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
  }, [userId, currentPage, pageSize]);

  return {
    orders,
    total,
    hasNext,
    hasPrevious,
    pageSize,
    currentPage,
    loading,
    error,
    refetch: fetchOrders,
    setCurrentPage,
  };
};

import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { createMoney, Order } from '@types';
import OrderDetailsModal from './OrderDetailsModal';

const orderWithVat: Order = {
  id: 'ORDER-VAT',
  userId: 'USER-1',
  items: [],
  total: createMoney(25.20, 'EUR'),
  subtotal: createMoney(21.00, 'EUR'),
  tax: createMoney(4.20, 'EUR'),
  vatPercentage: 20,
  status: 'PAID',
  createdAt: '2026-09-12T09:02:03.642714Z',
};

describe('OrderDetailsModal', () => {
  it('shows the order VAT rate and tax amount', () => {
    render(<OrderDetailsModal order={orderWithVat} payment={null} onClose={vi.fn()} />);

    expect(screen.getByText('VAT (20%)')).toBeInTheDocument();
    expect(screen.getByText('€4.20')).toBeInTheDocument();
  });
});

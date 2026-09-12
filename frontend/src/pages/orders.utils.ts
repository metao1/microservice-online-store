import { Money, Order, Payment } from '@types';

type OrderMoney = Pick<Order, 'items' | 'total'>;
type PaymentMoney = Pick<Payment, 'amount'> | null | undefined;

export const resolveOrderDisplayMoney = (
  order: OrderMoney,
  payment: PaymentMoney,
  fallback: Money = order.total,
): Money => order.total.amount > 0 ? order.total : payment?.amount ?? fallback;

import { OrderStatus, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';

const KNOWN_ORDER_STATUSES = new Set<OrderStatus>([
  'CREATED',
  'PENDING_PAYMENT',
  'PAID',
  'PAYMENT_FAILED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
]);

export abstract class BaseApiClient {
  protected getPlaceholderImage(title: string, index: number): string {
    const colors = ['4A90E2', '7ED321', '9013FE', 'D0021B', 'F5A623', '50E3C2', 'FF6B6B', '4ECDC4'];
    const bgColor = colors[index % colors.length];
    const textColor = 'FFFFFF';
    const cleanTitle = encodeURIComponent(title.substring(0, 20).replace(/[^a-zA-Z0-9\s]/g, ''));
    return `https://via.placeholder.com/400x500/${bgColor}/${textColor}?text=${cleanTitle}`;
    }

  protected getValidImageUrl(originalUrl: string, title: string, index: number): string {
    if (!originalUrl) {
      return this.getPlaceholderImage(title, index);
    }
    if (originalUrl.startsWith('http://ecx.images-amazon.com') || originalUrl.startsWith('http://images-amazon.com')) {
      return originalUrl.replace('http://', 'https://');
    }
    if (originalUrl.startsWith('http://')) {
      return this.getPlaceholderImage(title, index);
    }
    return originalUrl;
  }

  private resolveVariantFlags(product: Product): { hasColor: boolean; hasSize: boolean } {
    const tokens: string[] = [];
    if (product.category) {
      tokens.push(product.category);
    }
    if (Array.isArray(product.categories)) {
      tokens.push(...product.categories);
    }
    if (Array.isArray(product.tags)) {
      tokens.push(...product.tags);
    }
    const normalized = tokens.map((value) => value.toLowerCase());
    const colorKeywords = [
      'clothing',
      'apparel',
      'fashion',
      'shoe',
      'shoes',
      'footwear',
      'sneaker',
      'boot',
      'boots',
      'accessories',
      'jewelry',
      'bag',
      'bags',
      'handbag',
      'wallet',
    ];
    const sizeKeywords = [
      'clothing',
      'apparel',
      'fashion',
      'shoe',
      'shoes',
      'footwear',
      'sneaker',
      'boot',
      'boots',
    ];
    const hasColor = normalized.some((category) => colorKeywords.some((keyword) => category.includes(keyword)));
    const hasSize = normalized.some((category) => sizeKeywords.some((keyword) => category.includes(keyword)));
    return { hasColor, hasSize };
  }

  protected generateMockVariants(product: Product, index: number): {
    variants: any[];
    brand: string;
    originalPrice?: number;
    isNew?: boolean;
    isFeatured?: boolean;
    isSale?: boolean;
  } {
    const variantsProvided = Array.isArray((product as { variants?: unknown }).variants);
    const colorOptions = [
      { name: 'Black', value: '#000000', hexColor: '#000000' },
      { name: 'Navy', value: '#1e3a8a', hexColor: '#1e3a8a' },
      { name: 'Brown', value: '#8b4513', hexColor: '#8b4513' },
      { name: 'Gray', value: '#6b7280', hexColor: '#6b7280' },
      { name: 'White', value: '#ffffff', hexColor: '#ffffff' },
      { name: 'Red', value: '#dc2626', hexColor: '#dc2626' },
      { name: 'Blue', value: '#2563eb', hexColor: '#2563eb' },
      { name: 'Green', value: '#16a34a', hexColor: '#16a34a' },
    ];
    const sizeOptions = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];
    const { hasColor, hasSize } = this.resolveVariantFlags(product);
    const colorVariants = hasColor
      ? colorOptions.slice(0, Math.floor(Math.random() * 3) + 2).map((color, i) => ({
          id: `color-${product.sku}-${i}`,
          type: 'color' as const,
          name: color.name,
          value: color.value,
          hexColor: color.hexColor,
          inStock: Math.random() > 0.2,
          priceModifier: 0,
        }))
      : [];
    const sizeVariants = hasSize
      ? sizeOptions.slice(0, Math.floor(Math.random() * 3) + 3).map((size, i) => ({
          id: `size-${product.sku}-${i}`,
          type: 'size' as const,
          name: size,
          value: size,
          inStock: Math.random() > 0.3,
          priceModifier: size === 'XXL' ? 5 : 0,
        }))
      : [];
    const brands = ['Nike', 'Adidas', 'Puma', 'Reebok', 'Converse', 'New Balance'];
    const brand = brands[index % brands.length];
    const hasDiscount = Math.random() > 0.7;
    const originalPrice =
      hasDiscount ? Math.round(product.price * (1.2 + Math.random() * 0.3) * 100) / 100 : undefined;
    const isNew = Math.random() > 0.8;
    const isFeatured = Math.random() > 0.9;
    const isSale = hasDiscount;
    return {
      variants: variantsProvided ? product.variants ?? [] : [...colorVariants, ...sizeVariants],
      brand,
      originalPrice,
      isNew,
      isFeatured,
      isSale,
    };
  }

  /**
   * Normalises the `status` field returned by the order-microservice
   * (propagated from `OrderCreatedEvent` / `OrderStatusChangedEvent`) into the
   * strongly-typed `OrderStatus` union.
   *
   * Historically this method mapped the backend enum onto a much smaller
   * hand-crafted subset (PENDING / CONFIRMED / SHIPPED / DELIVERED) and fell
   * through to `'PENDING'` for anything it didn't recognise — which silently
   * masked real statuses such as `PAID`, `PAYMENT_FAILED` and `CANCELLED`
   * behind the generic "Processing" label. The frontend now tracks the
   * backend enum verbatim, so this is a straight pass-through with a
   * conservative fallback of `CREATED` for unknown values.
   */
  protected normalizeOrderStatus(backendStatus: string | null | undefined): OrderStatus {
    const candidate = (backendStatus || '').toUpperCase() as OrderStatus;
    return KNOWN_ORDER_STATUSES.has(candidate) ? candidate : 'CREATED';
  }

  protected mapPayment(dto: any): Payment {
    if (!dto) throw new Error('Invalid payment payload received from server');
    const currency =
      typeof dto.currency === 'string'
        ? dto.currency
        : dto.currency?.currencyCode || dto.currency?.currency || dto.currency?.code || 'USD';
    const status = (dto.status || '').toUpperCase();
    return {
      paymentId: dto.paymentId || dto.id,
      orderId: dto.orderId,
      amount: Number(dto.amount) || 0,
      currency,
      paymentMethodType: (dto.paymentMethodType || dto.paymentMethod || 'CREDIT_CARD') as PaymentMethodType,
      paymentMethodDetails: dto.paymentMethodDetails || dto.details || '',
      status,
      failureReason: dto.failureReason,
      processedAt: dto.processedAt,
      createdAt: dto.createdAt,
      isCompleted: dto.isCompleted ?? status === 'COMPLETED',
      isSuccessful: dto.isSuccessful ?? (status === 'COMPLETED' || status === 'SUCCESSFUL'),
    };
  }

  protected mapPaymentStats(raw: any): PaymentStatistics {
    const totalPayments = Number(raw?.totalPayments) || 0;
    const successfulPayments = Number(raw?.successfulPayments) || 0;
    const failedPayments = Number(raw?.failedPayments) || 0;
    const pendingPayments = Number(raw?.pendingPayments) || 0;
    const successRate = raw?.successRate ?? (totalPayments > 0 ? (successfulPayments / totalPayments) * 100 : 0);
    const failureRate = raw?.failureRate ?? (totalPayments > 0 ? (failedPayments / totalPayments) * 100 : 0);
    return {
      totalPayments,
      successfulPayments,
      failedPayments,
      pendingPayments,
      successRate,
      failureRate,
    };
  }
}

import { Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';

export abstract class BaseApiClient {
  protected getPlaceholderImage(title: string, index: number): string {
    const colors = ['4A90E2', '7ED321', 'F5A623', 'D0021B', '9013FE', '50E3C2'];
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

  protected generateMockVariants(product: Product, index: number): {
    variants: any[];
    brand: string;
    originalPrice?: number;
    isNew?: boolean;
    isFeatured?: boolean;
    isSale?: boolean;
  } {
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
    const sizeOptions = ['36', '37', '38', '39', '40', '41', '42', '43', '44', '45'];
    const numColors = Math.floor(Math.random() * 3) + 2;
    const selectedColors = colorOptions.slice(0, numColors);
    const colorVariants = selectedColors.map((color, i) => ({
      id: `color-${product.sku}-${i}`,
      type: 'color' as const,
      name: color.name,
      value: color.value,
      hexColor: color.hexColor,
      inStock: Math.random() > 0.2,
      priceModifier: 0,
    }));
    const numSizes = Math.floor(Math.random() * 3) + 3;
    const selectedSizes = sizeOptions.slice(0, numSizes);
    const sizeVariants = selectedSizes.map((size, i) => ({
      id: `size-${product.sku}-${i}`,
      type: 'size' as const,
      name: size,
      value: size,
      inStock: Math.random() > 0.3,
      priceModifier: size === 'XXL' ? 5 : 0,
    }));
    const brands = ['Nike', 'Adidas', 'Puma', 'Reebok', 'Converse', 'New Balance'];
    const brand = brands[index % brands.length];
    const hasDiscount = Math.random() > 0.7;
    const originalPrice =
      hasDiscount ? Math.round(product.price * (1.2 + Math.random() * 0.3) * 100) / 100 : undefined;
    const isNew = Math.random() > 0.8;
    const isFeatured = Math.random() > 0.9;
    const isSale = hasDiscount;
    return {
      variants: [...colorVariants, ...sizeVariants],
      brand,
      originalPrice,
      isNew,
      isFeatured,
      isSale,
    };
  }

  protected mapOrderStatus(backendStatus: string): 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' {
    const status = (backendStatus || '').toUpperCase();
    switch (status) {
      case 'PENDING':
      case 'PROCESSING':
        return 'PENDING';
      case 'CONFIRMED':
      case 'CONFIRMED_ORDER':
        return 'CONFIRMED';
      case 'SHIPPED':
      case 'IN_TRANSIT':
        return 'SHIPPED';
      case 'DELIVERED':
      case 'COMPLETED':
        return 'DELIVERED';
      default:
        return 'PENDING';
    }
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

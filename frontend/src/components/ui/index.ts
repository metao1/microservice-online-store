/**
 * UI Components Index
 * Foundational UI components for the e-commerce redesign
 * Based on requirements 1.5, 3.2, 8.2
 */

// Button components
export { default as Button } from './Button';
export type { ButtonProps } from './Button';

// Input components
export { default as Input } from './Input';
export type { InputProps } from './Input';

// Badge components
export { default as Badge } from './Badge';
export type { BadgeProps } from './Badge';

// Skeleton components
export { 
  default as Skeleton,
  SkeletonText,
  SkeletonAvatar,
  SkeletonButton,
  SkeletonCard,
  SkeletonProductCard,
  SkeletonNavigation
} from './Skeleton';
export type { SkeletonProps } from './Skeleton';

// Example components
export { default as ProductCardExample } from './ProductCardExample';
/**
 * Skeleton Component
 * Professional skeleton loading component for various content types
 * Based on requirements 2.4, 7.7, 9.2
 */

import React from 'react';
import { cn } from '../../../styles/utils';
import './Skeleton.css';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Skeleton variant */
  variant?: 'text' | 'rectangular' | 'circular' | 'rounded';
  /** Skeleton width */
  width?: string | number;
  /** Skeleton height */
  height?: string | number;
  /** Animation type */
  animation?: 'pulse' | 'wave' | 'none';
  /** Number of lines for text skeleton */
  lines?: number;
  /** Aspect ratio for rectangular skeletons */
  aspectRatio?: string;
}

const Skeleton: React.FC<SkeletonProps> = ({
  variant = 'text',
  width,
  height,
  animation = 'pulse',
  lines = 1,
  aspectRatio,
  className,
  style,
  ...props
}) => {
  const skeletonStyle: React.CSSProperties = {
    ...style,
    width: typeof width === 'number' ? `${width}px` : width,
    height: typeof height === 'number' ? `${height}px` : height,
    aspectRatio: aspectRatio,
  };

  // For text skeleton with multiple lines
  if (variant === 'text' && lines > 1) {
    return (
      <div
        className={cn('skeleton-container', className)}
        style={skeletonStyle}
        {...props}
      >
        {Array.from({ length: lines }, (_, index) => (
          <div
            key={index}
            className={cn(
              'skeleton',
              'skeleton-text',
              `skeleton-${animation}`,
              index === lines - 1 && 'skeleton-text-last'
            )}
            style={{
              width: index === lines - 1 ? '75%' : '100%',
            }}
          />
        ))}
      </div>
    );
  }

  return (
    <div
      className={cn(
        'skeleton',
        `skeleton-${variant}`,
        `skeleton-${animation}`,
        className
      )}
      style={skeletonStyle}
      {...props}
    />
  );
};

// Predefined skeleton components for common use cases
export const SkeletonText: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="text" {...props} />
);

export const SkeletonAvatar: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="circular" width={40} height={40} {...props} />
);

export const SkeletonButton: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="rounded" width={100} height={40} {...props} />
);

export const SkeletonCard: React.FC<{ 
  showImage?: boolean; 
  showTitle?: boolean; 
  showDescription?: boolean;
  className?: string;
  'data-testid'?: string;
}> = ({ 
  showImage = true, 
  showTitle = true, 
  showDescription = true,
  className,
  'data-testid': testId,
  ...props
}) => (
  <div className={cn('skeleton-card', className)} data-testid={testId} {...props}>
    {showImage && (
      <Skeleton 
        variant="rectangular" 
        width="100%" 
        aspectRatio="4/3"
        className="skeleton-card-image"
      />
    )}
    <div className="skeleton-card-content">
      {showTitle && (
        <SkeletonText width="80%" height={20} className="skeleton-card-title" />
      )}
      {showDescription && (
        <SkeletonText lines={2} className="skeleton-card-description" />
      )}
    </div>
  </div>
);

export const SkeletonProductCard: React.FC<{ 
  className?: string;
  'data-testid'?: string;
}> = ({ className, 'data-testid': testId, ...props }) => (
  <div className={cn('skeleton-product-card', className)} data-testid={testId} {...props}>
    <Skeleton 
      variant="rectangular" 
      width="100%" 
      aspectRatio="4/5"
      className="skeleton-product-image"
    />
    <div className="skeleton-product-content">
      <SkeletonText width="60%" height={14} className="skeleton-product-brand" />
      <SkeletonText width="90%" height={16} className="skeleton-product-title" />
      <SkeletonText width="40%" height={18} className="skeleton-product-price" />
      <div className="skeleton-product-rating">
        <Skeleton variant="rectangular" width={80} height={12} />
      </div>
    </div>
  </div>
);

export const SkeletonNavigation: React.FC<{ 
  className?: string;
  'data-testid'?: string;
}> = ({ className, 'data-testid': testId, ...props }) => (
  <div className={cn('skeleton-navigation', className)} data-testid={testId} {...props}>
    <div className="skeleton-nav-left">
      <Skeleton variant="rectangular" width={120} height={32} />
    </div>
    <div className="skeleton-nav-center">
      <Skeleton variant="rounded" width={300} height={40} />
    </div>
    <div className="skeleton-nav-right">
      <Skeleton variant="circular" width={32} height={32} />
      <Skeleton variant="circular" width={32} height={32} />
    </div>
  </div>
);

export default Skeleton;
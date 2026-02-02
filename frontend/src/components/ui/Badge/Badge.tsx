/**
 * Badge Component
 * Professional badge component for cart count, sale indicators, and status
 * Based on requirements 1.5, 3.2, 8.2
 */

import React from 'react';
import { cn } from '../../../styles/utils';
import './Badge.css';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** Badge variant */
  variant?: 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info' | 'sale';
  /** Badge size */
  size?: 'sm' | 'md' | 'lg';
  /** Badge content */
  children?: React.ReactNode;
  /** Dot variant (shows only a dot without text) */
  dot?: boolean;
  /** Maximum count to display (shows "99+" if exceeded) */
  maxCount?: number;
  /** Count value for numeric badges */
  count?: number;
  /** Show badge even when count is 0 */
  showZero?: boolean;
}

const Badge: React.FC<BadgeProps> = ({
  variant = 'default',
  size = 'md',
  children,
  dot = false,
  maxCount = 99,
  count,
  showZero = false,
  className,
  ...props
}) => {
  // Handle count display logic
  const getDisplayContent = () => {
    if (dot) return null;
    
    if (typeof count === 'number') {
      if (count === 0 && !showZero) return null;
      if (count > maxCount) return `${maxCount}+`;
      return count.toString();
    }
    
    return children;
  };

  const displayContent = getDisplayContent();
  
  // Don't render if no content and not a dot
  if (!dot && !displayContent) return null;

  return (
    <span
      className={cn(
        'badge',
        `badge-${variant}`,
        `badge-${size}`,
        dot && 'badge-dot',
        className
      )}
      {...props}
    >
      {displayContent}
    </span>
  );
};

export default Badge;
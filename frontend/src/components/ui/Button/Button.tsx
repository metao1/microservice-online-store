/**
 * Button Component
 * Professional button component with multiple variants
 * Based on requirements 1.5, 3.2, 8.2
 */

import React, { forwardRef } from 'react';
import { cn } from '../../../styles/utils';
import './Button.css';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /** Button variant */
  variant?: 'primary' | 'secondary' | 'ghost' | 'outline';
  /** Button size */
  size?: 'sm' | 'md' | 'lg';
  /** Full width button */
  fullWidth?: boolean;
  /** Loading state */
  loading?: boolean;
  /** Icon to display before text */
  startIcon?: React.ReactNode;
  /** Icon to display after text */
  endIcon?: React.ReactNode;
  /** Children content */
  children?: React.ReactNode;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      size = 'md',
      fullWidth = false,
      loading = false,
      startIcon,
      endIcon,
      children,
      className,
      disabled,
      ...props
    },
    ref
  ) => {
    const isDisabled = disabled || loading;

    return (
      <button
        ref={ref}
        className={cn(
          'btn',
          `btn-${variant}`,
          `btn-${size}`,
          fullWidth && 'btn-full-width',
          loading && 'btn-loading',
          className
        )}
        disabled={isDisabled}
        aria-disabled={isDisabled}
        {...props}
      >
        {loading && (
          <span className="btn-spinner" aria-hidden="true">
            <svg
              className="btn-spinner-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeDasharray="31.416"
                strokeDashoffset="31.416"
              />
            </svg>
          </span>
        )}
        
        {!loading && startIcon && (
          <span className="btn-icon btn-icon-start" aria-hidden="true">
            {startIcon}
          </span>
        )}
        
        {children && (
          <span className={cn('btn-text', loading && 'btn-text-loading')}>
            {children}
          </span>
        )}
        
        {!loading && endIcon && (
          <span className="btn-icon btn-icon-end" aria-hidden="true">
            {endIcon}
          </span>
        )}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;

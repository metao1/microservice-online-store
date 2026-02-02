/**
 * Input Component
 * Professional input component with multiple variants
 * Based on requirements 1.3, 4.1, 5.1
 */

import React, { forwardRef } from 'react';
import { cn } from '../../../styles/utils';
import './Input.css';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  /** Input variant */
  variant?: 'default' | 'search' | 'filter';
  /** Input size */
  size?: 'sm' | 'md' | 'lg';
  /** Error state */
  error?: boolean;
  /** Error message */
  errorMessage?: string;
  /** Helper text */
  helperText?: string;
  /** Label text */
  label?: string;
  /** Icon to display before input */
  startIcon?: React.ReactNode;
  /** Icon to display after input */
  endIcon?: React.ReactNode;
  /** Full width input */
  fullWidth?: boolean;
  /** Loading state */
  loading?: boolean;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      variant = 'default',
      size = 'md',
      error = false,
      errorMessage,
      helperText,
      label,
      startIcon,
      endIcon,
      fullWidth = false,
      loading = false,
      className,
      id,
      ...props
    },
    ref
  ) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
    const errorId = error && errorMessage ? `${inputId}-error` : undefined;
    const helperId = helperText ? `${inputId}-helper` : undefined;

    return (
      <div className={cn('input-wrapper', fullWidth && 'input-wrapper-full-width')}>
        {label && (
          <label htmlFor={inputId} className="input-label">
            {label}
          </label>
        )}
        
        <div
          className={cn(
            'input-container',
            `input-container-${variant}`,
            `input-container-${size}`,
            error && 'input-container-error',
            loading && 'input-container-loading',
            startIcon && 'input-container-start-icon',
            endIcon && 'input-container-end-icon'
          )}
        >
          {startIcon && (
            <span className="input-icon input-icon-start" aria-hidden="true">
              {startIcon}
            </span>
          )}
          
          <input
            ref={ref}
            id={inputId}
            className={cn(
              'input',
              `input-${variant}`,
              `input-${size}`,
              error && 'input-error',
              className
            )}
            aria-invalid={error}
            aria-describedby={cn(errorId, helperId).trim() || undefined}
            {...props}
          />
          
          {loading && (
            <span className="input-spinner" aria-hidden="true">
              <svg
                className="input-spinner-icon"
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
          
          {!loading && endIcon && (
            <span className="input-icon input-icon-end" aria-hidden="true">
              {endIcon}
            </span>
          )}
        </div>
        
        {error && errorMessage && (
          <div id={errorId} className="input-error-message" role="alert">
            {errorMessage}
          </div>
        )}
        
        {!error && helperText && (
          <div id={helperId} className="input-helper-text">
            {helperText}
          </div>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
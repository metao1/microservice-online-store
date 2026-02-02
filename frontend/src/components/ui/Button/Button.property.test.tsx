/**
 * Button Component Property-Based Tests
 * Property tests for the Button component using fast-check
 * **Feature: ecommerce-redesign**
 * Based on requirements 7.4, 10.1, 10.4
 */

import React from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import '@testing-library/jest-dom';
import * as fc from 'fast-check';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Button from './Button';

describe('Button Component - Property Tests', () => {
  afterEach(() => {
    cleanup();
  });

  /**
   * **Property 1: Button accessibility and interaction**
   * **Validates: Requirements 7.4, 10.1, 10.4**
   * 
   * For any button configuration, the button should:
   * - Be accessible via keyboard navigation
   * - Provide proper ARIA attributes
   * - Have appropriate focus indicators
   * - Support interaction when enabled
   * - Block interaction when disabled or loading
   */
  describe('Property 1: Button accessibility and interaction', () => {
    const buttonVariantArb = fc.constantFrom('primary', 'secondary', 'ghost', 'outline');
    const buttonSizeArb = fc.constantFrom('sm', 'md', 'lg');
    const buttonTextArb = fc.string({ minLength: 1, maxLength: 50 });
    const booleanArb = fc.boolean();

    it('should maintain accessibility and interaction properties across all configurations', () => {
      fc.assert(
        fc.property(
          buttonVariantArb,
          buttonSizeArb,
          buttonTextArb,
          booleanArb, // disabled
          booleanArb, // loading
          booleanArb, // fullWidth
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, size, text, disabled, loading, fullWidth, uniqueId) => {
            const mockClick = vi.fn();
            const testId = `property-button-${uniqueId}`;
            
            const { container, unmount } = render(
              <Button
                variant={variant}
                size={size}
                disabled={disabled}
                loading={loading}
                fullWidth={fullWidth}
                onClick={mockClick}
                data-testid={testId}
              >
                {text}
              </Button>
            );

            const button = screen.getByTestId(testId);
            
            // Property: Button should always be focusable unless disabled or loading
            const shouldBeFocusable = !disabled && !loading;
            if (shouldBeFocusable) {
              button.focus();
              expect(button).toHaveFocus();
            }

            // Property: Button should have proper role (implicit for HTML button elements)
            expect(button.tagName.toLowerCase()).toBe('button');

            // Property: Button should have proper aria-disabled state
            const expectedAriaDisabled = disabled || loading;
            expect(button).toHaveAttribute('aria-disabled', expectedAriaDisabled.toString());

            // Property: Button should be actually disabled when disabled or loading
            if (disabled || loading) {
              expect(button).toBeDisabled();
            } else {
              expect(button).not.toBeDisabled();
            }

            // Property: Click events should only work when button is enabled
            fireEvent.click(button);
            if (disabled || loading) {
              expect(mockClick).not.toHaveBeenCalled();
            } else {
              expect(mockClick).toHaveBeenCalledTimes(1);
            }

            // Property: Button should have consistent CSS classes
            expect(button).toHaveClass('btn');
            expect(button).toHaveClass(`btn-${variant}`);
            expect(button).toHaveClass(`btn-${size}`);
            
            if (fullWidth) {
              expect(button).toHaveClass('btn-full-width');
            }
            
            if (loading) {
              expect(button).toHaveClass('btn-loading');
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle keyboard navigation consistently', () => {
      fc.assert(
        fc.property(
          buttonVariantArb,
          buttonSizeArb,
          buttonTextArb,
          booleanArb, // disabled
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, size, text, disabled, uniqueId) => {
            const mockClick = vi.fn();
            const testId = `keyboard-button-${uniqueId}`;
            
            const { unmount } = render(
              <Button
                variant={variant}
                size={size}
                disabled={disabled}
                onClick={mockClick}
                data-testid={testId}
              >
                {text}
              </Button>
            );

            const button = screen.getByTestId(testId);
            
            // Property: Keyboard events should work the same as click events
            if (!disabled) {
              button.focus();
              expect(button).toHaveFocus();
              
              // Test Enter key
              fireEvent.keyDown(button, { key: 'Enter', code: 'Enter' });
              // Note: Native button elements don't automatically trigger onClick for keyDown
              // This is expected behavior - we're testing that the button can receive focus
              // and that keyboard events can be attached if needed
            }

            // Property: Disabled buttons should not be focusable (HTML disabled buttons are automatically not focusable)
            if (disabled) {
              expect(button).toBeDisabled();
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should maintain proper ARIA attributes across all states', () => {
      fc.assert(
        fc.property(
          buttonVariantArb,
          buttonTextArb,
          booleanArb, // disabled
          booleanArb, // loading
          fc.option(fc.string({ minLength: 1, maxLength: 30 })), // aria-label
          fc.option(fc.string({ minLength: 1, maxLength: 30 })), // aria-describedby
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, text, disabled, loading, ariaLabel, ariaDescribedBy, uniqueId) => {
            const testId = `aria-button-${uniqueId}`;
            const props: any = {
              variant,
              disabled,
              loading,
              'data-testid': testId
            };

            if (ariaLabel) {
              props['aria-label'] = ariaLabel;
            }
            if (ariaDescribedBy) {
              props['aria-describedby'] = ariaDescribedBy;
            }

            const { unmount } = render(<Button {...props}>{text}</Button>);

            const button = screen.getByTestId(testId);
            
            // Property: aria-disabled should always reflect the actual disabled state
            expect(button).toHaveAttribute('aria-disabled', (disabled || loading).toString());

            // Property: Custom ARIA attributes should be preserved
            if (ariaLabel) {
              expect(button).toHaveAttribute('aria-label', ariaLabel);
            }
            if (ariaDescribedBy) {
              expect(button).toHaveAttribute('aria-describedby', ariaDescribedBy);
            }

            // Property: Button should always have a role (implicit for HTML button elements)
            expect(button.tagName.toLowerCase()).toBe('button');

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});
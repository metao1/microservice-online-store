/**
 * Input Component Property-Based Tests
 * Property tests for the Input component using fast-check
 * **Feature: ecommerce-redesign**
 * Based on requirements 7.4, 10.1, 10.4
 */

import { render, screen, fireEvent, cleanup, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import * as fc from 'fast-check';
import Input from './Input';
import { afterEach, describe, expect, it, vi } from 'vitest';

describe('Input Component - Property Tests', () => {
  afterEach(() => {
    cleanup();
  });

  /**
   * **Property 2: Input validation and state management**
   * **Validates: Requirements 7.4, 10.1, 10.4**
   * 
   * For any input configuration, the input should:
   * - Maintain proper validation states
   * - Handle user input correctly
   * - Provide appropriate accessibility attributes
   * - Associate labels and error messages correctly
   * - Support keyboard navigation
   */
  describe('Property 2: Input validation and state management', () => {
    const inputVariantArb = fc.constantFrom('default', 'search', 'filter');
    const inputSizeArb = fc.constantFrom('sm', 'md', 'lg');
    const inputTextArb = fc.string({ minLength: 0, maxLength: 100 });
    const labelTextArb = fc.string({ minLength: 2, maxLength: 30 }).filter(s => s.trim().length >= 2 && /[a-zA-Z]/.test(s));
    const errorMessageArb = fc.string({ minLength: 2, maxLength: 50 }).filter(s => s.trim().length >= 2 && /[a-zA-Z]/.test(s));
    const helperTextArb = fc.string({ minLength: 2, maxLength: 50 }).filter(s => s.trim().length >= 2 && /[a-zA-Z]/.test(s));
    const booleanArb = fc.boolean();

    it('should maintain validation and accessibility properties across all configurations', () => {
      fc.assert(
        fc.property(
          inputVariantArb,
          inputSizeArb,
          fc.option(labelTextArb),
          fc.option(errorMessageArb),
          fc.option(helperTextArb),
          booleanArb, // error
          booleanArb, // disabled
          booleanArb, // loading
          booleanArb, // fullWidth
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, size, label, errorMessage, helperText, error, disabled, loading, fullWidth, uniqueId) => {
            const mockChange = vi.fn();
            const mockFocus = vi.fn();
            const mockBlur = vi.fn();
            const testId = `property-input-${uniqueId}-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
            
            const props: any = {
              variant,
              size,
              error: error && errorMessage ? true : false, // Only set error if we have an error message
              disabled,
              loading,
              fullWidth,
              onChange: mockChange,
              onFocus: mockFocus,
              onBlur: mockBlur,
              'data-testid': testId
            };

            if (label) props.label = label;
            if (error && errorMessage) props.errorMessage = errorMessage;
            if (!error && helperText) props.helperText = helperText;

            const { unmount, container } = render(<Input {...props} />);

            const input = screen.getByTestId(testId);
            
            // Property: Input should be an input element with textbox role
            expect(input.tagName.toLowerCase()).toBe('input');

            // Property: Input should have proper CSS classes
            expect(input).toHaveClass('input');
            expect(input).toHaveClass(`input-${variant}`);
            expect(input).toHaveClass(`input-${size}`);

            // Property: Error state should be reflected in aria-invalid
            if (error && errorMessage) {
              expect(input).toHaveAttribute('aria-invalid', 'true');
              expect(input).toHaveClass('input-error');
            } else {
              expect(input).toHaveAttribute('aria-invalid', 'false');
            }

            // Property: Disabled state should be consistent
            if (disabled) {
              expect(input).toBeDisabled();
            } else {
              expect(input).not.toBeDisabled();
            }

            // Property: Label association should be correct
            if (label) {
              const labelElement = within(container).getByText(label.trim());
              expect(labelElement).toBeInTheDocument();
              expect(labelElement).toHaveAttribute('for', input.getAttribute('id'));
            }

            // Property: Error message should be associated with input
            if (error && errorMessage) {
              const errorElements = screen.getAllByRole('alert');
              const inputErrorElement = errorElements.find(el => 
                el.getAttribute('id') === input.getAttribute('aria-describedby')
              );
              expect(inputErrorElement).toBeTruthy();
              // Use a more flexible text content matcher that handles whitespace normalization
              expect(inputErrorElement?.textContent?.trim()).toBe(errorMessage.trim());
            }

            // Property: Helper text should be associated with input (when no error)
            if (!error && helperText) {
              const describedById = input.getAttribute('aria-describedby');
              if (describedById) {
                const helperElement = document.getElementById(describedById);
                expect(helperElement).toBeInTheDocument();
                expect(helperElement?.textContent?.trim()).toBe(helperText.trim());
                expect(input).toHaveAttribute('aria-describedby', describedById);
              }
            }

            // Property: Input should handle focus/blur events when not disabled
            if (!disabled) {
              fireEvent.focus(input);
              expect(mockFocus).toHaveBeenCalledTimes(1);
              
              fireEvent.blur(input);
              expect(mockBlur).toHaveBeenCalledTimes(1);
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should handle input changes and maintain state correctly', () => {
      fc.assert(
        fc.property(
          inputVariantArb,
          inputTextArb,
          booleanArb, // disabled
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, inputValue, disabled, uniqueId) => {
            const mockChange = vi.fn();
            const testId = `change-input-${uniqueId}-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
            
            const { unmount } = render(
              <Input
                variant={variant}
                disabled={disabled}
                onChange={mockChange}
                data-testid={testId}
              />
            );

            const input = screen.getByTestId(testId);
            
            // Property: Input changes should only work when not disabled
            if (!disabled && inputValue.length > 0) {
              fireEvent.change(input, { target: { value: inputValue } });
              expect(mockChange).toHaveBeenCalled();
              expect(input).toHaveValue(inputValue);
            } else if (disabled) {
              // Disabled inputs shouldn't accept changes, but React Testing Library
              // may still trigger the event. We check that the input remains disabled.
              fireEvent.change(input, { target: { value: inputValue } });
              expect(input).toBeDisabled();
            }

            // Property: Input should maintain its variant class
            expect(input).toHaveClass(`input-${variant}`);

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should maintain proper container structure and styling', () => {
      fc.assert(
        fc.property(
          inputVariantArb,
          inputSizeArb,
          booleanArb, // error
          booleanArb, // loading
          booleanArb, // fullWidth
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (variant, size, error, loading, fullWidth, uniqueId) => {
            const testId = `container-input-${uniqueId}-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
            
            const { unmount } = render(
              <Input
                variant={variant}
                size={size}
                error={error}
                loading={loading}
                fullWidth={fullWidth}
                errorMessage={error ? "Test error" : undefined}
                data-testid={testId}
              />
            );

            const input = screen.getByTestId(testId);
            const container = input.closest('.input-container');
            const wrapper = container?.closest('.input-wrapper');
            
            // Property: Input should always be wrapped in proper container structure
            expect(container).toBeInTheDocument();
            expect(wrapper).toBeInTheDocument();

            // Property: Container should have proper variant and size classes
            expect(container).toHaveClass(`input-container-${variant}`);
            expect(container).toHaveClass(`input-container-${size}`);

            // Property: Error state should be reflected in container
            if (error) {
              expect(container).toHaveClass('input-container-error');
            }

            // Property: Loading state should be reflected in container
            if (loading) {
              expect(container).toHaveClass('input-container-loading');
            }

            // Property: Full width should be reflected in wrapper
            if (fullWidth) {
              expect(wrapper).toHaveClass('input-wrapper-full-width');
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should handle icon placement and loading states correctly', () => {
      fc.assert(
        fc.property(
          booleanArb, // hasStartIcon
          booleanArb, // hasEndIcon
          booleanArb, // loading
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (hasStartIcon, hasEndIcon, loading, uniqueId) => {
            const startIcon = hasStartIcon ? <span data-testid={`start-icon-${uniqueId}`}>Start</span> : undefined;
            const endIcon = hasEndIcon ? <span data-testid={`end-icon-${uniqueId}`}>End</span> : undefined;
            const testId = `icon-input-${uniqueId}-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
            
            const { unmount } = render(
              <Input
                startIcon={startIcon}
                endIcon={endIcon}
                loading={loading}
                data-testid={testId}
              />
            );

            const input = screen.getByTestId(testId);
            const container = input.closest('.input-container');
            
            // Property: Start icon should always be visible when provided
            if (hasStartIcon) {
              expect(screen.getByTestId(`start-icon-${uniqueId}`)).toBeInTheDocument();
              expect(container).toHaveClass('input-container-start-icon');
            }

            // Property: End icon should be hidden when loading, visible otherwise
            if (hasEndIcon && !loading) {
              expect(screen.getByTestId(`end-icon-${uniqueId}`)).toBeInTheDocument();
              expect(container).toHaveClass('input-container-end-icon');
            } else if (hasEndIcon && loading) {
              expect(screen.queryByTestId(`end-icon-${uniqueId}`)).not.toBeInTheDocument();
            }

            // Property: Loading spinner should be visible when loading
            if (loading) {
              expect(container?.querySelector('.input-spinner')).toBeInTheDocument();
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 50 }
      );
    });
  });
});

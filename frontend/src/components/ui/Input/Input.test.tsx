/**
 * Input Component Tests
 * Unit tests for the Input component
 * Based on requirements 1.3, 4.1, 5.1, 10.1, 10.7
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { describe, expect, it, vi } from 'vitest';
import Input from './Input';

describe('Input Component', () => {
  // Basic rendering tests
  describe('Rendering', () => {
    it('renders input element', () => {
      render(<Input placeholder="Enter text" />);
      expect(screen.getByRole('textbox')).toBeInTheDocument();
    });

    it('renders with label', () => {
      render(<Input label="Username" />);
      expect(screen.getByLabelText('Username')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<Input className="custom-input" />);
      expect(screen.getByRole('textbox')).toHaveClass('custom-input');
    });
  });

  // Variant tests
  describe('Variants', () => {
    it('applies default variant', () => {
      render(<Input />);
      expect(screen.getByRole('textbox')).toHaveClass('input-default');
    });

    it('applies search variant', () => {
      render(<Input variant="search" />);
      expect(screen.getByRole('textbox')).toHaveClass('input-search');
    });

    it('applies filter variant', () => {
      render(<Input variant="filter" />);
      expect(screen.getByRole('textbox')).toHaveClass('input-filter');
    });
  });

  // Size tests
  describe('Sizes', () => {
    it('applies medium size by default', () => {
      render(<Input />);
      expect(screen.getByRole('textbox')).toHaveClass('input-md');
    });

    it('applies small size', () => {
      render(<Input size="sm" />);
      expect(screen.getByRole('textbox')).toHaveClass('input-sm');
    });

    it('applies large size', () => {
      render(<Input size="lg" />);
      expect(screen.getByRole('textbox')).toHaveClass('input-lg');
    });
  });

  // State tests
  describe('States', () => {
    it('handles error state', () => {
      render(<Input error errorMessage="This field is required" />);
      const input = screen.getByRole('textbox');
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(screen.getByRole('alert')).toHaveTextContent('This field is required');
    });

    it('handles loading state', () => {
      render(<Input loading />);
      expect(screen.getByRole('textbox').closest('.input-container')).toHaveClass('input-container-loading');
    });

    it('handles disabled state', () => {
      render(<Input disabled />);
      expect(screen.getByRole('textbox')).toBeDisabled();
    });
  });

  // Helper text tests
  describe('Helper Text', () => {
    it('renders helper text', () => {
      render(<Input helperText="Enter your username" />);
      expect(screen.getByText('Enter your username')).toBeInTheDocument();
    });

    it('prioritizes error message over helper text', () => {
      render(
        <Input 
          error 
          errorMessage="Error message" 
          helperText="Helper text" 
        />
      );
      expect(screen.getByText('Error message')).toBeInTheDocument();
      expect(screen.queryByText('Helper text')).not.toBeInTheDocument();
    });
  });

  // Icon tests
  describe('Icons', () => {
    const TestIcon = () => <span data-testid="test-icon">Icon</span>;

    it('renders start icon', () => {
      render(<Input startIcon={<TestIcon />} />);
      expect(screen.getByTestId('test-icon')).toBeInTheDocument();
    });

    it('renders end icon', () => {
      render(<Input endIcon={<TestIcon />} />);
      expect(screen.getByTestId('test-icon')).toBeInTheDocument();
    });

    it('hides end icon when loading', () => {
      render(<Input loading endIcon={<TestIcon />} />);
      expect(screen.queryByTestId('test-icon')).not.toBeInTheDocument();
    });
  });

  // Interaction tests
  describe('Interactions', () => {
    it('handles input changes', () => {
      const handleChange = vi.fn();
      render(<Input onChange={handleChange} />);
      
      const input = screen.getByRole('textbox');
      fireEvent.change(input, { target: { value: 'test input' } });
      
      expect(handleChange).toHaveBeenCalled();
      expect(input).toHaveValue('test input');
    });

    it('handles focus and blur events', () => {
      const handleFocus = vi.fn();
      const handleBlur = vi.fn();
      render(<Input onFocus={handleFocus} onBlur={handleBlur} />);
      
      const input = screen.getByRole('textbox');
      fireEvent.focus(input);
      expect(handleFocus).toHaveBeenCalled();
      
      fireEvent.blur(input);
      expect(handleBlur).toHaveBeenCalled();
    });
  });

  // Accessibility tests
  describe('Accessibility', () => {
    it('associates label with input', () => {
      render(<Input label="Email Address" />);
      const input = screen.getByRole('textbox');
      const label = screen.getByText('Email Address');
      
      expect(input).toHaveAttribute('id');
      expect(label).toHaveAttribute('for', input.getAttribute('id'));
    });

    it('associates error message with input', () => {
      render(<Input error errorMessage="Invalid email" />);
      const input = screen.getByRole('textbox');
      const errorMessage = screen.getByRole('alert');
      
      expect(input).toHaveAttribute('aria-describedby');
      expect(errorMessage).toHaveAttribute('id', input.getAttribute('aria-describedby'));
    });

    it('associates helper text with input', () => {
      render(<Input helperText="Enter a valid email address" />);
      const input = screen.getByRole('textbox');
      const helperText = screen.getByText('Enter a valid email address');
      
      expect(input).toHaveAttribute('aria-describedby');
      expect(helperText).toHaveAttribute('id', input.getAttribute('aria-describedby'));
    });

    it('supports keyboard navigation', () => {
      render(<Input />);
      const input = screen.getByRole('textbox');
      
      input.focus();
      expect(input).toHaveFocus();
    });
  });

  // Forward ref test
  describe('Forward Ref', () => {
    it('forwards ref to input element', () => {
      const ref = React.createRef<HTMLInputElement>();
      render(<Input ref={ref} />);
      
      expect(ref.current).toBeInstanceOf(HTMLInputElement);
      expect(ref.current).toBe(screen.getByRole('textbox'));
    });
  });
});
/**
 * Container Component - E-commerce Redesign
 * Responsive container with max-widths for different breakpoints
 * Based on requirements 2.1, 2.6
 */

import React from 'react';
import './Container.css';

export interface ContainerProps {
  /** Content to be contained */
  children: React.ReactNode;
  /** Container size variant */
  size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | 'full';
  /** Whether to center the container */
  centered?: boolean;
  /** Additional CSS classes */
  className?: string;
  /** Custom padding override */
  padding?: 'none' | 'sm' | 'md' | 'lg';
  /** HTML element type */
  as?: keyof JSX.IntrinsicElements;
  /** Additional props to pass to the container element */
  [key: string]: any;
}

/**
 * Container component provides responsive max-widths and consistent padding
 * 
 * Features:
 * - Responsive max-widths based on design tokens
 * - Configurable padding options
 * - Centered by default with auto margins
 * - Flexible element type with 'as' prop
 * - Full width option for edge-to-edge layouts
 * 
 * @example
 * ```tsx
 * <Container>
 *   <h1>Page Content</h1>
 * </Container>
 * 
 * <Container size="lg" padding="lg">
 *   <ProductGrid />
 * </Container>
 * 
 * <Container size="full" padding="none" as="section">
 *   <HeroSection />
 * </Container>
 * ```
 */
export const Container: React.FC<ContainerProps> = ({
  children,
  size = 'xl',
  centered = true,
  className = '',
  padding = 'md',
  as: Element = 'div',
  ...props
}) => {
  const containerClasses = [
    'container-component',
    `container-${size}`,
    `container-padding-${padding}`,
    centered && 'container-centered',
    className
  ].filter(Boolean).join(' ');

  return (
    <Element 
      className={containerClasses}
      {...props}
    >
      {children}
    </Element>
  );
};

Container.displayName = 'Container';
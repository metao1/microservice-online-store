/**
 * Grid Component - E-commerce Redesign
 * Responsive CSS Grid component for product layouts
 * Based on requirements 2.1, 2.6
 */

import React from 'react';
import './Grid.css';

export interface GridProps {
  /** Content to be arranged in grid */
  children: React.ReactNode;
  /** Number of columns for different breakpoints */
  columns?: {
    xs?: number;
    sm?: number;
    md?: number;
    lg?: number;
    xl?: number;
  };
  /** Gap between grid items */
  gap?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /** Align items within grid cells */
  alignItems?: 'start' | 'center' | 'end' | 'stretch';
  /** Justify items within grid cells */
  justifyItems?: 'start' | 'center' | 'end' | 'stretch';
  /** Additional CSS classes */
  className?: string;
  /** HTML element type */
  as?: keyof JSX.IntrinsicElements;
  /** Auto-fit columns with minimum width */
  autoFit?: string;
  /** Auto-fill columns with minimum width */
  autoFill?: string;
  /** Additional props to pass to the grid element */
  [key: string]: any;
}

/**
 * Grid component provides responsive CSS Grid layouts optimized for e-commerce
 * 
 * Features:
 * - Responsive column counts for different breakpoints
 * - Configurable gap sizes using design tokens
 * - Auto-fit and auto-fill options for dynamic layouts
 * - Alignment and justification controls
 * - Optimized for product card layouts
 * 
 * @example
 * ```tsx
 * // Product grid with responsive columns
 * <Grid columns={{ xs: 2, md: 3, xl: 4 }} gap="md">
 *   {products.map(product => <ProductCard key={product.id} product={product} />)}
 * </Grid>
 * 
 * // Auto-fit grid with minimum column width
 * <Grid autoFit="280px" gap="lg">
 *   {products.map(product => <ProductCard key={product.id} product={product} />)}
 * </Grid>
 * 
 * // Custom grid for layout sections
 * <Grid columns={{ xs: 1, lg: 2 }} gap="xl" alignItems="center">
 *   <div>Content 1</div>
 *   <div>Content 2</div>
 * </Grid>
 * ```
 */
export const Grid: React.FC<GridProps> = ({
  children,
  columns = { xs: 2, md: 3, xl: 4 },
  gap = 'md',
  alignItems = 'stretch',
  justifyItems = 'stretch',
  className = '',
  as: Element = 'div',
  autoFit,
  autoFill,
  ...props
}) => {
  const gridClasses = [
    'grid-component',
    `grid-gap-${gap}`,
    `grid-align-${alignItems}`,
    `grid-justify-${justifyItems}`,
    autoFit && 'grid-auto-fit',
    autoFill && 'grid-auto-fill',
    className
  ].filter(Boolean).join(' ');

  // Generate responsive column classes
  const columnClasses = Object.entries(columns)
    .map(([breakpoint, count]) => `grid-cols-${breakpoint}-${count}`)
    .join(' ');

  const finalClasses = `${gridClasses} ${columnClasses}`.trim();

  const style: React.CSSProperties = {
    ...(autoFit && { gridTemplateColumns: `repeat(auto-fit, minmax(${autoFit}, 1fr))` }),
    ...(autoFill && { gridTemplateColumns: `repeat(auto-fill, minmax(${autoFill}, 1fr))` }),
  };

  return (
    <Element 
      className={finalClasses}
      style={style}
      {...props}
    >
      {children}
    </Element>
  );
};

Grid.displayName = 'Grid';
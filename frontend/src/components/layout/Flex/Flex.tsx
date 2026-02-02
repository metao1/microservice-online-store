/**
 * Flex Component - E-commerce Redesign
 * Flexible Flexbox utility component for alignment and spacing
 * Based on requirements 2.1, 2.6
 */

import React from 'react';
import './Flex.css';

export interface FlexProps {
  /** Content to be arranged with flexbox */
  children: React.ReactNode;
  /** Flex direction */
  direction?: 'row' | 'column' | 'row-reverse' | 'column-reverse';
  /** Flex wrap behavior */
  wrap?: 'nowrap' | 'wrap' | 'wrap-reverse';
  /** Justify content (main axis alignment) */
  justify?: 'start' | 'end' | 'center' | 'between' | 'around' | 'evenly';
  /** Align items (cross axis alignment) */
  align?: 'start' | 'end' | 'center' | 'baseline' | 'stretch';
  /** Gap between flex items */
  gap?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /** Flex grow behavior */
  grow?: boolean;
  /** Flex shrink behavior */
  shrink?: boolean;
  /** Additional CSS classes */
  className?: string;
  /** HTML element type */
  as?: keyof JSX.IntrinsicElements;
  /** Responsive direction changes */
  responsive?: {
    xs?: FlexProps['direction'];
    sm?: FlexProps['direction'];
    md?: FlexProps['direction'];
    lg?: FlexProps['direction'];
    xl?: FlexProps['direction'];
  };
  /** Additional props to pass to the flex element */
  [key: string]: any;
}

/**
 * Flex component provides flexible Flexbox layouts with responsive controls
 * 
 * Features:
 * - Full flexbox property control
 * - Responsive direction changes
 * - Gap spacing using design tokens
 * - Common alignment patterns
 * - Grow and shrink controls
 * 
 * @example
 * ```tsx
 * // Basic horizontal layout
 * <Flex justify="between" align="center" gap="md">
 *   <div>Left content</div>
 *   <div>Right content</div>
 * </Flex>
 * 
 * // Responsive column to row
 * <Flex 
 *   direction="column" 
 *   responsive={{ md: 'row' }}
 *   gap="lg"
 * >
 *   <div>Item 1</div>
 *   <div>Item 2</div>
 * </Flex>
 * 
 * // Navigation layout
 * <Flex as="nav" justify="between" align="center">
 *   <Logo />
 *   <NavigationLinks />
 *   <UserActions />
 * </Flex>
 * ```
 */
export const Flex: React.FC<FlexProps> = ({
  children,
  direction = 'row',
  wrap = 'nowrap',
  justify = 'start',
  align = 'stretch',
  gap = 'md',
  grow = false,
  shrink = true,
  className = '',
  as: Element = 'div',
  responsive,
  ...props
}) => {
  const flexClasses = [
    'flex-component',
    `flex-direction-${direction}`,
    `flex-wrap-${wrap}`,
    `flex-justify-${justify}`,
    `flex-align-${align}`,
    `flex-gap-${gap}`,
    grow && 'flex-grow',
    !shrink && 'flex-no-shrink',
    className
  ].filter(Boolean).join(' ');

  // Generate responsive direction classes
  const responsiveClasses = responsive 
    ? Object.entries(responsive)
        .map(([breakpoint, dir]) => `flex-${breakpoint}-${dir}`)
        .join(' ')
    : '';

  const finalClasses = `${flexClasses} ${responsiveClasses}`.trim();

  return (
    <Element 
      className={finalClasses}
      {...props}
    >
      {children}
    </Element>
  );
};

Flex.displayName = 'Flex';
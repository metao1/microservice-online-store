/**
 * Design System Utilities
 * Helper functions for working with the design system
 * Based on requirements 7.1, 7.2, 7.3
 */

import { designTokens } from './design-tokens';

// CSS class name utilities
export const cn = (...classes: (string | undefined | null | false)[]): string => {
  return classes.filter(Boolean).join(' ');
};

// Responsive utilities
export const getResponsiveClasses = (
  base: string,
  sm?: string,
  md?: string,
  lg?: string,
  xl?: string
): string => {
  const classes = [base];
  
  if (sm) classes.push(`sm:${sm}`);
  if (md) classes.push(`md:${md}`);
  if (lg) classes.push(`lg:${lg}`);
  if (xl) classes.push(`xl:${xl}`);
  
  return classes.join(' ');
};

// Spacing utilities
export const getSpacingClass = (
  property: 'p' | 'm' | 'px' | 'py' | 'pt' | 'pb' | 'pl' | 'pr' | 'mx' | 'my' | 'mt' | 'mb' | 'ml' | 'mr',
  size: keyof typeof designTokens.spacing
): string => {
  return `${property}-${size}`;
};

// Typography utilities
export const getTextClass = (
  size: keyof typeof designTokens.typography.fontSize,
  weight?: keyof typeof designTokens.typography.fontWeight,
  color?: string
): string => {
  const classes = [`text-${size}`];
  
  if (weight) classes.push(`font-${weight}`);
  if (color) classes.push(`text-${color}`);
  
  return classes.join(' ');
};

// Color utilities
export const getColorClass = (
  type: 'bg' | 'text' | 'border',
  color: string
): string => {
  return `${type}-${color}`;
};

// Layout utilities
export const getFlexClasses = (
  direction?: 'row' | 'col',
  justify?: 'start' | 'end' | 'center' | 'between' | 'around' | 'evenly',
  align?: 'start' | 'end' | 'center' | 'baseline' | 'stretch',
  wrap?: 'wrap' | 'nowrap',
  gap?: keyof typeof designTokens.spacing
): string => {
  const classes = ['d-flex'];
  
  if (direction) classes.push(`flex-${direction}`);
  if (justify) classes.push(`justify-${justify}`);
  if (align) classes.push(`align-${align}`);
  if (wrap) classes.push(`flex-${wrap}`);
  if (gap) classes.push(`gap-${gap}`);
  
  return classes.join(' ');
};

// Grid utilities
export const getGridClasses = (
  cols?: number,
  gap?: keyof typeof designTokens.spacing,
  responsive?: {
    sm?: number;
    md?: number;
    lg?: number;
    xl?: number;
  }
): string => {
  const classes = ['d-grid'];
  
  if (cols) classes.push(`grid-cols-${cols}`);
  if (gap) classes.push(`gap-${gap}`);
  
  if (responsive) {
    if (responsive.sm) classes.push(`sm:grid-cols-${responsive.sm}`);
    if (responsive.md) classes.push(`md:grid-cols-${responsive.md}`);
    if (responsive.lg) classes.push(`lg:grid-cols-${responsive.lg}`);
    if (responsive.xl) classes.push(`xl:grid-cols-${responsive.xl}`);
  }
  
  return classes.join(' ');
};

// Button utilities
export const getButtonClasses = (
  variant: 'primary' | 'secondary' | 'ghost' | 'outline' = 'primary',
  size: 'sm' | 'md' | 'lg' = 'md',
  fullWidth?: boolean,
  disabled?: boolean
): string => {
  const classes = ['btn'];
  
  // Add variant classes (these would be defined in component CSS)
  classes.push(`btn-${variant}`);
  classes.push(`btn-${size}`);
  
  if (fullWidth) classes.push('w-100');
  if (disabled) classes.push('disabled');
  
  return classes.join(' ');
};

// Card utilities
export const getCardClasses = (
  padding?: 'sm' | 'md' | 'lg',
  shadow?: keyof typeof designTokens.shadows,
  border?: boolean
): string => {
  const classes = ['card'];
  
  if (padding) classes.push(`card-${padding}`);
  if (shadow) classes.push(`shadow-${shadow}`);
  if (border) classes.push('border');
  
  return classes.join(' ');
};

// Responsive visibility utilities
export const getVisibilityClasses = (
  hideOn?: ('xs' | 'sm' | 'md' | 'lg' | 'xl')[],
  showOn?: ('xs' | 'sm' | 'md' | 'lg' | 'xl')[]
): string => {
  const classes: string[] = [];
  
  if (hideOn) {
    hideOn.forEach(breakpoint => {
      classes.push(`hide-${breakpoint}`);
    });
  }
  
  if (showOn) {
    showOn.forEach(breakpoint => {
      classes.push(`show-${breakpoint}-only`);
    });
  }
  
  return classes.join(' ');
};

// Animation utilities
export const getTransitionClass = (
  property: 'all' | 'colors' | 'opacity' | 'transform' = 'all',
  duration: keyof typeof designTokens.animation.duration = 'base'
): string => {
  return `transition-${property} duration-${duration}`;
};

// Accessibility utilities
export const getAccessibilityClasses = (
  focusRing?: boolean,
  screenReaderOnly?: boolean,
  touchTarget?: boolean
): string => {
  const classes: string[] = [];
  
  if (focusRing) classes.push('focus-ring');
  if (screenReaderOnly) classes.push('sr-only');
  if (touchTarget) classes.push('touch-target');
  
  return classes.join(' ');
};

// CSS custom property utilities
export const getCSSVariable = (tokenPath: string): string => {
  return `var(--${tokenPath.replace(/\./g, '-')})`;
};

export const setCSSVariable = (element: HTMLElement, property: string, value: string): void => {
  element.style.setProperty(`--${property}`, value);
};

export const getCSSVariableValue = (element: HTMLElement, property: string): string => {
  return getComputedStyle(element).getPropertyValue(`--${property}`).trim();
};

// Breakpoint utilities for JavaScript
export const breakpoints = {
  xs: 320,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200,
  '2xl': 1400,
};

export const isBreakpoint = (breakpoint: keyof typeof breakpoints): boolean => {
  return window.innerWidth >= breakpoints[breakpoint];
};

export const getCurrentBreakpoint = (): keyof typeof breakpoints => {
  const width = window.innerWidth;
  
  if (width >= breakpoints['2xl']) return '2xl';
  if (width >= breakpoints.xl) return 'xl';
  if (width >= breakpoints.lg) return 'lg';
  if (width >= breakpoints.md) return 'md';
  if (width >= breakpoints.sm) return 'sm';
  return 'xs';
};

// Media query utilities for JavaScript
export const matchMedia = (query: string): boolean => {
  return window.matchMedia(query).matches;
};

export const isMobile = (): boolean => {
  return matchMedia(`(max-width: ${breakpoints.md - 1}px)`);
};

export const isTablet = (): boolean => {
  return matchMedia(`(min-width: ${breakpoints.md}px) and (max-width: ${breakpoints.lg - 1}px)`);
};

export const isDesktop = (): boolean => {
  return matchMedia(`(min-width: ${breakpoints.lg}px)`);
};

// Theme utilities
export const applyTheme = (theme: 'light' | 'dark' = 'light'): void => {
  document.documentElement.setAttribute('data-theme', theme);
};

export const getTheme = (): 'light' | 'dark' => {
  return document.documentElement.getAttribute('data-theme') as 'light' | 'dark' || 'light';
};

export const toggleTheme = (): void => {
  const currentTheme = getTheme();
  applyTheme(currentTheme === 'light' ? 'dark' : 'light');
};

// Accessibility utilities
export const announceToScreenReader = (message: string): void => {
  const announcement = document.createElement('div');
  announcement.setAttribute('aria-live', 'polite');
  announcement.setAttribute('aria-atomic', 'true');
  announcement.className = 'sr-only';
  announcement.textContent = message;
  
  document.body.appendChild(announcement);
  
  setTimeout(() => {
    document.body.removeChild(announcement);
  }, 1000);
};

export const trapFocus = (element: HTMLElement): (() => void) => {
  const focusableElements = element.querySelectorAll(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  ) as NodeListOf<HTMLElement>;
  
  const firstElement = focusableElements[0];
  const lastElement = focusableElements[focusableElements.length - 1];
  
  const handleTabKey = (e: KeyboardEvent) => {
    if (e.key !== 'Tab') return;
    
    if (e.shiftKey) {
      if (document.activeElement === firstElement) {
        lastElement.focus();
        e.preventDefault();
      }
    } else {
      if (document.activeElement === lastElement) {
        firstElement.focus();
        e.preventDefault();
      }
    }
  };
  
  element.addEventListener('keydown', handleTabKey);
  firstElement?.focus();
  
  return () => {
    element.removeEventListener('keydown', handleTabKey);
  };
};

// Performance utilities
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};

// Validation utilities
export const isValidColor = (color: string): boolean => {
  const style = new Option().style;
  style.color = color;
  return style.color !== '';
};

export const isValidCSSUnit = (value: string): boolean => {
  const cssUnitRegex = /^-?\d*\.?\d+(px|em|rem|%|vh|vw|vmin|vmax|ex|ch|cm|mm|in|pt|pc)$/;
  return cssUnitRegex.test(value);
};

// Export all utilities as a single object
export const designSystemUtils = {
  cn,
  getResponsiveClasses,
  getSpacingClass,
  getTextClass,
  getColorClass,
  getFlexClasses,
  getGridClasses,
  getButtonClasses,
  getCardClasses,
  getVisibilityClasses,
  getTransitionClass,
  getAccessibilityClasses,
  getCSSVariable,
  setCSSVariable,
  getCSSVariableValue,
  isBreakpoint,
  getCurrentBreakpoint,
  matchMedia,
  isMobile,
  isTablet,
  isDesktop,
  applyTheme,
  getTheme,
  toggleTheme,
  announceToScreenReader,
  trapFocus,
  debounce,
  throttle,
  isValidColor,
  isValidCSSUnit,
};

export default designSystemUtils;
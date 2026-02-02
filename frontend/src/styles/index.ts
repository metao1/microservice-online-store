/**
 * Design System - Main Export
 * Centralized export for all design system components
 * Based on requirements 7.1, 7.2, 7.3
 */

// Export design tokens
export { designTokens, getColor, getSpacing, getFontSize, getBreakpoint, mediaQueries } from './design-tokens';
export type { DesignTokens, ColorTokens, TypographyTokens, SpacingTokens, BreakpointTokens } from './design-tokens';

// Export utilities
export { 
  designSystemUtils, 
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
  isValidCSSUnit 
} from './utils';

// CSS imports (these need to be imported in the main application)
// Import these in your main CSS file or component:
// @import './design-tokens.css';
// @import './utilities.css';
// @import './responsive.css';

/**
 * Usage Examples:
 * 
 * // Import design tokens
 * import { designTokens, getColor, getSpacing } from '@/styles';
 * 
 * // Use in components
 * const primaryColor = getColor('primary');
 * const spacing = getSpacing('4');
 * 
 * // Import utilities
 * import { cn, getFlexClasses, isMobile } from '@/styles';
 * 
 * // Use utility classes
 * const flexClasses = getFlexClasses('row', 'between', 'center', 'nowrap', '4');
 * const combinedClasses = cn('base-class', isMobile() && 'mobile-class');
 * 
 * // Import CSS in your main CSS file:
 * // @import './styles/index.css';
 */
/**
 * Design Tokens - TypeScript Export
 * Design system tokens for JavaScript/TypeScript usage
 * Based on requirements 7.1, 7.2, 7.3
 */

export const designTokens = {
  // Colors
  colors: {
    primary: '#000000',
    primaryHover: '#333333',
    primaryActive: '#1a1a1a',
    secondary: '#ffffff',
    secondaryHover: '#f8f8f8',
    secondaryActive: '#f0f0f0',
    
    // Neutral colors
    neutral: {
      50: '#fafafa',
      100: '#f5f5f5',
      200: '#eeeeee',
      300: '#e0e0e0',
      400: '#bdbdbd',
      500: '#9e9e9e',
      600: '#757575',
      700: '#616161',
      800: '#424242',
      900: '#212121',
    },
    
    // Semantic colors
    success: '#27ae60',
    successLight: '#2ecc71',
    successDark: '#229954',
    error: '#e74c3c',
    errorLight: '#ec7063',
    errorDark: '#c0392b',
    warning: '#f39c12',
    warningLight: '#f4d03f',
    warningDark: '#d68910',
    info: '#3498db',
    infoLight: '#5dade2',
    infoDark: '#2980b9',
    
    // Background colors
    background: '#ffffff',
    backgroundAlt: '#fafafa',
    backgroundMuted: '#f5f5f5',
    
    // Text colors
    text: {
      primary: '#000000',
      secondary: '#666666',
      muted: '#999999',
      inverse: '#ffffff',
    },
    
    // Border colors
    border: '#e0e0e0',
    borderLight: '#f1f1f1',
    borderDark: '#bdbdbd',
    borderFocus: '#000000',
  },
  
  // Typography
  typography: {
    fontFamily: {
      primary: "'Helvetica Neue', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif",
      secondary: "'Helvetica Neue', Arial, sans-serif",
      mono: "'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace",
    },
    
    fontSize: {
      xs: '0.75rem',    // 12px
      sm: '0.875rem',   // 14px
      base: '1rem',     // 16px
      lg: '1.125rem',   // 18px
      xl: '1.25rem',    // 20px
      '2xl': '1.5rem',  // 24px
      '3xl': '1.875rem', // 30px
      '4xl': '2.25rem', // 36px
      '5xl': '3rem',    // 48px
      '6xl': '3.75rem', // 60px
    },
    
    fontWeight: {
      light: 300,
      normal: 400,
      medium: 500,
      semibold: 600,
      bold: 700,
      extrabold: 800,
    },
    
    lineHeight: {
      tight: 1.25,
      snug: 1.375,
      normal: 1.5,
      relaxed: 1.625,
      loose: 2,
    },
    
    letterSpacing: {
      tighter: '-0.05em',
      tight: '-0.025em',
      normal: '0',
      wide: '0.025em',
      wider: '0.05em',
      widest: '0.1em',
    },
  },
  
  // Spacing
  spacing: {
    0: '0',
    1: '0.25rem',   // 4px
    2: '0.5rem',    // 8px
    3: '0.75rem',   // 12px
    4: '1rem',      // 16px
    5: '1.25rem',   // 20px
    6: '1.5rem',    // 24px
    8: '2rem',      // 32px
    10: '2.5rem',   // 40px
    12: '3rem',     // 48px
    16: '4rem',     // 64px
    20: '5rem',     // 80px
    24: '6rem',     // 96px
    32: '8rem',     // 128px
  },
  
  // Breakpoints
  breakpoints: {
    xs: '320px',
    sm: '576px',
    md: '768px',
    lg: '992px',
    xl: '1200px',
    '2xl': '1400px',
  },
  
  // Container max-widths
  container: {
    sm: '540px',
    md: '720px',
    lg: '960px',
    xl: '1140px',
    '2xl': '1320px',
  },
  
  // Shadows
  shadows: {
    xs: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    sm: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
    '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
    inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
    none: '0 0 #0000',
  },
  
  // Border radius
  borderRadius: {
    none: '0',
    sm: '0.125rem',   // 2px
    base: '0.25rem',  // 4px
    md: '0.375rem',   // 6px
    lg: '0.5rem',     // 8px
    xl: '0.75rem',    // 12px
    '2xl': '1rem',    // 16px
    full: '9999px',
  },
  
  // Transitions
  transitions: {
    fast: '150ms ease',
    base: '200ms ease',
    slow: '300ms ease',
    slower: '500ms ease',
  },
  
  // Z-index scale
  zIndex: {
    hide: -1,
    auto: 'auto',
    base: 0,
    docked: 10,
    dropdown: 1000,
    sticky: 1020,
    banner: 1030,
    overlay: 1040,
    modal: 1050,
    popover: 1060,
    skiplink: 1070,
    toast: 1080,
    tooltip: 1090,
  },
  
  // Component tokens
  components: {
    button: {
      height: {
        sm: '2rem',
        md: '2.5rem',
        lg: '3rem',
      },
      paddingX: {
        sm: '0.75rem',
        md: '1rem',
        lg: '1.5rem',
      },
    },
    
    input: {
      height: {
        sm: '2rem',
        md: '2.5rem',
        lg: '3rem',
      },
      paddingX: '0.75rem',
      borderWidth: '1px',
    },
    
    card: {
      padding: '1rem',
      paddingSm: '0.75rem',
      paddingLg: '1.5rem',
      borderWidth: '1px',
      borderRadius: '0.25rem',
    },
    
    navigation: {
      height: '60px',
      paddingX: '1rem',
      itemPaddingX: '1rem',
      itemPaddingY: '0.75rem',
    },
    
    productGrid: {
      aspectRatio: '4/5',
      gap: '1rem',
      gapSm: '0.75rem',
    },
  },
  
  // Accessibility
  accessibility: {
    focusRing: {
      width: '2px',
      offset: '2px',
      color: '#000000',
      opacity: 0.5,
    },
    touchTarget: {
      min: '44px',
    },
  },
  
  // Animation
  animation: {
    easing: {
      in: 'cubic-bezier(0.4, 0, 1, 1)',
      out: 'cubic-bezier(0, 0, 0.2, 1)',
      inOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
      bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
    },
    duration: {
      fast: '150ms',
      base: '200ms',
      slow: '300ms',
      slower: '500ms',
    },
  },
} as const;

// Type definitions for design tokens
export type DesignTokens = typeof designTokens;
export type ColorTokens = typeof designTokens.colors;
export type TypographyTokens = typeof designTokens.typography;
export type SpacingTokens = typeof designTokens.spacing;
export type BreakpointTokens = typeof designTokens.breakpoints;

// Utility functions for accessing design tokens
export const getColor = (path: string): string => {
  const keys = path.split('.');
  let value: any = designTokens.colors;
  
  for (const key of keys) {
    value = value?.[key];
  }
  
  return value || '#000000';
};

export const getSpacing = (key: keyof SpacingTokens): string => {
  return designTokens.spacing[key] || '0';
};

export const getFontSize = (key: keyof TypographyTokens['fontSize']): string => {
  return designTokens.typography.fontSize[key] || '1rem';
};

export const getBreakpoint = (key: keyof BreakpointTokens): string => {
  return designTokens.breakpoints[key] || '0px';
};

// Media query helpers
export const mediaQueries = {
  xs: `@media (max-width: ${designTokens.breakpoints.sm})`,
  sm: `@media (min-width: ${designTokens.breakpoints.sm})`,
  md: `@media (min-width: ${designTokens.breakpoints.md})`,
  lg: `@media (min-width: ${designTokens.breakpoints.lg})`,
  xl: `@media (min-width: ${designTokens.breakpoints.xl})`,
  '2xl': `@media (min-width: ${designTokens.breakpoints['2xl']})`,
  
  // Between breakpoints
  smOnly: `@media (min-width: ${designTokens.breakpoints.sm}) and (max-width: calc(${designTokens.breakpoints.md} - 1px))`,
  mdOnly: `@media (min-width: ${designTokens.breakpoints.md}) and (max-width: calc(${designTokens.breakpoints.lg} - 1px))`,
  lgOnly: `@media (min-width: ${designTokens.breakpoints.lg}) and (max-width: calc(${designTokens.breakpoints.xl} - 1px))`,
  xlOnly: `@media (min-width: ${designTokens.breakpoints.xl}) and (max-width: calc(${designTokens.breakpoints['2xl']} - 1px))`,
  
  // Special media queries
  mobile: `@media (max-width: calc(${designTokens.breakpoints.md} - 1px))`,
  tablet: `@media (min-width: ${designTokens.breakpoints.md}) and (max-width: calc(${designTokens.breakpoints.lg} - 1px))`,
  desktop: `@media (min-width: ${designTokens.breakpoints.lg})`,
  
  // Accessibility media queries
  reducedMotion: '@media (prefers-reduced-motion: reduce)',
  highContrast: '@media (prefers-contrast: high)',
  darkMode: '@media (prefers-color-scheme: dark)',
};

export default designTokens;
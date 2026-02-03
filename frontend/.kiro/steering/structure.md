# Project Structure

## Directory Organization

```
src/
├── components/          # Reusable UI components
│   ├── ui/             # Base UI components (Button, Input, etc.)
│   ├── layout/         # Layout components (Grid, Container)
│   └── *.tsx           # Feature components (Navigation, ProductCard)
├── pages/              # Route-level page components
├── context/            # React Context providers (Auth, Cart)
├── hooks/              # Custom React hooks
├── services/           # API and external service integrations
├── styles/             # Global styles and design tokens
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
└── test/               # Test setup and utilities
```

## Component Architecture

### UI Components (`src/components/ui/`)
- **Atomic Design**: Base components like Button, Input, Badge
- **Self-Contained**: Each component has its own folder with `.tsx`, `.css`, `.test.tsx`, and `index.ts`
- **Property-Based Testing**: Components include `.property.test.tsx` files using fast-check

### Feature Components (`src/components/`)
- **Business Logic**: Components like Navigation, ProductCard, ErrorBoundary
- **Comprehensive Testing**: Both unit tests and property-based tests
- **CSS Modules**: Component-specific styling with `.css` files

### Pages (`src/pages/`)
- **Route Components**: Top-level components for each route
- **Layout Integration**: Use layout components and feature components
- **Responsive Design**: Mobile-first CSS with breakpoint-specific styles

## Styling Architecture

### Design System
- **Design Tokens**: Centralized in `src/styles/design-tokens.ts` and `design-tokens.css`
- **Responsive Utilities**: Breakpoint-based responsive classes
- **Component Tokens**: Specific tokens for component sizing and spacing

### CSS Organization
- **Global Styles**: `src/styles/index.css` imports all global styles
- **Component Styles**: Co-located with components
- **Bootstrap Integration**: Bootstrap CSS imported globally with custom overrides

## Testing Structure

### Unit Tests
- **Location**: Co-located with components (`.test.tsx`)
- **Framework**: Vitest with React Testing Library
- **Setup**: Global test setup in `src/test/setup.ts`

### Property-Based Tests
- **Location**: Co-located with components (`.property.test.tsx`)
- **Framework**: fast-check for property-based testing
- **Purpose**: Test component behavior across wide input ranges

### E2E Tests
- **Location**: `e2e/` directory
- **Framework**: Playwright
- **Coverage**: Full user workflows and page interactions

## State Management

### Context Providers
- **AuthContext**: User authentication state
- **CartContext**: Shopping cart state and operations
- **Provider Hierarchy**: Wrapped in App.tsx with proper nesting

### Custom Hooks
- **useCart**: Cart operations and state access
- **useProducts**: Product data fetching and management

## File Naming Conventions

- **Components**: PascalCase (e.g., `ProductCard.tsx`)
- **Hooks**: camelCase with `use` prefix (e.g., `useCart.ts`)
- **Types**: PascalCase interfaces in `types/index.ts`
- **Styles**: Match component name (e.g., `ProductCard.css`)
- **Tests**: Match component name with suffix (e.g., `ProductCard.test.tsx`)

## Import Patterns

- **Path Aliases**: Use TypeScript path mapping for clean imports
- **Index Files**: Export components through `index.ts` files
- **Type Imports**: Use `import type` for type-only imports
- **Default Exports**: Components use default exports, utilities use named exports
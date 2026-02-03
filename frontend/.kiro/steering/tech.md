# Technology Stack

## Core Technologies

- **Frontend Framework**: React 18.3.1 with TypeScript 5.3.3
- **Build Tool**: Vite 6.4.1 with React plugin
- **Styling**: Bootstrap 5.3.3 + Custom CSS with design tokens
- **Routing**: React Router DOM 6.21.0
- **State Management**: React Context API (Auth, Cart)

## Development Tools

- **Testing**: Vitest 3.2.4 with jsdom environment
- **E2E Testing**: Playwright 1.40.0
- **Property-Based Testing**: fast-check 4.5.3
- **Type Checking**: TypeScript with strict mode enabled

## Key Libraries

- **HTTP Client**: Axios 1.7.7
- **UI Components**: React Bootstrap 2.10.0
- **Notifications**: React Toastify 10.0.0
- **Utilities**: Lodash 4.17.15

## Common Commands

### Development
```bash
npm run dev          # Start development server (port 3000)
npm run build        # Build for production
npm run preview      # Preview production build
```

### Testing
```bash
npm test            # Run unit tests in watch mode
npm run test:run    # Run unit tests once
npm run test:ui     # Run tests with UI
npm run e2e         # Run Playwright e2e tests
npm run e2e:ui      # Run e2e tests with UI
npm run e2e:debug   # Debug e2e tests
```

## Path Aliases

The project uses TypeScript path mapping for clean imports:
- `@components/*` → `src/components/*`
- `@pages/*` → `src/pages/*`
- `@hooks/*` → `src/hooks/*`
- `@services/*` → `src/services/*`
- `@types` → `src/types/index`
- `@utils/*` → `src/utils/*`
- `@context/*` → `src/context/*`
- `@styles/*` → `src/styles/*`

## Build Configuration

- **Output Directory**: `dist/`
- **Source Maps**: Disabled in production
- **TypeScript**: Strict mode with comprehensive type checking
- **Vite Optimizations**: Excludes Playwright and Node.js modules from browser bundle
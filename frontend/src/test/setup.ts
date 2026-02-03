import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Make vi available globally as jest for compatibility
(globalThis as any).jest = vi;

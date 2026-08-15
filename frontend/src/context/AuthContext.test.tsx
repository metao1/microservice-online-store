import { act, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const adapter = vi.hoisted(() => ({
  authenticated: false,
  token: undefined as string | undefined,
  tokenParsed: undefined as Record<string, unknown> | undefined,
  init: vi.fn(),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  updateToken: vi.fn(),
  onAuthLogout: undefined as (() => void) | undefined,
}));

vi.mock('../auth/keycloak', () => ({ keycloak: adapter }));

let ProtectedRoute: typeof import('../auth/ProtectedRoute').default;
let AuthProvider: typeof import('./AuthContext').AuthProvider;
let useAuthContext: typeof import('./AuthContext').useAuthContext;

function AuthProbe() {
  const auth = useAuthContext();

  return (
    <div>
      <output aria-label="initialized">{String(auth.initialized)}</output>
      <output aria-label="authenticated">{String(auth.isAuthenticated)}</output>
      <output aria-label="user">{auth.user ? JSON.stringify(auth.user) : 'none'}</output>
      <button onClick={() => void auth.login('/orders?page=2')}>login</button>
      <button onClick={() => void auth.register('/cart')}>register</button>
      <button onClick={() => void auth.logout()}>logout</button>
      <button
        onClick={() => {
          void auth.getAccessToken().then((token) => {
            document.body.dataset.accessToken = token;
          });
        }}
      >
        token
      </button>
    </div>
  );
}

beforeEach(async () => {
  adapter.authenticated = false;
  adapter.token = undefined;
  adapter.tokenParsed = undefined;
  adapter.onAuthLogout = undefined;
  adapter.init.mockReset().mockResolvedValue(false);
  adapter.login.mockReset().mockResolvedValue(undefined);
  adapter.register.mockReset().mockResolvedValue(undefined);
  adapter.logout.mockReset().mockResolvedValue(undefined);
  adapter.updateToken.mockReset().mockResolvedValue(false);
  delete document.body.dataset.accessToken;
  window.history.replaceState({}, '', '/');

  vi.resetModules();
  ({ AuthProvider, useAuthContext } = await import('./AuthContext'));
  ({ default: ProtectedRoute } = await import('../auth/ProtectedRoute'));
});

describe('AuthProvider', () => {
  it('initializes check-sso with PKCE S256 and maps identity from token claims', async () => {
    adapter.authenticated = true;
    adapter.token = 'access-token';
    adapter.tokenParsed = {
      sub: 'customer-123',
      email: 'reader@example.com',
      name: 'Ada Reader',
    };
    adapter.init.mockResolvedValue(true);

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );

    await waitFor(() => expect(screen.getByLabelText('initialized')).toHaveTextContent('true'));

    expect(adapter.init).toHaveBeenCalledWith({ onLoad: 'check-sso', pkceMethod: 'S256' });
    expect(screen.getByLabelText('authenticated')).toHaveTextContent('true');
    expect(screen.getByLabelText('user')).toHaveTextContent(
      JSON.stringify({ id: 'customer-123', email: 'reader@example.com', name: 'Ada Reader' }),
    );
  });

  it('initializes the singleton adapter only once across provider remounts', async () => {
    const firstMount = render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );
    await waitFor(() => expect(screen.getByLabelText('initialized')).toHaveTextContent('true'));

    firstMount.unmount();
    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );
    await waitFor(() => expect(screen.getByLabelText('initialized')).toHaveTextContent('true'));

    expect(adapter.init).toHaveBeenCalledTimes(1);
  });

  it('uses absolute return URLs for login, registration, and logout', async () => {
    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );
    await waitFor(() => expect(screen.getByLabelText('initialized')).toHaveTextContent('true'));

    screen.getByRole('button', { name: 'login' }).click();
    screen.getByRole('button', { name: 'register' }).click();
    screen.getByRole('button', { name: 'logout' }).click();

    expect(adapter.login).toHaveBeenCalledWith({ redirectUri: 'http://localhost:3000/orders?page=2' });
    expect(adapter.register).toHaveBeenCalledWith({ redirectUri: 'http://localhost:3000/cart' });
    expect(adapter.logout).toHaveBeenCalledWith({ redirectUri: 'http://localhost:3000/' });
  });

  it('refreshes with 30 seconds minimum validity before returning the active token', async () => {
    adapter.authenticated = true;
    adapter.token = 'old-token';
    adapter.tokenParsed = { sub: 'customer-123' };
    adapter.init.mockResolvedValue(true);
    adapter.updateToken.mockImplementation(async () => {
      adapter.token = 'fresh-token';
      return true;
    });

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );
    await waitFor(() => expect(screen.getByLabelText('initialized')).toHaveTextContent('true'));

    screen.getByRole('button', { name: 'token' }).click();

    await waitFor(() => expect(document.body.dataset.accessToken).toBe('fresh-token'));
    expect(adapter.updateToken).toHaveBeenCalledWith(30);
  });

  it('clears token-derived identity when refresh fails', async () => {
    adapter.authenticated = true;
    adapter.token = 'expired-token';
    adapter.tokenParsed = { sub: 'customer-123', email: 'reader@example.com' };
    adapter.init.mockResolvedValue(true);
    adapter.updateToken.mockRejectedValue(new Error('refresh failed'));

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    );
    await waitFor(() => expect(screen.getByLabelText('authenticated')).toHaveTextContent('true'));

    await act(async () => {
      screen.getByRole('button', { name: 'token' }).click();
    });

    await waitFor(() => expect(screen.getByLabelText('authenticated')).toHaveTextContent('false'));
    expect(screen.getByLabelText('user')).toHaveTextContent('none');
  });
});

describe('ProtectedRoute', () => {
  it('redirects an anonymous visitor once and preserves the requested path', async () => {
    adapter.init.mockResolvedValue(false);

    render(
      <MemoryRouter initialEntries={['/orders?status=paid']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/orders"
              element={
                <ProtectedRoute>
                  <div>private orders</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(adapter.login).toHaveBeenCalledWith({
        redirectUri: 'http://localhost:3000/orders?status=paid',
      });
    });
    expect(adapter.login).toHaveBeenCalledTimes(1);
    expect(screen.queryByText('private orders')).not.toBeInTheDocument();
  });

  it('shows an accessible recovery action when login fails and retries only on request', async () => {
    adapter.init.mockResolvedValue(false);
    adapter.login
      .mockRejectedValueOnce(new Error('identity provider unavailable'))
      .mockResolvedValueOnce(undefined);

    render(
      <MemoryRouter initialEntries={['/cart']}>
        <AuthProvider>
          <ProtectedRoute>
            <div>private cart</div>
          </ProtectedRoute>
        </AuthProvider>
      </MemoryRouter>,
    );

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Unable to start sign in');
    expect(adapter.login).toHaveBeenCalledTimes(1);

    screen.getByRole('button', { name: 'Retry' }).click();

    await waitFor(() => expect(adapter.login).toHaveBeenCalledTimes(2));
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});

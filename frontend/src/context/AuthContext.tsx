import { createContext, ReactNode, useCallback, useContext, useEffect, useState } from 'react';
import { AuthenticatedUser, AuthContextValue } from '../auth/auth.types';
import { keycloak } from '../auth/keycloak';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);
let initialization: Promise<boolean> | null = null;

function initializeKeycloak() {
  initialization ??= keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256' });
  return initialization;
}

function profileFromToken(): AuthenticatedUser | null {
  const claims = keycloak.tokenParsed;
  if (!claims?.sub) {
    return null;
  }

  return {
    id: claims.sub,
    ...(typeof claims.email === 'string' ? { email: claims.email } : {}),
    ...(typeof claims.name === 'string' ? { name: claims.name } : {}),
  };
}

function absoluteReturnUrl(returnTo = '/') {
  return new URL(returnTo, window.location.origin).href;
}

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [initialized, setInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<AuthenticatedUser | null>(null);

  const clearIdentity = useCallback(() => {
    setIsAuthenticated(false);
    setUser(null);
  }, []);

  useEffect(() => {
    let active = true;

    void initializeKeycloak()
      .then((authenticated) => {
        if (!active) return;
        setIsAuthenticated(authenticated);
        setUser(authenticated ? profileFromToken() : null);
      })
      .catch(() => {
        if (active) clearIdentity();
      })
      .finally(() => {
        if (active) setInitialized(true);
      });

    const previousLogoutHandler = keycloak.onAuthLogout;
    keycloak.onAuthLogout = clearIdentity;

    return () => {
      active = false;
      keycloak.onAuthLogout = previousLogoutHandler;
    };
  }, [clearIdentity]);

  const login = useCallback((returnTo?: string) => {
    return keycloak.login({ redirectUri: absoluteReturnUrl(returnTo) });
  }, []);

  const register = useCallback((returnTo?: string) => {
    return keycloak.register({ redirectUri: absoluteReturnUrl(returnTo) });
  }, []);

  const logout = useCallback(() => {
    return keycloak.logout({ redirectUri: absoluteReturnUrl('/') });
  }, []);

  const getAccessToken = useCallback(async (minValidity = 30) => {
    if (!keycloak.authenticated) {
      return undefined;
    }

    try {
      await keycloak.updateToken(minValidity);
      setIsAuthenticated(Boolean(keycloak.authenticated));
      setUser(profileFromToken());
      return keycloak.token;
    } catch {
      clearIdentity();
      return undefined;
    }
  }, [clearIdentity]);

  const value: AuthContextValue = {
    initialized,
    isAuthenticated,
    user,
    login,
    register,
    logout,
    getAccessToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return context;
};

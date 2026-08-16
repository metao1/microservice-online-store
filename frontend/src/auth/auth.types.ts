export interface AuthenticatedUser {
  id: string;
  email?: string;
  name?: string;
}

export interface AuthContextValue {
  initialized: boolean;
  isAuthenticated: boolean;
  user: AuthenticatedUser | null;
  login(returnTo?: string): Promise<void>;
  register(returnTo?: string): Promise<void>;
  logout(): Promise<void>;
  getAccessToken(minValidity?: number): Promise<string | undefined>;
}

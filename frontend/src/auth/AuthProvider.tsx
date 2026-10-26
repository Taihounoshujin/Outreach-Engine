import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import * as authApi from '../api/auth';
import { getToken, setToken, setUnauthorizedHandler } from '../api/client';
import type { LoginRequest, RegisterRequest, UserResponse } from '../api/types';
import { AuthContext } from './AuthContext';
import type { AuthContextValue } from './AuthContext';

const USER_KEY = 'outreach.user';

/**
 * Holds "who is logged in" for the whole app.
 *
 * The token itself lives in the API client (that's what attaches it to
 * requests). This provider mirrors whether one exists, plus the user's
 * profile for display, so components re-render when you log in or out.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  // Initialise from storage so a page reload keeps you logged in.
  const [user, setUser] = useState<UserResponse | null>(() =>
    getToken() ? readStoredUser() : null,
  );

  const logout = useCallback(() => {
    setToken(null);
    storeUser(null);
    setUser(null);
  }, []);

  // Let the API client log us out when the server rejects our token.
  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authApi.login(request);
    setToken(response.token);
    storeUser(response.user);
    setUser(response.user);
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await authApi.register(request);
    setToken(response.token);
    storeUser(response.user);
    setUser(response.user);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: user !== null, login, register, logout }),
    [user, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Module-private helpers are fine here: only exports affect Fast Refresh.

function readStoredUser(): UserResponse | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as UserResponse) : null;
  } catch {
    return null;
  }
}

function storeUser(user: UserResponse | null): void {
  try {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  } catch {
    // ignore
  }
}

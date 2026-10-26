import { createContext } from 'react';
import type { LoginRequest, RegisterRequest, UserResponse } from '../api/types';

export interface AuthContextValue {
  user: UserResponse | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
}

/**
 * Only the context object and its type live here. The provider component and
 * the useAuth hook are in their own files so Vite's Fast Refresh can hot-swap
 * the component — it only works on files that export nothing but components.
 */
export const AuthContext = createContext<AuthContextValue | null>(null);
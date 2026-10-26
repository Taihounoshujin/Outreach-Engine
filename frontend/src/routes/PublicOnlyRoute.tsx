import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

/** Login and register pages: already logged in → go to the app instead. */
export function PublicOnlyRoute() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Navigate to="/" replace /> : <Outlet />;
}

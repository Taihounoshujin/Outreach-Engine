import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

/**
 * Wraps every page that needs a logged-in user.
 *
 * Not logged in → redirect to /login, remembering where you were trying to go
 * (location.state.from) so login can send you back there afterwards.
 *
 * This is a UX guard, not security. Anyone can edit the JavaScript and skip
 * it. The real protection is the backend rejecting requests without a valid
 * token — this only stops the UI showing pages that would fail anyway.
 */
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <Outlet />;
}

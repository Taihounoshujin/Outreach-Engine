import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

/**
 * The frame around every signed-in page: top bar, then whichever page the
 * route matched, rendered where <Outlet /> is. Pages only render their own
 * content, so the top bar exists in exactly one place.
 */
export function AppLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <span className="brand">Outreach Engine</span>
        <div className="topbar-user">
          <span>{user?.displayName ?? user?.email}</span>
          <button className="secondary" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}

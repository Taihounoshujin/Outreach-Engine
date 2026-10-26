import { useAuth } from '../auth/useAuth';

/** Placeholder home page — proves the protected route and token work. */
export function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <main className="dashboard">
      <header className="topbar">
        <span className="brand">Outreach Engine</span>
        <div className="topbar-user">
          <span>{user?.displayName ?? user?.email}</span>
          <button className="secondary" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>
      <section className="dashboard-body">
        <h1>Welcome{user?.displayName ? `, ${user.displayName}` : ''}</h1>
        <p>Campaigns, leads and enrollments will live here.</p>
      </section>
    </main>
  );
}

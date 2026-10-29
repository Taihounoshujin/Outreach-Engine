import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listCampaigns } from '../api/campaigns';
import { ApiError } from '../api/client';
import type { CampaignResponse } from '../api/types';
import { CreateCampaignForm } from '../components/CreateCampaignForm';
import { StatusBadge } from '../components/StatusBadge';

const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' });

export function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<CampaignResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    // If this component unmounts (or StrictMode re-runs the effect in dev)
    // before the request finishes, `ignore` stops a stale response from
    // overwriting state. Classic race-condition guard for fetch-in-effect.
    let ignore = false;

    listCampaigns()
      .then((data) => {
        if (!ignore) setCampaigns(data);
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : 'Could not load campaigns.');
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  function handleCreated(campaign: CampaignResponse) {
    // Add it locally instead of refetching the whole list: the server already
    // returned the saved campaign, so we know exactly what to show.
    setCampaigns((current) => [campaign, ...current]);
    setShowForm(false);
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Campaigns</h1>
          <p className="muted">Each campaign is a timed email sequence sent to the leads you enroll.</p>
        </div>
        {!showForm && <button onClick={() => setShowForm(true)}>New campaign</button>}
      </div>

      {showForm && <CreateCampaignForm onCreated={handleCreated} onCancel={() => setShowForm(false)} />}

      {loading && <p className="muted">Loading campaigns…</p>}

      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}

      {!loading && !error && campaigns.length === 0 && !showForm && (
        <div className="empty-state">
          <h2>No campaigns yet</h2>
          <p className="muted">Create your first campaign to start building an email sequence.</p>
          <button onClick={() => setShowForm(true)}>Create campaign</button>
        </div>
      )}

      {campaigns.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Status</th>
                <th>From</th>
                <th className="num">Daily cap</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map((campaign) => (
                <tr key={campaign.id}>
                  <td className="strong">
                    <Link to={`/campaigns/${campaign.id}`} className="row-link">
                      {campaign.name}
                    </Link>
                  </td>
                  <td>
                    <StatusBadge status={campaign.status} />
                  </td>
                  <td>
                    <div>{campaign.fromName}</div>
                    <div className="muted small">{campaign.fromEmail}</div>
                  </td>
                  <td className="num">{campaign.dailyCap}</td>
                  <td>{dateFormat.format(new Date(campaign.createdAt))}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getCampaign, listSteps, pauseCampaign, startCampaign } from '../api/campaigns';
import { ApiError } from '../api/client';
import type { CampaignResponse, SequenceStepResponse } from '../api/types';
import { AddStepForm } from '../components/AddStepForm';
import { StatusBadge } from '../components/StatusBadge';
import { StepCard } from '../components/StepCard';

export function CampaignDetailPage() {
  const { id = '' } = useParams();

  const [campaign, setCampaign] = useState<CampaignResponse | null>(null);
  const [steps, setSteps] = useState<SequenceStepResponse[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  // Which campaign ID the state above belongs to. "Loading" isn't stored —
  // it's derived: we're loading whenever the data on hand is for a different
  // ID than the one in the URL (including on first render, when it's null).
  const [loadedFor, setLoadedFor] = useState<string | null>(null);
  const loading = loadedFor !== id;

  const [showStepForm, setShowStepForm] = useState(false);
  const [statusBusy, setStatusBusy] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;

    // No setState up here: every state update happens in a callback, after the
    // requests settle. Both requests run at once instead of one after the other.
    Promise.all([getCampaign(id), listSteps(id)])
      .then(([campaignData, stepData]) => {
        if (ignore) return;
        setCampaign(campaignData);
        setSteps([...stepData].sort((a, b) => a.stepOrder - b.stepOrder));
        setLoadError(null);
        setLoadedFor(id);
      })
      .catch((err) => {
        if (ignore) return;
        if (err instanceof ApiError && err.status === 404) {
          // The backend returns 404 for campaigns you don't own as well as ones
          // that don't exist — on purpose, so it never confirms someone else's ID.
          setLoadError('Campaign not found.');
        } else {
          setLoadError(err instanceof ApiError ? err.message : 'Could not load this campaign.');
        }
        setLoadedFor(id);
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  async function changeStatus(action: 'start' | 'pause') {
    setActionError(null);
    setStatusBusy(true);
    try {
      const updated = action === 'start' ? await startCampaign(id) : await pauseCampaign(id);
      setCampaign(updated);
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Could not update the campaign.');
    } finally {
      setStatusBusy(false);
    }
  }

  function handleStepAdded(step: SequenceStepResponse) {
    setSteps((current) => [...current, step].sort((a, b) => a.stepOrder - b.stepOrder));
    setShowStepForm(false);
  }

  if (loading) {
    return <p className="muted">Loading campaign…</p>;
  }

  if (loadError || !campaign) {
    return (
      <>
        <Link to="/campaigns" className="back-link">
          ← Campaigns
        </Link>
        <div className="form-error" role="alert">
          {loadError ?? 'Campaign not found.'}
        </div>
      </>
    );
  }

  const canStart = campaign.status === 'DRAFT' || campaign.status === 'PAUSED';
  const canPause = campaign.status === 'RUNNING';
  const nextStepOrder = steps.length === 0 ? 1 : Math.max(...steps.map((s) => s.stepOrder)) + 1;

  return (
    <>
      <Link to="/campaigns" className="back-link">
        ← Campaigns
      </Link>

      <div className="page-header">
        <div>
          <div className="title-row">
            <h1>{campaign.name}</h1>
            <StatusBadge status={campaign.status} />
          </div>
          <p className="muted">
            From {campaign.fromName} &lt;{campaign.fromEmail}&gt; · up to {campaign.dailyCap} emails a day
          </p>
        </div>

        <div className="header-actions">
          {canStart && (
            <button
              onClick={() => changeStatus('start')}
              disabled={statusBusy || steps.length === 0}
              title={steps.length === 0 ? 'Add at least one step before starting' : undefined}
            >
              {statusBusy ? 'Starting…' : campaign.status === 'PAUSED' ? 'Resume' : 'Start campaign'}
            </button>
          )}
          {canPause && (
            <button className="secondary" onClick={() => changeStatus('pause')} disabled={statusBusy}>
              {statusBusy ? 'Pausing…' : 'Pause'}
            </button>
          )}
        </div>
      </div>

      {actionError && (
        <div className="form-error" role="alert">
          {actionError}
        </div>
      )}

      {campaign.status === 'PAUSED' && (
        <p className="notice">
          Paused. No emails go out until you resume. Enrolled leads keep their place in the sequence.
        </p>
      )}

      <section>
        <div className="section-header">
          <h2>Sequence</h2>
          {!showStepForm && (
            <button className="secondary" onClick={() => setShowStepForm(true)}>
              Add step
            </button>
          )}
        </div>

        {showStepForm && (
          <AddStepForm
            campaignId={campaign.id}
            nextStepOrder={nextStepOrder}
            onAdded={handleStepAdded}
            onCancel={() => setShowStepForm(false)}
          />
        )}

        {steps.length === 0 && !showStepForm ? (
          <div className="empty-state">
            <h2>No steps yet</h2>
            <p className="muted">A campaign needs at least one email before it can start.</p>
            <button onClick={() => setShowStepForm(true)}>Add the first step</button>
          </div>
        ) : (
          <ol className="step-list">
            {steps.map((step) => (
              <StepCard key={step.id} step={step} />
            ))}
          </ol>
        )}
      </section>
    </>
  );
}

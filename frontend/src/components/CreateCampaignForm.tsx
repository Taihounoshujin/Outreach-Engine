import { useState } from 'react';
import type { FormEvent } from 'react';
import { createCampaign } from '../api/campaigns';
import { ApiError } from '../api/client';
import type { CampaignResponse } from '../api/types';

interface Props {
  onCreated: (campaign: CampaignResponse) => void;
  onCancel: () => void;
}

export function CreateCampaignForm({ onCreated, onCancel }: Props) {
  const [name, setName] = useState('');
  const [fromName, setFromName] = useState('');
  const [fromEmail, setFromEmail] = useState('');
  const [dailyCap, setDailyCap] = useState('40');
  const [error, setError] = useState<string | null>(null);
  const [details, setDetails] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setDetails([]);
    setSubmitting(true);
    try {
      const campaign = await createCampaign({
        name: name.trim(),
        fromName: fromName.trim(),
        fromEmail: fromEmail.trim(),
        // Number inputs still hand us a string. Empty → omit, so the
        // backend applies its own default instead of receiving 0.
        dailyCap: dailyCap === '' ? undefined : Number(dailyCap),
      });
      onCreated(campaign);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        setDetails(err.details);
      } else {
        setError('Something went wrong. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="panel form-grid" onSubmit={handleSubmit} noValidate>
      <h2 className="span-2">New campaign</h2>

      {error && (
        <div className="form-error span-2" role="alert">
          {error}
          {details.length > 0 && (
            <ul>
              {details.map((d) => (
                <li key={d}>{d}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="field span-2">
        <label htmlFor="c-name">Campaign name</label>
        <input id="c-name" value={name} onChange={(e) => setName(e.target.value)} required />
      </div>

      <div className="field">
        <label htmlFor="c-from-name">From name</label>
        <input id="c-from-name" value={fromName} onChange={(e) => setFromName(e.target.value)} required />
      </div>

      <div className="field">
        <label htmlFor="c-from-email">From email</label>
        <input
          id="c-from-email"
          type="email"
          value={fromEmail}
          onChange={(e) => setFromEmail(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label htmlFor="c-cap">Daily send cap</label>
        <input
          id="c-cap"
          type="number"
          min={1}
          max={200}
          value={dailyCap}
          onChange={(e) => setDailyCap(e.target.value)}
        />
      </div>

      <div className="form-actions span-2">
        <button type="button" className="secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" disabled={submitting || !name || !fromName || !fromEmail}>
          {submitting ? 'Creating…' : 'Create campaign'}
        </button>
      </div>
    </form>
  );
}

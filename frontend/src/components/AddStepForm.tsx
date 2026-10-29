import { useState } from 'react';
import type { FormEvent } from 'react';
import { addStep } from '../api/campaigns';
import { ApiError } from '../api/client';
import type { SequenceStepResponse } from '../api/types';

const PLACEHOLDERS = [
  '{{contact_name}}',
  '{{company_name}}',
  '{{industry}}',
  '{{city}}',
  '{{personalized_opener}}',
];

interface Props {
  campaignId: string;
  nextStepOrder: number;
  onAdded: (step: SequenceStepResponse) => void;
  onCancel: () => void;
}

export function AddStepForm({ campaignId, nextStepOrder, onAdded, onCancel }: Props) {
  const isFirst = nextStepOrder === 1;
  const [delayDays, setDelayDays] = useState(isFirst ? '0' : '3');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [details, setDetails] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setDetails([]);
    setSubmitting(true);
    try {
      const step = await addStep(campaignId, {
        stepOrder: nextStepOrder,
        delayDays: delayDays === '' ? 0 : Number(delayDays),
        subjectTemplate: subject,
        bodyTemplate: body,
      });
      onAdded(step);
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
      <h2 className="span-2">Add step {nextStepOrder}</h2>

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

      <div className="field">
        <label htmlFor="s-delay">{isFirst ? 'Send after (days)' : 'Days after previous step'}</label>
        <input
          id="s-delay"
          type="number"
          min={0}
          value={delayDays}
          onChange={(e) => setDelayDays(e.target.value)}
        />
      </div>

      <div className="field span-2">
        <label htmlFor="s-subject">Subject</label>
        <input id="s-subject" value={subject} onChange={(e) => setSubject(e.target.value)} required />
      </div>

      <div className="field span-2">
        <label htmlFor="s-body">Body (HTML)</label>
        <textarea
          id="s-body"
          rows={8}
          value={body}
          onChange={(e) => setBody(e.target.value)}
          placeholder="<p>Hi {{contact_name}},</p>"
          required
        />
        <p className="hint">
          Placeholders:{' '}
          {PLACEHOLDERS.map((p) => (
            <code key={p}>{p}</code>
          ))}
        </p>
      </div>

      <div className="form-actions span-2">
        <button type="button" className="secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" disabled={submitting || !subject.trim() || !body.trim()}>
          {submitting ? 'Adding…' : 'Add step'}
        </button>
      </div>
    </form>
  );
}

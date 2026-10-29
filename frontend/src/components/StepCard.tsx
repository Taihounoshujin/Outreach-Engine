import type { SequenceStepResponse } from '../api/types';
import { htmlToPlainText, truncate } from '../lib/text';

function timingLabel(step: SequenceStepResponse): string {
  if (step.stepOrder === 1) {
    return step.delayDays === 0 ? 'Sends immediately' : `Sends after ${step.delayDays} day${step.delayDays === 1 ? '' : 's'}`;
  }
  return `+${step.delayDays} day${step.delayDays === 1 ? '' : 's'} after step ${step.stepOrder - 1}`;
}

export function StepCard({ step }: { step: SequenceStepResponse }) {
  return (
    <li className="step-card">
      <div className="step-number" aria-hidden="true">
        {step.stepOrder}
      </div>
      <div className="step-content">
        <div className="step-meta">
          Step {step.stepOrder} · {timingLabel(step)}
        </div>
        <div className="step-subject">{step.subjectTemplate}</div>
        {/* Plain-text preview only — see lib/text.ts for why. */}
        <p className="step-body muted">{truncate(htmlToPlainText(step.bodyTemplate), 220)}</p>
      </div>
    </li>
  );
}

import type { CampaignStatus } from '../api/types';

const LABELS: Record<CampaignStatus, string> = {
  DRAFT: 'Draft',
  RUNNING: 'Running',
  PAUSED: 'Paused',
  ARCHIVED: 'Archived',
};

/** Colour comes from CSS (badge-draft, badge-running, …); text always says the status too. */
export function StatusBadge({ status }: { status: CampaignStatus }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{LABELS[status]}</span>;
}

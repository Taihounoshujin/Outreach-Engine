import { apiFetch } from './client';
import type {
  CampaignResponse,
  CreateCampaignRequest,
  CreateSequenceStepRequest,
  SequenceStepResponse,
} from './types';

export function listCampaigns(): Promise<CampaignResponse[]> {
  return apiFetch<CampaignResponse[]>('/api/campaigns');
}

export function getCampaign(id: string): Promise<CampaignResponse> {
  return apiFetch<CampaignResponse>(`/api/campaigns/${encodeURIComponent(id)}`);
}

export function createCampaign(request: CreateCampaignRequest): Promise<CampaignResponse> {
  return apiFetch<CampaignResponse>('/api/campaigns', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function startCampaign(id: string): Promise<CampaignResponse> {
  return apiFetch<CampaignResponse>(`/api/campaigns/${encodeURIComponent(id)}/start`, { method: 'POST' });
}

export function pauseCampaign(id: string): Promise<CampaignResponse> {
  return apiFetch<CampaignResponse>(`/api/campaigns/${encodeURIComponent(id)}/pause`, { method: 'POST' });
}

export function listSteps(campaignId: string): Promise<SequenceStepResponse[]> {
  return apiFetch<SequenceStepResponse[]>(`/api/campaigns/${encodeURIComponent(campaignId)}/steps`);
}

export function addStep(
  campaignId: string,
  request: CreateSequenceStepRequest,
): Promise<SequenceStepResponse> {
  return apiFetch<SequenceStepResponse>(`/api/campaigns/${encodeURIComponent(campaignId)}/steps`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
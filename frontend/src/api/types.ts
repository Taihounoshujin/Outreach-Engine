// Shapes of the backend's JSON. Keep these in sync with the Java DTOs —
// check field names against Swagger if a response comes back undefined.

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

/** The body GlobalExceptionHandler returns for every error. */
export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[] | null;
}

// ---- campaigns ----

export type CampaignStatus = 'DRAFT' | 'RUNNING' | 'PAUSED' | 'ARCHIVED';

export interface CampaignResponse {
  id: string;
  name: string;
  status: CampaignStatus;
  fromName: string;
  fromEmail: string;
  dailyCap: number;
  timeZone: string;
  createdAt: string; // ISO-8601 instant from the backend
}

export interface CreateCampaignRequest {
  name: string;
  fromName: string;
  fromEmail: string;
  dailyCap?: number;
  timeZone?: string;
}

// ---- sequence steps ----

export interface SequenceStepResponse {
  id: string;
  stepOrder: number;
  delayDays: number;
  subjectTemplate: string;
  bodyTemplate: string;
}

export interface CreateSequenceStepRequest {
  stepOrder: number;
  delayDays: number;
  subjectTemplate: string;
  bodyTemplate: string;
}
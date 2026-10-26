import type { ApiErrorBody } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const TOKEN_KEY = 'outreach.token';

/**
 * Every request in the app goes through apiFetch. That single choke point is
 * what makes auth automatic: pages never touch the token or the
 * Authorization header — they just call the API.
 */

// ---- token storage -------------------------------------------------------
// localStorage survives reloads and browser restarts. Trade-off: any script
// running on the page can read it, so this is only as safe as our XSS
// hygiene. Production upgrade: an httpOnly cookie set by the server.

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null; // storage can throw in private mode or when blocked
  }
}

export function setToken(token: string | null): void {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    // ignore — the user simply stays logged out on reload
  }
}

// ---- 401 handling --------------------------------------------------------
// The client can't navigate by itself (it isn't a React component), so the
// auth layer registers a callback here. On any 401 we clear the token and
// call it, which logs the user out and sends them to /login.

let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  onUnauthorized = handler;
}

// ---- errors --------------------------------------------------------------

export class ApiError extends Error {
  status: number;
  details: string[];

  constructor(status: number, message: string, details: string[] = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

// ---- the request function ------------------------------------------------

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  } catch {
    // fetch only rejects on network failure — server down, DNS, or a CORS block.
    throw new ApiError(0, 'Could not reach the server. Is the backend running?');
  }

  // A 401 on a request that carried a token means the token is expired or
  // invalid. Login itself sends no token, so a wrong password falls through
  // to the normal error path instead of triggering a logout.
  if (response.status === 401 && token) {
    setToken(null);
    onUnauthorized?.();
    throw new ApiError(401, 'Your session has expired. Please log in again.');
  }

  if (!response.ok) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return undefined as T; // No Content, e.g. DELETE
  }
  return (await response.json()) as T;
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as Partial<ApiErrorBody>;
    return new ApiError(
      response.status,
      body.message ?? `Request failed (${response.status})`,
      body.details ?? [],
    );
  } catch {
    // Non-JSON error body (e.g. the plain 401 from the security entry point).
    return new ApiError(response.status, `Request failed (${response.status})`);
  }
}
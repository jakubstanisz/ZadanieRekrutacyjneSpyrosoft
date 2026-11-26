import type { ProblemDetail } from './types';

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, '') || 'http://localhost:8080';

export const DEMO_USER_ID = import.meta.env.VITE_DEMO_USER_ID || 'test-user-123';

export class ApiError extends Error {
  readonly status: number;
  readonly title: string;
  readonly detail: string;
  readonly fieldErrors?: Record<string, string>;

  constructor(status: number, title: string, detail: string, fieldErrors?: Record<string, string>) {
    super(detail || title);
    this.name = 'ApiError';
    this.status = status;
    this.title = title;
    this.detail = detail;
    this.fieldErrors = fieldErrors;
  }
}

async function parseErrorResponse(response: Response): Promise<ApiError> {
  try {
    const data = (await response.json()) as ProblemDetail;
    const title = data.title || `HTTP ${response.status}`;
    const detail = data.detail || response.statusText || 'Wystąpił błąd podczas komunikacji z serwerem.';
    return new ApiError(response.status, title, detail, data.errors);
  } catch {
    return new ApiError(
      response.status,
      `HTTP ${response.status}`,
      response.statusText || 'Nieoczekiwana odpowiedź serwera.',
    );
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set('X-User-Id', DEMO_USER_ID);
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
    });
  } catch {
    throw new ApiError(
      0,
      'Brak połączenia z serwerem',
      'Nie udało się połączyć z backendem API. Sprawdź, czy serwer działa pod adresem ' + API_BASE_URL,
    );
  }

  if (!response.ok) {
    throw await parseErrorResponse(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

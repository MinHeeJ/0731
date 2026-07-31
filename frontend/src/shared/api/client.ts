export type ApiError = {
  code: string;
  message: string;
  fields?: Record<string, string>;
};
export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  requestId: string;
};
export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/api/"))
    throw new Error("API path must be relative /api/...");
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const envelope = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !envelope.success) {
    const error = new Error(envelope.error?.message || "API 오류") as Error & {
      status?: number;
      apiError?: ApiError;
    };
    error.status = response.status;
    error.apiError = envelope.error || undefined;
    throw error;
  }
  return envelope.data as T;
}
export const api = {
  get: <T>(path: string) => apiRequest<T>(path),
  post: <T>(path: string, body?: unknown) =>
    apiRequest<T>(path, { method: "POST", body: JSON.stringify(body ?? {}) }),
  put: <T>(path: string, body?: unknown) =>
    apiRequest<T>(path, { method: "PUT", body: JSON.stringify(body ?? {}) }),
  patch: <T>(path: string, body?: unknown) =>
    apiRequest<T>(path, { method: "PATCH", body: JSON.stringify(body ?? {}) }),
  delete: <T>(path: string) => apiRequest<T>(path, { method: "DELETE" }),
};

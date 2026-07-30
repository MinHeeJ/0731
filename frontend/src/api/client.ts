export type ApiError = {
  code: string;
  message: string;
  fieldErrors?: Array<{ field: string; message: string }>;
};

export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  error: ApiError | null;
};

export async function api<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/api/")) {
    throw new Error("API path must be relative and start with /api/");
  }
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options,
  });
  const envelope = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !envelope.success) {
    const message =
      envelope.error?.message ?? "요청 처리 중 오류가 발생했습니다.";
    const error = new Error(message) as Error & {
      apiError?: ApiError;
      status?: number;
    };
    error.apiError = envelope.error ?? undefined;
    error.status = response.status;
    throw error;
  }
  return envelope.data as T;
}

function withParams(path: string, params: Record<string, string>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value.trim()) search.set(key, value.trim());
  });
  const query = search.toString();
  return query ? `${path}?${query}` : path;
}

export const endpoints = {
  login: (body: { loginId: string; password: string }) =>
    api<{ user: unknown; menuRoutes: string[] }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  me: () => api<{ user: unknown; menuRoutes: string[] }>("/api/auth/me"),
  logout: () =>
    api<{ loggedOut: boolean }>("/api/auth/logout", { method: "POST" }),
  get: <T = Record<string, unknown>>(path: string) => api<T>(path),
  list: (path: string, params: Record<string, string>) =>
    api<{ items: Array<Record<string, unknown>> }>(withParams(path, params)),
  patch: (path: string, body: unknown) =>
    api<Record<string, unknown>>(path, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  put: (path: string, body: unknown) =>
    api<Record<string, unknown>>(path, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  post: (path: string, body: unknown) =>
    api<Record<string, unknown>>(path, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  delete: (path: string, body: unknown) =>
    api<Record<string, unknown>>(path, {
      method: "DELETE",
      body: JSON.stringify(body),
    }),
};

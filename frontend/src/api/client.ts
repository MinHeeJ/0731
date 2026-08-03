export type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { message: string; fields?: Record<string, string> };
  pagination?: unknown;
};
export async function api<T>(
  path: string,
  options: RequestInit = {},
): Promise<ApiResponse<T>> {
  if (!path.startsWith("/api/"))
    throw new Error("API path must be relative /api/...");
  const res = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const body = await res.json().catch(() => ({
    success: false,
    error: { message: "응답을 읽을 수 없습니다.", fields: {} },
  }));
  if (!res.ok && body.success !== false)
    return {
      success: false,
      error: { message: `HTTP ${res.status}`, fields: {} },
    };
  return body;
}
export const qs = (params: Record<string, string>) => {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v) q.set(k, v);
  });
  return q.toString() ? `?${q}` : "";
};

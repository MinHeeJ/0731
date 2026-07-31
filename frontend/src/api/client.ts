export type ApiEnvelope<T> = {
  success: boolean;
  data: T | null;
  error: {
    code: string;
    message: string;
    fieldErrors?: Array<{ field: string; message: string }>;
  } | null;
};

export class ApiRequestError extends Error {
  status: number;
  fieldErrors: Array<{ field: string; message: string }>;

  constructor(
    message: string,
    status: number,
    fieldErrors: Array<{ field: string; message: string }> = [],
  ) {
    super(message);
    this.name = "ApiRequestError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!path.startsWith("/api/")) {
    throw new Error("API path must be relative /api/...");
  }
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  let envelope: ApiEnvelope<T>;
  try {
    envelope = (await response.json()) as ApiEnvelope<T>;
  } catch {
    envelope = {
      success: response.ok,
      data: null,
      error: null,
    } as ApiEnvelope<T>;
  }
  if (!response.ok || !envelope.success) {
    const fieldErrors = envelope.error?.fieldErrors || [];
    const message =
      fieldErrors[0]?.message ||
      envelope.error?.message ||
      `요청 처리에 실패했습니다. (${response.status})`;
    throw new ApiRequestError(message, response.status, fieldErrors);
  }
  return envelope.data as T;
}

export const api = {
  login: (loginId: string, password: string) =>
    request<{ user: { userName: string; roleCodes: string[] } }>(
      "/api/auth/login",
      {
        method: "POST",
        body: JSON.stringify({ loginId, password }),
      },
    ),
  logout: () => request("/api/auth/logout", { method: "POST" }),
  me: () => request<{ userName: string; roleCodes: string[] }>("/api/auth/me"),
  health: () =>
    request<{
      status: string;
      application?: string;
      decisionStatus: string;
      persistence?: string;
      checks: Array<Record<string, unknown>>;
    }>("/api/health"),
  menusForMe: () => request<{ items: MenuItem[] }>("/api/admin/me/menus"),
  list: (path: string, filter = "") => {
    const separator = path.includes("?") ? "&" : "?";
    return request<PageData>(
      `${path}${filter ? `${separator}filter=${encodeURIComponent(filter)}` : ""}`,
    );
  },
  save: (path: string, method: "POST" | "PUT" | "PATCH", body: unknown) =>
    request<Record<string, unknown>>(path, {
      method,
      body: JSON.stringify(body),
    }),
  remove: (path: string) =>
    request<Record<string, unknown>>(path, { method: "DELETE" }),
};

export type PageData = {
  items: Array<Record<string, unknown>>;
  page?: number;
  size?: number;
  count?: number;
};

export type MenuItem = {
  menuId: string;
  parentMenuId?: string | null;
  menuName: string;
  routePath?: string | null;
  screenId?: string | null;
  sortOrder?: number | null;
};

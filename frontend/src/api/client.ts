export type ApiFieldError = { field: string; reason: string };

export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    errors?: ApiFieldError[];
  };
};

export type ApiErrorShape = Error & {
  status?: number;
  error?: ApiResponse<unknown>["error"];
};

export type Row = Record<string, unknown>;

type QueryValue = string | number | boolean | undefined | null;

function buildQuery(params: Record<string, QueryValue> = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  if (!path.startsWith("/api/")) {
    throw new Error("API path must be relative /api/...");
  }

  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });

  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success) {
    const message = body.error?.message ?? `요청 실패: ${response.status}`;
    throw Object.assign(new Error(message), {
      status: response.status,
      error: body.error,
    });
  }
  return body.data;
}

export const api = {
  getHealth: () => request<{ status: string; service: string }>("/api/health"),
  login: (userId: string, password: string) =>
    request<{ userId: string; name: string; roles: string[] }>(
      "/api/auth/login",
      { method: "POST", body: JSON.stringify({ userId, password }) },
    ),
  logout: () => request<void>("/api/auth/logout", { method: "POST" }),
  me: () =>
    request<{ userId: string; name: string; roles: string[] }>("/api/me"),
  myMenus: () => request<Row[]>("/api/me/menus"),
  listUsers: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/users${buildQuery(params)}`),
  updateUserAccount: (userId: string, payload: Row) =>
    request<Row>(`/api/admin/users/${encodeURIComponent(userId)}/account`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),
  replaceUserRoles: (userId: string, payload: Row) =>
    request<Row>(`/api/admin/users/${encodeURIComponent(userId)}/roles`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listOrganizations: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/orgs${buildQuery(params)}`),
  getOrganizationTree: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/orgs/tree${buildQuery(params)}`),
  updateOrganizationRelation: (relationId: string, payload: Row) =>
    request<Row>(`/api/admin/org-relations/${encodeURIComponent(relationId)}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listRoles: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/roles${buildQuery(params)}`),
  updateRole: (roleCode: string, payload: Row) =>
    request<Row>(`/api/admin/roles/${encodeURIComponent(roleCode)}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listUserRoles: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/user-roles${buildQuery(params)}`),
  grantUserRole: (payload: Row) =>
    request<Row>("/api/admin/user-roles", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  updateUserRole: (assignmentId: string, payload: Row) =>
    request<Row>(`/api/admin/user-roles/${encodeURIComponent(assignmentId)}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),
  revokeUserRole: (assignmentId: string, reason?: string) =>
    request<Row>(
      `/api/admin/user-roles/${encodeURIComponent(assignmentId)}${buildQuery({ reason })}`,
      { method: "DELETE" },
    ),
  listMenuPermissions: (targetType = "ROLE", targetId = "R09") =>
    request<Row[]>(
      `/api/admin/menu-permissions${buildQuery({ targetType, targetId })}`,
    ),
  saveMenuPermissions: (payload: Row) =>
    request<Row>("/api/admin/menu-permissions", {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  getMenuTree: () => request<Row[]>("/api/admin/menus/tree"),
  updateMenuStructure: (menuId: string, payload: Row) =>
    request<Row>(`/api/admin/menus/${encodeURIComponent(menuId)}/structure`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  reorderMenus: (payload: Row) =>
    request<Row>("/api/admin/menus/reorder", {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listMenus: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/menus${buildQuery(params)}`),
  createMenu: (payload: Row) =>
    request<Row>("/api/admin/menus", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  updateMenu: (menuId: string, payload: Row) =>
    request<Row>(`/api/admin/menus/${encodeURIComponent(menuId)}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listCodeGroups: (params: Record<string, QueryValue> = {}) =>
    request<Row[]>(`/api/admin/code-groups${buildQuery(params)}`),
  createCodeGroup: (payload: Row) =>
    request<Row>("/api/admin/code-groups", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  updateCodeGroup: (groupId: string, payload: Row) =>
    request<Row>(`/api/admin/code-groups/${encodeURIComponent(groupId)}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
  listCodeDetails: (groupId: string, params: Record<string, QueryValue> = {}) =>
    request<Row[]>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes${buildQuery(params)}`,
    ),
  createCodeDetail: (groupId: string, payload: Row) =>
    request<Row>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes`,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
    ),
  updateCodeDetail: (groupId: string, codeValue: string, payload: Row) =>
    request<Row>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes/${encodeURIComponent(codeValue)}`,
      { method: "PUT", body: JSON.stringify(payload) },
    ),
};

export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  error?: {
    code: string;
    message: string;
    errors?: Array<{ field: string; reason: string }>;
  } | null;
};

async function request<T>(
  path: string,
  init?: RequestInit,
): Promise<ApiResponse<T>> {
  if (!path.startsWith("/api/"))
    throw new Error("API path must be relative /api/...");
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
    ...init,
  });
  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok && !payload.error)
    throw new Error(`API 오류 ${response.status}`);
  return payload;
}
export const api = {
  health: () => request<{ status: string }>("/api/health"),
  login: (userId: string, password: string) =>
    request<Record<string, unknown>>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ userId, password }),
    }),
  me: () => request<Record<string, unknown>>("/api/me"),
  menus: () => request<Array<Record<string, unknown>>>("/api/me/menus"),
  logout: () =>
    request<{ status: string }>("/api/auth/logout", { method: "POST" }),
  adminUsers: () => request<Array<Record<string, unknown>>>("/api/admin/users"),
  updateAdminUserAccount: (userId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/users/${encodeURIComponent(userId)}/account`,
      { method: "PATCH", body: JSON.stringify(body) },
    ),
  replaceAdminUserRoles: (userId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/users/${encodeURIComponent(userId)}/roles`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  adminOrgs: () => request<Array<Record<string, unknown>>>("/api/admin/orgs"),
  adminOrgTree: () =>
    request<Array<Record<string, unknown>>>("/api/admin/orgs/tree"),
  updateAdminOrgRelation: (relationId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/org-relations/${encodeURIComponent(relationId)}`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  adminRoles: () => request<Array<Record<string, unknown>>>("/api/admin/roles"),
  updateAdminRole: (roleCode: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/roles/${encodeURIComponent(roleCode)}`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  adminUserRoles: () =>
    request<Array<Record<string, unknown>>>("/api/admin/user-roles"),
  grantAdminUserRole: (body: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/user-roles", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  updateAdminUserRole: (assignmentId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/user-roles/${encodeURIComponent(assignmentId)}`,
      { method: "PATCH", body: JSON.stringify(body) },
    ),
  revokeAdminUserRole: (assignmentId: string) =>
    request<Record<string, unknown>>(
      `/api/admin/user-roles/${encodeURIComponent(assignmentId)}`,
      { method: "DELETE" },
    ),
  adminMenuPermissions: () =>
    request<Array<Record<string, unknown>>>("/api/admin/menu-permissions"),
  saveAdminMenuPermissions: (body: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/menu-permissions", {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  adminMenusTree: () =>
    request<Array<Record<string, unknown>>>("/api/admin/menus/tree"),
  updateAdminMenuStructure: (menuId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/menus/${encodeURIComponent(menuId)}/structure`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  reorderAdminMenus: (body: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/menus/reorder", {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  adminMenus: () => request<Array<Record<string, unknown>>>("/api/admin/menus"),
  createAdminMenu: (body: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/menus", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  updateAdminMenu: (menuId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/menus/${encodeURIComponent(menuId)}`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  adminCodeGroups: () =>
    request<Array<Record<string, unknown>>>("/api/admin/code-groups"),
  createAdminCodeGroup: (body: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/code-groups", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  updateAdminCodeGroup: (groupId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  adminCodeDetails: (groupId: string) =>
    request<Array<Record<string, unknown>>>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes`,
    ),
  createAdminCodeDetail: (groupId: string, body: Record<string, unknown>) =>
    request<Record<string, unknown>>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes`,
      { method: "POST", body: JSON.stringify(body) },
    ),
  updateAdminCodeDetail: (
    groupId: string,
    codeValue: string,
    body: Record<string, unknown>,
  ) =>
    request<Record<string, unknown>>(
      `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes/${encodeURIComponent(codeValue)}`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
  get: <T>(path: string) => request<T>(path),
  send: <T>(path: string, method: string, body: Record<string, unknown>) =>
    request<T>(path, { method, body: JSON.stringify(body) }),
};

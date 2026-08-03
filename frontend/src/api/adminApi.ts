import { api, qs } from "./client";

export const getList = (path: string, filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`${path}${qs(filters)}`);

export const mutate = (
  path: string,
  method: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(path, { method, body: JSON.stringify(body) });

export const listUsers = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/users${qs(filters)}`);
export const updateUserUsage = (
  userId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>>(`/api/users/${userId}/usage`, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
export const replaceUserRoles = (
  userId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>>(`/api/users/${userId}/roles`, {
    method: "PUT",
    body: JSON.stringify(body),
  });

export const listOrganizations = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/organizations${qs(filters)}`);
export const listOrganizationTree = () =>
  api<Record<string, unknown>[]>("/api/organizations/tree");
export const saveOrganizationRelations = (
  organizationCode: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(
    `/api/organizations/${organizationCode}/relations`,
    { method: "PUT", body: JSON.stringify(body) },
  );

export const listRoles = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/roles${qs(filters)}`);
export const createRole = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/roles", {
    method: "POST",
    body: JSON.stringify(body),
  });
export const updateRole = (roleCode: string, body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>(`/api/roles/${roleCode}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });

export const listUserRoles = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/user-roles${qs(filters)}`);
export const grantUserRole = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/user-roles", {
    method: "POST",
    body: JSON.stringify(body),
  });
export const updateUserRole = (
  assignmentId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(`/api/user-roles/${assignmentId}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
export const revokeUserRole = (
  assignmentId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(`/api/user-roles/${assignmentId}`, {
    method: "DELETE",
    body: JSON.stringify(body),
  });

export const listMenus = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/menus${qs(filters)}`);
export const listMyMenus = () =>
  api<Record<string, unknown>[]>("/api/menus/my");
export const listMenuTree = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/menus/tree${qs(filters)}`);
export const createMenu = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/menus", {
    method: "POST",
    body: JSON.stringify(body),
  });
export const updateMenu = (menuId: string, body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>(`/api/menus/${menuId}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
export const updateMenuParent = (
  menuId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(`/api/menus/${menuId}/parent`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
export const reorderMenus = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/menus/reorder", {
    method: "PUT",
    body: JSON.stringify(body),
  });

export const listMenuPermissions = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/menu-permissions${qs(filters)}`);
export const saveMenuPermissions = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/menu-permissions", {
    method: "PUT",
    body: JSON.stringify(body),
  });

export const listCodeGroups = (filters: Record<string, string> = {}) =>
  api<Record<string, unknown>[]>(`/api/code-groups${qs(filters)}`);
export const createCodeGroup = (body: Record<string, unknown>) =>
  api<Record<string, unknown>[]>("/api/code-groups", {
    method: "POST",
    body: JSON.stringify(body),
  });
export const updateCodeGroup = (
  groupId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(`/api/code-groups/${groupId}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
export const listDetailCodes = (
  groupId: string,
  filters: Record<string, string> = {},
) =>
  api<Record<string, unknown>[]>(
    `/api/code-groups/${groupId}/detail-codes${qs(filters)}`,
  );
export const listDetailCodeTree = (
  groupId: string,
  filters: Record<string, string> = {},
) =>
  api<Record<string, unknown>[]>(
    `/api/code-groups/${groupId}/detail-codes/tree${qs(filters)}`,
  );
export const createDetailCode = (
  groupId: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(`/api/code-groups/${groupId}/detail-codes`, {
    method: "POST",
    body: JSON.stringify(body),
  });
export const updateDetailCode = (
  groupId: string,
  codeValue: string,
  body: Record<string, unknown>,
) =>
  api<Record<string, unknown>[]>(
    `/api/code-groups/${groupId}/detail-codes/${codeValue}`,
    { method: "PUT", body: JSON.stringify(body) },
  );

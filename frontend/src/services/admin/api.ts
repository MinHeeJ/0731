export type AdminItem = {
  id?: string;
  name: string;
  changeReason: string;
  status?: string;
  useStatus?: "ENABLED" | "DISABLED";
  effectiveStatus?: "ACTIVE" | "EXPIRED" | "REVOKED";
  permissionAction?: "READ" | "CREATE" | "UPDATE" | "DELETE";
  displayStatus?: "VISIBLE" | "HIDDEN";
  updatedAt?: string;
};
export type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    errors?: { field: string; reason: string }[];
  };
};
export async function adminRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<ApiResponse<T>> {
  const response = await fetch(path, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      "X-Role": currentRole(),
      ...(init?.headers ?? {}),
    },
  });
  return response.json();
}
export function currentRole() {
  return window.localStorage.getItem("role") ?? "SYSTEM_ADMIN";
}
export function setCurrentRole(role: "SYSTEM_ADMIN" | "SYSTEM_VIEWER") {
  window.localStorage.setItem("role", role);
  window.dispatchEvent(new Event("role-change"));
}

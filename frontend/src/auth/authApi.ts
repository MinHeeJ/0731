import { api } from "../api/client";
export type CurrentUser = {
  userId: string;
  loginId: string;
  displayName: string;
  roles: string[];
};
export const login = (loginId: string, password: string) =>
  api<CurrentUser>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ loginId, password }),
  });
export const me = () => api<CurrentUser>("/api/auth/me");
export const logout = () =>
  api<{ status: string }>("/api/auth/logout", { method: "POST" });

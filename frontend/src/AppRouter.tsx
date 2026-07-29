import { Navigate, Route, Routes } from "react-router-dom";
import { AdminDashboardPage } from "./pages/AdminDashboardPage";
import { AdminShell } from "./pages/AdminShell";
import { LoginPage } from "./pages/LoginPage";
import { UserManagementPage } from "./pages/UserManagementPage";
import {
  CodeGroupPage,
  CommonCodePage,
  MenuInfoPage,
  MenuPermissionPage,
  MenuTreePage,
  OrganizationManagementPage,
  RoleManagementPage,
  UserRoleManagementPage,
} from "./pages/ManagementPages";

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin" element={<AdminShell />}>
        <Route index element={<AdminDashboardPage />} />
        <Route path="users" element={<UserManagementPage />} />
        <Route path="organizations" element={<OrganizationManagementPage />} />
        <Route path="roles" element={<RoleManagementPage />} />
        <Route path="user-roles" element={<UserRoleManagementPage />} />
        <Route path="menu-permissions" element={<MenuPermissionPage />} />
        <Route path="menus/tree" element={<MenuTreePage />} />
        <Route path="menus" element={<MenuInfoPage />} />
        <Route path="code-groups" element={<CodeGroupPage />} />
        <Route path="code-groups/:groupId/codes" element={<CommonCodePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  );
}

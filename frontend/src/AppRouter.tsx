import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import type { MenuItem } from "./api/client";
import ManagementPage from "./pages/ManagementPage";
import ReadinessPage from "./pages/ReadinessPage";

function UsersPage() {
  return <ManagementPage pathname="/system/users" />;
}
function OrganizationsPage() {
  return <ManagementPage pathname="/system/organizations" />;
}
function RolesPage() {
  return <ManagementPage pathname="/system/roles" />;
}
function UserRolesPage() {
  return <ManagementPage pathname="/system/user-roles" />;
}
function MenuPermissionsPage() {
  return <ManagementPage pathname="/system/menu-permissions" />;
}
function MenuTreePage() {
  return <ManagementPage pathname="/system/menu-tree" />;
}
function MenusPage() {
  return <ManagementPage pathname="/system/menus" />;
}
function CodeGroupsPage() {
  return <ManagementPage pathname="/system/code-groups" />;
}
function DetailCodesPage() {
  return <ManagementPage pathname="/system/code-groups/:groupId/codes" />;
}

export default function AppRouter({ menus }: { menus: MenuItem[] }) {
  const location = useLocation();
  return (
    <Routes location={location}>
      <Route path="/" element={<Navigate to="/system/readiness" replace />} />
      <Route
        path="/system/readiness"
        element={<ReadinessPage menus={menus} />}
      />
      <Route path="/system/users" element={<UsersPage />} />
      <Route path="/system/organizations" element={<OrganizationsPage />} />
      <Route path="/system/roles" element={<RolesPage />} />
      <Route path="/system/user-roles" element={<UserRolesPage />} />
      <Route
        path="/system/menu-permissions"
        element={<MenuPermissionsPage />}
      />
      <Route path="/system/menu-tree" element={<MenuTreePage />} />
      <Route path="/system/menus" element={<MenusPage />} />
      <Route path="/system/code-groups" element={<CodeGroupsPage />} />
      <Route
        path="/system/code-groups/:groupId/codes"
        element={<DetailCodesPage />}
      />
      <Route path="*" element={<Navigate to="/system/readiness" replace />} />
    </Routes>
  );
}

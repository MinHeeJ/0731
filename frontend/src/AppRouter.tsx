import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { ManagementPage } from "./components/ManagementPage";
import { LoginPage } from "./pages/LoginPage";
import { screens } from "./screens";

const users = screens.find((screen) => screen.route === "/admin/users")!;
const orgs = screens.find((screen) => screen.route === "/admin/orgs")!;
const roles = screens.find((screen) => screen.route === "/admin/roles")!;
const userRoles = screens.find(
  (screen) => screen.route === "/admin/user-roles",
)!;
const menuPermissions = screens.find(
  (screen) => screen.route === "/admin/menu-permissions",
)!;
const menuStructure = screens.find(
  (screen) => screen.route === "/admin/menu-structure",
)!;
const menuInfo = screens.find((screen) => screen.route === "/admin/menu-info")!;
const codeGroups = screens.find(
  (screen) => screen.route === "/admin/code-groups",
)!;
const codeDetails = screens.find(
  (screen) => screen.route === "/admin/code-details",
)!;

function UsersPage() {
  return <ManagementPage screen={users} />;
}

function OrgsPage() {
  return <ManagementPage screen={orgs} />;
}

function RolesPage() {
  return <ManagementPage screen={roles} />;
}

function UserRolesPage() {
  return <ManagementPage screen={userRoles} />;
}

function MenuPermissionsPage() {
  return <ManagementPage screen={menuPermissions} />;
}

function MenuStructurePage() {
  return <ManagementPage screen={menuStructure} />;
}

function MenuInfoPage() {
  return <ManagementPage screen={menuInfo} />;
}

function CodeGroupsPage() {
  return <ManagementPage screen={codeGroups} />;
}

function CodeDetailsPage() {
  return <ManagementPage screen={codeDetails} />;
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AppShell />}>
        <Route path="/admin/users" element={<UsersPage />} />
        <Route path="/admin/orgs" element={<OrgsPage />} />
        <Route path="/admin/roles" element={<RolesPage />} />
        <Route path="/admin/user-roles" element={<UserRolesPage />} />
        <Route
          path="/admin/menu-permissions"
          element={<MenuPermissionsPage />}
        />
        <Route path="/admin/menu-structure" element={<MenuStructurePage />} />
        <Route path="/admin/menu-info" element={<MenuInfoPage />} />
        <Route path="/admin/code-groups" element={<CodeGroupsPage />} />
        <Route path="/admin/code-details" element={<CodeDetailsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/admin/users" replace />} />
    </Routes>
  );
}

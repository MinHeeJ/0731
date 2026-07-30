import { useState } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api } from "./api/client";
import { LoginPage } from "./pages/LoginPage";
import { AdminPage, screenConfigs } from "./pages/admin/AdminPage";
import {
  CodeDetailsPage,
  CodeGroupsPage,
  MenuInfoPage,
  MenuPermissionsPage,
  MenuStructurePage,
  OrgsPage,
  RolesPage,
  UserRolesPage,
  UsersPage,
} from "./pages/admin/screens";

export function AppRouter() {
  const [menus, setMenus] = useState<Array<Record<string, unknown>>>([]);
  const navigate = useNavigate();
  const loadMenus = async () => {
    const res = await api.menus().catch(() => ({ success: false, data: [] }));
    if (res.success) setMenus(res.data || []);
  };
  const success = async () => {
    await loadMenus();
    navigate("/admin/users");
  };
  const page = (route: string) => (
    <AdminPage
      config={screenConfigs.find((item) => item.route === route)!}
      menus={menus}
    />
  );

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage onSuccess={success} />} />
      <Route
        path="/admin/users"
        element={<UsersPage>{page("/admin/users")}</UsersPage>}
      />
      <Route
        path="/admin/orgs"
        element={<OrgsPage>{page("/admin/orgs")}</OrgsPage>}
      />
      <Route
        path="/admin/roles"
        element={<RolesPage>{page("/admin/roles")}</RolesPage>}
      />
      <Route
        path="/admin/user-roles"
        element={<UserRolesPage>{page("/admin/user-roles")}</UserRolesPage>}
      />
      <Route
        path="/admin/menu-permissions"
        element={
          <MenuPermissionsPage>
            {page("/admin/menu-permissions")}
          </MenuPermissionsPage>
        }
      />
      <Route
        path="/admin/menu-structure"
        element={
          <MenuStructurePage>{page("/admin/menu-structure")}</MenuStructurePage>
        }
      />
      <Route
        path="/admin/menu-info"
        element={<MenuInfoPage>{page("/admin/menu-info")}</MenuInfoPage>}
      />
      <Route
        path="/admin/code-groups"
        element={<CodeGroupsPage>{page("/admin/code-groups")}</CodeGroupsPage>}
      />
      <Route
        path="/admin/code-details"
        element={
          <CodeDetailsPage>{page("/admin/code-details")}</CodeDetailsPage>
        }
      />
      <Route
        path="*"
        element={
          <LoginPage
            onSuccess={success}
            notFoundPath={window.location.pathname}
          />
        }
      />
    </Routes>
  );
}

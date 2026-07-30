import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { endpoints } from "./api/client";
import { Layout } from "./components/Layout";
import { LoginPage } from "./pages/LoginPage";
import { PermissionDenied } from "./pages/PermissionDenied";
import {
  CodeDetailsPage,
  CodeGroupsPage,
  MenuInfoPage,
  MenuPermissionsPage,
  MenuStructurePage,
  OrganizationsPage,
  RolesPage,
  UserRolesPage,
  UsersPage,
} from "./pages/SystemPages";

const routeElements = [
  { route: "/system/users", element: <UsersPage /> },
  { route: "/system/organizations", element: <OrganizationsPage /> },
  { route: "/system/roles", element: <RolesPage /> },
  { route: "/system/user-roles", element: <UserRolesPage /> },
  { route: "/system/menu-permissions", element: <MenuPermissionsPage /> },
  { route: "/system/menu-structure", element: <MenuStructurePage /> },
  { route: "/system/menu-info", element: <MenuInfoPage /> },
  { route: "/system/code-groups", element: <CodeGroupsPage /> },
  { route: "/system/code-details", element: <CodeDetailsPage /> },
];

export function AppRouter() {
  const [authenticated, setAuthenticated] = useState(false);
  const [permittedRoutes, setPermittedRoutes] = useState<string[]>([]);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    endpoints
      .me()
      .then((result) => {
        setAuthenticated(true);
        setPermittedRoutes(result.menuRoutes);
      })
      .catch(() => {
        setAuthenticated(false);
        setPermittedRoutes([]);
      })
      .finally(() => setChecked(true));
  }, []);

  if (!checked) {
    return (
      <div className="flex min-h-svh items-center justify-center bg-background text-sm text-muted-foreground">
        <div className="card flex items-center gap-3 p-4">
          <span className="size-4 animate-spin rounded-full border-2 border-primary border-r-transparent" />
          초기 인증 상태 확인 중...
        </div>
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={
          <LoginPage
            onLogin={(routes) => {
              setAuthenticated(true);
              setPermittedRoutes(routes);
            }}
          />
        }
      />
      <Route
        element={
          <Layout
            authenticated={authenticated}
            permittedRoutes={permittedRoutes}
            onLogout={() => {
              setAuthenticated(false);
              setPermittedRoutes([]);
            }}
          />
        }
      >
        <Route path="/" element={<Navigate to="/system/users" replace />} />
        {routeElements.map((entry) => (
          <Route
            key={entry.route}
            path={entry.route}
            element={
              <ProtectedRoute
                authenticated={authenticated}
                permittedRoutes={permittedRoutes}
                route={entry.route}
              >
                {entry.element}
              </ProtectedRoute>
            }
          />
        ))}
        <Route path="/permission-denied" element={<PermissionDenied />} />
      </Route>
      <Route path="*" element={<Navigate to="/system/users" replace />} />
    </Routes>
  );
}

function ProtectedRoute({
  authenticated,
  permittedRoutes,
  route,
  children,
}: {
  authenticated: boolean;
  permittedRoutes: string[];
  route: string;
  children: JSX.Element;
}) {
  const location = useLocation();
  if (!authenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (!permittedRoutes.includes(route)) return <PermissionDenied />;
  return children;
}

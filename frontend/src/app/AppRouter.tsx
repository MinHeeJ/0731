import { useEffect, useState } from "react";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";
import { AdminLayout } from "./AdminLayout";
import { adminRoutes } from "./routes";
import { LoginPage } from "../features/auth/LoginPage";
import { api } from "../shared/api/client";
import type { CurrentUser } from "../shared/types";
import { DashboardPage } from "./DashboardPage";

export function AppRouter() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

function AppRoutes() {
  const location = useLocation();
  const routerNavigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [sessionLoading, setSessionLoading] = useState(
    location.pathname !== "/login",
  );

  useEffect(() => {
    let active = true;
    if (location.pathname === "/login") {
      setSessionLoading(false);
      return () => {
        active = false;
      };
    }
    setSessionLoading(true);
    api
      .get<CurrentUser>("/api/auth/me")
      .then((user) => {
        if (active) setCurrentUser(user);
      })
      .catch(() => {
        if (!active) return;
        setCurrentUser(null);
        routerNavigate("/login", { replace: true });
      })
      .finally(() => {
        if (active) setSessionLoading(false);
      });
    return () => {
      active = false;
    };
  }, [location.pathname, routerNavigate]);

  async function logout() {
    await api.post("/api/auth/logout", {});
    setCurrentUser(null);
    routerNavigate("/login", { replace: true });
  }

  const protectedElement = (page: JSX.Element) => (
    <AdminLayout
      currentUser={currentUser}
      currentPath={location.pathname}
      onLogout={logout}
    >
      {sessionLoading ? <SessionSkeleton /> : page}
    </AdminLayout>
  );

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin" element={protectedElement(<DashboardPage />)} />
      {adminRoutes.map((route) => {
        const Page = route.component;
        return (
          <Route
            key={route.path}
            path={route.path}
            element={protectedElement(<Page />)}
          />
        );
      })}
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  );
}

function SessionSkeleton() {
  return (
    <section className="page" aria-busy="true">
      <div className="page-heading">
        <p className="eyebrow">session</p>
        <h1>세션을 확인하고 있습니다</h1>
        <p>보호 화면 접근 권한과 현재 사용자를 조회합니다.</p>
      </div>
      <div className="card-grid dashboard-grid">
        <div className="skeleton-card" />
        <div className="skeleton-card" />
        <div className="skeleton-card" />
      </div>
    </section>
  );
}

import type { ReactNode } from "react";
import { Link, useInRouterContext } from "react-router-dom";
import { adminRoutes } from "./routes";
import type { CurrentUser } from "../shared/types";

type Props = {
  currentUser: CurrentUser | null;
  currentPath?: string;
  children: ReactNode;
  onLogout: () => void;
};
type NavProps = {
  to: string;
  active: boolean;
  className?: string;
  children: ReactNode;
};

function NavItem({ to, active, className = "", children }: NavProps) {
  const inRouter = useInRouterContext();
  const composedClassName = `${className} ${active ? "active" : ""}`.trim();
  if (inRouter) {
    return (
      <Link className={composedClassName} to={to}>
        {children}
      </Link>
    );
  }
  return (
    <a className={composedClassName} href={to}>
      {children}
    </a>
  );
}

export function AdminLayout({
  currentUser,
  currentPath = window.location.pathname,
  children,
  onLogout,
}: Props) {
  const grouped = adminRoutes.reduce<Record<string, typeof adminRoutes>>(
    (acc, route) => {
      acc[route.group] = [...(acc[route.group] || []), route];
      return acc;
    },
    {},
  );
  return (
    <div className="layout">
      <aside className="sidebar" aria-label="시스템 관리 사이드바">
        <div className="sidebar-header">
          <div className="brand-mark">KN</div>
          <div>
            <div className="brand">교수업적평가시스템</div>
            <div className="brand-subtitle">Common Foundation Admin</div>
          </div>
        </div>
        <NavItem
          active={currentPath === "/admin"}
          className="nav-home"
          to="/admin"
        >
          <span className="nav-icon">⌘</span>
          <span>Dashboard</span>
        </NavItem>
        {Object.entries(grouped).map(([group, routes]) => (
          <section key={group} className="nav-group">
            <h3>{group}</h3>
            <div className="nav-list">
              {routes.map((route) => (
                <NavItem
                  active={currentPath === route.path}
                  key={route.path}
                  to={route.path}
                >
                  <span className="nav-icon">•</span>
                  <span>{route.label}</span>
                </NavItem>
              ))}
            </div>
          </section>
        ))}
      </aside>
      <main className="main">
        <header className="header">
          <div className="header-title">
            <strong>시스템 관리</strong>
            <span>shadcn-admin 기반 API-backed 관리자 콘솔</span>
          </div>
          <div className="session">
            <div className="avatar" aria-hidden="true">
              {(currentUser?.loginId ?? "A").slice(0, 1).toUpperCase()}
            </div>
            <div className="session-copy">
              <strong>{currentUser?.loginId ?? "anonymous"}</strong>
              <span>
                {currentUser?.roleCodes?.join(", ") ?? "세션 확인 중"}
              </span>
            </div>
            <button className="button secondary" onClick={onLogout}>
              로그아웃
            </button>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}

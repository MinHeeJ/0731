import { Link, useLocation, useNavigate } from "react-router-dom";
import type { MenuItem } from "../api/client";
import { api } from "../api/client";

type Props = {
  children: React.ReactNode;
  menus: MenuItem[];
  userName?: string | null;
  onLoggedOut: () => void;
};

type NavGroup = {
  title: string;
  routes: string[];
};

const groups: NavGroup[] = [
  { title: "공통", routes: ["/system/readiness"] },
  {
    title: "사용자·조직 관리",
    routes: ["/system/users", "/system/organizations"],
  },
  {
    title: "역할·권한 관리",
    routes: ["/system/roles", "/system/user-roles", "/system/menu-permissions"],
  },
  { title: "메뉴 관리", routes: ["/system/menu-tree", "/system/menus"] },
  {
    title: "공통코드 관리",
    routes: ["/system/code-groups", "/system/code-groups/SYSTEM_STATUS/codes"],
  },
];

function routeActive(current: string, route?: string | null) {
  if (!route) return false;
  if (current === route) return true;
  if (
    route.includes("/codes") &&
    current.startsWith("/system/code-groups/") &&
    current.endsWith("/codes")
  )
    return true;
  return false;
}

function initials(name?: string | null) {
  return (name || "R09").slice(0, 2).toUpperCase();
}

export default function Layout({
  children,
  menus,
  userName,
  onLoggedOut,
}: Props) {
  const location = useLocation();
  const navigate = useNavigate();
  const leafMenus = menus.filter((menu) => menu.routePath);

  async function logout() {
    await api.logout().catch(() => undefined);
    onLoggedOut();
    navigate("/login");
  }

  const groupedMenus = groups.map((group) => ({
    ...group,
    menus: leafMenus.filter((menu) =>
      group.routes.some(
        (route) =>
          routeActive(route, menu.routePath) || menu.routePath === route,
      ),
    ),
  }));
  const ungrouped = leafMenus.filter(
    (menu) =>
      !groups.some((group) => group.routes.includes(menu.routePath || "")),
  );

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="시스템 관리 메뉴">
        <div className="sidebar-brand">
          <div className="brand-mark">KN</div>
          <div>
            <strong>KNUE CMS</strong>
            <span>공통관리</span>
          </div>
        </div>
        <nav className="sidebar-nav">
          {groupedMenus.map(
            (group) =>
              group.menus.length > 0 && (
                <section className="nav-group" key={group.title}>
                  <p>{group.title}</p>
                  {group.menus.map((menu) => (
                    <Link
                      key={menu.menuId}
                      className={
                        routeActive(location.pathname, menu.routePath)
                          ? "active nav-link"
                          : "nav-link"
                      }
                      to={menu.routePath || "/system/readiness"}
                    >
                      <span className="nav-dot" />
                      {menu.menuName}
                    </Link>
                  ))}
                </section>
              ),
          )}
          {ungrouped.length > 0 && (
            <section className="nav-group">
              <p>기타</p>
              {ungrouped.map((menu) => (
                <Link
                  key={menu.menuId}
                  className={
                    routeActive(location.pathname, menu.routePath)
                      ? "active nav-link"
                      : "nav-link"
                  }
                  to={menu.routePath || "/system/readiness"}
                >
                  <span className="nav-dot" />
                  {menu.menuName}
                </Link>
              ))}
            </section>
          )}
        </nav>
        <div className="sidebar-footer">
          <div className="avatar">{initials(userName)}</div>
          <div>
            <strong>{userName || "R09 관리자"}</strong>
            <span>시스템 관리자</span>
          </div>
        </div>
      </aside>
      <div className="main-area">
        <header className="header">
          <div>
            <p className="eyebrow">한국교원대학교 교수업적평가시스템</p>
            <h1>시스템 관리 1차 공통기능</h1>
          </div>
          <div className="session-box">
            <div className="avatar small">{initials(userName)}</div>
            <span>{userName || "R09 관리자"}</span>
            <button className="outline" onClick={logout}>
              로그아웃
            </button>
          </div>
        </header>
        <main className="main-content" id="main-content">
          {children}
        </main>
      </div>
    </div>
  );
}

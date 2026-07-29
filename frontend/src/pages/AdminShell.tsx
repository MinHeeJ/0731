import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Menu } from "../types";
import { PermissionState, SkeletonBlock } from "../components/Ui";

type CurrentUser = { userId: string; roleCodes: string[] };

const navGroups = [
  { group: "대시보드", items: [{ label: "공통기능 현황", to: "/admin" }] },
  {
    group: "사용자·조직 관리",
    items: [
      { label: "사용자 관리", to: "/admin/users" },
      { label: "조직 관리", to: "/admin/organizations" },
    ],
  },
  {
    group: "역할·권한 관리",
    items: [
      { label: "역할 관리", to: "/admin/roles" },
      { label: "사용자 역할 관리", to: "/admin/user-roles" },
      { label: "메뉴 권한 관리", to: "/admin/menu-permissions" },
    ],
  },
  {
    group: "메뉴 관리",
    items: [
      { label: "메뉴 구조 관리", to: "/admin/menus/tree" },
      { label: "메뉴 정보 관리", to: "/admin/menus" },
    ],
  },
  {
    group: "공통코드 관리",
    items: [{ label: "코드그룹 관리", to: "/admin/code-groups" }],
  },
];

export function AdminShell() {
  const navigate = useNavigate();
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [menus, setMenus] = useState<Menu[]>([]);
  const [health, setHealth] = useState("확인 중");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      api<CurrentUser>("/api/auth/me"),
      api<{ menus: Menu[]; roleCodes: string[] }>("/api/admin/me"),
      api<{ status: string }>("/api/health"),
    ])
      .then(([me, ctx, h]) => {
        setUser(me);
        setMenus(ctx.menus);
        setHealth(h.status);
      })
      .catch((err: Error) => {
        setError(err.message);
        if (err.message.includes("인증") || err.message.includes("로그인"))
          navigate("/login");
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  function logout() {
    api("/api/auth/logout", { method: "POST", body: "{}" }).finally(() =>
      navigate("/login"),
    );
  }

  const isAdmin = user?.roleCodes.includes("R09");
  const leafMenus = menus.filter((m) => m.menuLevel === 3);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-mark small">K</div>
          <div>
            <b>시스템 관리</b>
            <span>공통기능 콘솔</span>
          </div>
        </div>
        <nav>
          {navGroups.map((group) => (
            <section className="nav-group" key={group.group}>
              <p>{group.group}</p>
              {group.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === "/admin"}
                  className={({ isActive }) =>
                    isActive ? "nav-link active" : "nav-link"
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </section>
          ))}
        </nav>
        <div className="sidebar-footer">
          <span>
            {user?.userId || "-"} · {health}
          </span>
          <button className="btn outline" onClick={logout}>
            로그아웃
          </button>
        </div>
      </aside>
      <div className="shell-main">
        <header className="top-header">
          <div>
            <b>한국교원대학교 교수업적평가시스템</b>
            <span>{leafMenus.length}개 관리 화면 연결</span>
          </div>
          <div className="header-badges">
            <span className={health === "UP" ? "health up" : "health"}>
              /api/health {health}
            </span>
            <span>{user?.roleCodes.join(", ")}</span>
          </div>
        </header>
        <main className="main-content">
          {loading ? (
            <SkeletonBlock />
          ) : !isAdmin ? (
            <PermissionState />
          ) : error ? (
            <PermissionState />
          ) : (
            <Outlet context={{ menus, user, health }} />
          )}
        </main>
      </div>
    </div>
  );
}

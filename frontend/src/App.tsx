import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { api, MenuItem } from "./api/client";
import AppRouter from "./AppRouter";
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";

const fallbackMenus: MenuItem[] = [
  {
    menuId: "M-READY",
    menuName: "1차 검증 대시보드",
    routePath: "/system/readiness",
    parentMenuId: null,
  },
  {
    menuId: "M-USERS",
    menuName: "사용자 관리",
    routePath: "/system/users",
    parentMenuId: "G-USER-ORG",
  },
  {
    menuId: "M-ORGS",
    menuName: "조직 관리",
    routePath: "/system/organizations",
    parentMenuId: "G-USER-ORG",
  },
  {
    menuId: "M-ROLES",
    menuName: "역할 관리",
    routePath: "/system/roles",
    parentMenuId: "G-ROLE-PERM",
  },
  {
    menuId: "M-USER-ROLES",
    menuName: "사용자 역할 관리",
    routePath: "/system/user-roles",
    parentMenuId: "G-ROLE-PERM",
  },
  {
    menuId: "M-MENU-PERM",
    menuName: "메뉴 권한 관리",
    routePath: "/system/menu-permissions",
    parentMenuId: "G-ROLE-PERM",
  },
  {
    menuId: "M-MENU-TREE",
    menuName: "메뉴 구조 관리",
    routePath: "/system/menu-tree",
    parentMenuId: "G-MENU",
  },
  {
    menuId: "M-MENU-INFO",
    menuName: "메뉴 정보 관리",
    routePath: "/system/menus",
    parentMenuId: "G-MENU",
  },
  {
    menuId: "M-CODE-GROUPS",
    menuName: "코드그룹 관리",
    routePath: "/system/code-groups",
    parentMenuId: "G-CODE",
  },
  {
    menuId: "M-DETAIL-CODES",
    menuName: "상세코드 관리",
    routePath: "/system/code-groups/SYSTEM_STATUS/codes",
    parentMenuId: "G-CODE",
  },
];

export default function App() {
  const [menus, setMenus] = useState<MenuItem[]>(fallbackMenus);
  const [userName, setUserName] = useState<string | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [checking, setChecking] = useState(true);
  const location = useLocation();

  async function refreshSession() {
    const me = await api.me();
    setUserName(me.userName);
    const menuData = await api.menusForMe();
    const apiMenus = menuData.items.filter((item) => item.routePath);
    const routeSet = new Set(apiMenus.map((item) => item.routePath));
    const merged = [
      ...apiMenus,
      ...fallbackMenus.filter(
        (item) => item.routePath && !routeSet.has(item.routePath),
      ),
    ];
    setMenus(merged);
    setAuthenticated(true);
    setChecking(false);
  }

  useEffect(() => {
    if (location.pathname === "/login") {
      setChecking(false);
      return;
    }
    setChecking(true);
    refreshSession().catch(() => {
      setAuthenticated(false);
      setChecking(false);
    });
  }, []);

  if (location.pathname === "/login") {
    return <LoginPage onLoggedIn={refreshSession} />;
  }

  if (checking) {
    return (
      <div className="permission-page">
        <div className="auth-state-card">
          <span className="spinner" />
          <h1>세션 확인 중</h1>
          <p>관리자 메뉴와 권한을 불러오고 있습니다.</p>
        </div>
      </div>
    );
  }

  if (!authenticated) {
    return (
      <div className="permission-page">
        <div className="auth-state-card">
          <h1>권한 확인 필요</h1>
          <p>세션이 없거나 만료되었습니다.</p>
          <a className="button-like primary" href="/login">
            로그인으로 이동
          </a>
        </div>
      </div>
    );
  }

  const effectiveMenus = menus.length ? menus : fallbackMenus;
  return (
    <Layout
      menus={effectiveMenus}
      userName={userName}
      onLoggedOut={() => setAuthenticated(false)}
    >
      <AppRouter menus={effectiveMenus} />
    </Layout>
  );
}

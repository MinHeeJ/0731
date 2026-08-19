import {
  NavLink,
  Navigate,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import { LoginPage } from "../pages/LoginPage";
import { ValidationPage } from "../pages/ValidationPage";
import { ProtectedRoute } from "./ProtectedRoute";
import { useAuth } from "../auth/AuthProvider";
import { logout } from "../auth/authApi";
import { UserManagementPage } from "../pages/UserManagementPage";
import { OrganizationManagementPage } from "../pages/OrganizationManagementPage";
import { RoleManagementPage } from "../pages/RoleManagementPage";
import { UserRoleManagementPage } from "../pages/UserRoleManagementPage";
import { MenuPermissionManagementPage } from "../pages/MenuPermissionManagementPage";
import { MenuStructureManagementPage } from "../pages/MenuStructureManagementPage";
import { MenuInformationManagementPage } from "../pages/MenuInformationManagementPage";
import { CodeGroupManagementPage } from "../pages/CodeGroupManagementPage";
import { DetailCodeManagementPage } from "../pages/DetailCodeManagementPage";
import { ScrCommonSetting } from "../pages/ScrCommonSetting";

const navGroups = [
  {
    title: "사용자·조직 관리",
    items: [
      ["사용자 관리", "/system/users"],
      ["조직 관리", "/system/organizations"],
    ],
  },
  {
    title: "역할·권한 관리",
    items: [
      ["역할 관리", "/system/roles"],
      ["사용자 역할 관리", "/system/user-roles"],
      ["메뉴 권한 관리", "/system/menu-permissions"],
    ],
  },
  {
    title: "메뉴 관리",
    items: [
      ["메뉴 구조 관리", "/system/menu-structure"],
      ["메뉴 정보 관리", "/system/menus"],
    ],
  },
  {
    title: "공통코드 관리",
    items: [
      ["코드그룹 관리", "/system/code-groups"],
      ["상세코드 관리", "/system/code-groups/EVAL_AREA/detail-codes"],
    ],
  },
  {
    title: "시스템 환경설정",
    items: [["공통 환경설정", "/system/common-settings"]],
  },
  {
    title: "검증",
    items: [["1차 산출물 검증", "/system/validation"]],
  },
];

function Layout({ children }: { children: JSX.Element }) {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();
  const isR09 = user?.roles?.includes("R09");
  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="시스템 관리 메뉴">
        <div className="sidebar-header">
          <div className="brand-mark">CMS</div>
          <div>
            <h2>교원사이트</h2>
            <p>공통기능 Admin</p>
          </div>
        </div>
        <div className="user-card">
          <div>
            <strong>{user?.displayName || user?.loginId}</strong>
            <small>{user?.loginId}</small>
          </div>
          {isR09 && <span className="badge badge-primary">R09</span>}
        </div>
        <nav className="sidebar-nav">
          {isR09 ? (
            navGroups.map((group) => (
              <section key={group.title} className="nav-group">
                <p>{group.title}</p>
                {group.items.map(([name, path]) => (
                  <NavLink
                    key={path}
                    to={path}
                    className={({ isActive }) =>
                      `nav-item${isActive ? " active" : ""}`
                    }
                  >
                    <span>{name}</span>
                  </NavLink>
                ))}
              </section>
            ))
          ) : (
            <div className="permission-card">
              R09 권한이 없어 메뉴가 숨겨졌습니다.
            </div>
          )}
        </nav>
        <button
          className="button button-outline sidebar-logout"
          onClick={async () => {
            await logout();
            setUser(null);
            navigate("/login");
          }}
        >
          로그아웃
        </button>
      </aside>
      <div className="content-inset">
        <header className="top-header">
          <div>
            <span className="eyebrow">시스템 관리</span>
            <strong>공통기능 운영 콘솔</strong>
          </div>
          <span className="badge">API-backed</span>
        </header>
        <main className="main">{children}</main>
      </div>
    </div>
  );
}

const protectedPage = (child: JSX.Element) => (
  <ProtectedRoute>
    <Layout>{child}</Layout>
  </ProtectedRoute>
);

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<Navigate to="/system/users" replace />} />
      <Route
        path="/system/users"
        element={protectedPage(<UserManagementPage />)}
      />
      <Route
        path="/system/organizations"
        element={protectedPage(<OrganizationManagementPage />)}
      />
      <Route
        path="/system/roles"
        element={protectedPage(<RoleManagementPage />)}
      />
      <Route
        path="/system/user-roles"
        element={protectedPage(<UserRoleManagementPage />)}
      />
      <Route
        path="/system/menu-permissions"
        element={protectedPage(<MenuPermissionManagementPage />)}
      />
      <Route
        path="/system/menu-structure"
        element={protectedPage(<MenuStructureManagementPage />)}
      />
      <Route
        path="/system/menus"
        element={protectedPage(<MenuInformationManagementPage />)}
      />
      <Route
        path="/system/code-groups"
        element={protectedPage(<CodeGroupManagementPage />)}
      />
      <Route
        path="/system/code-groups/:groupId/detail-codes"
        element={protectedPage(<DetailCodeManagementPage />)}
      />
      <Route
        path="/system/common-settings"
        element={protectedPage(<ScrCommonSetting />)}
      />
      <Route
        path="/system/validation"
        element={protectedPage(<ValidationPage />)}
      />
    </Routes>
  );
}

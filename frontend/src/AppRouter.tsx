import { ReactNode, useMemo, useState } from "react";
import {
  AlertCircle,
  Boxes,
  ChevronRight,
  CircleDot,
  Code2,
  FolderTree,
  KeyRound,
  LayoutDashboard,
  MenuSquare,
  Search,
  ShieldCheck,
  UsersRound,
} from "lucide-react";
import {
  BrowserRouter,
  Link,
  Navigate,
  NavLink,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { AdminScreen } from "./routes/system/AdminScreen";
import { ScreenConfig, screens } from "./routes/system/screens";
import { currentRole, setCurrentRole } from "./services/admin/api";

const groupIcons: Record<string, ReactNode> = {
  "사용자·조직": <UsersRound size={16} />,
  "역할·권한": <KeyRound size={16} />,
  메뉴: <MenuSquare size={16} />,
  공통코드: <Code2 size={16} />,
};

const screenIcons: Record<ScreenConfig["archetype"], ReactNode> = {
  SEARCH_LIST_DETAIL: <LayoutDashboard size={15} />,
  TREE_EDITOR: <FolderTree size={15} />,
  PERMISSION_MATRIX: <ShieldCheck size={15} />,
  CONTENT_EDITOR: <Boxes size={15} />,
};

function ShellLayout({ children }: { children: ReactNode }) {
  const [role, setRole] = useState(currentRole());
  const location = useLocation();
  const grouped = useMemo(
    () =>
      screens.reduce<Record<string, ScreenConfig[]>>((acc, screen) => {
        acc[screen.group] = [...(acc[screen.group] ?? []), screen];
        return acc;
      }, {}),
    [],
  );
  const current = screens.find((screen) => screen.route === location.pathname);

  function updateRole(nextRole: "SYSTEM_ADMIN" | "SYSTEM_VIEWER") {
    setCurrentRole(nextRole);
    setRole(nextRole);
  }

  return (
    <div className="shell">
      <aside className="sidebar" aria-label="시스템 관리 메뉴">
        <Link className="brand" to="/system/users" aria-label="사용자 관리 홈">
          <span className="brand-mark">
            <ShieldCheck size={22} />
          </span>
          <span className="brand-copy">
            <strong>test0731</strong>
            <small>시스템 관리 콘솔</small>
          </span>
        </Link>
        <div className="sidebar-content">
          {Object.entries(grouped).map(([group, groupScreens]) => {
            const activeGroup = groupScreens.some(
              (screen) => screen.route === location.pathname,
            );
            return (
              <nav key={group} className="nav-group" aria-label={group}>
                <h2 className={activeGroup ? "expanded" : ""}>
                  <span>{groupIcons[group] ?? <CircleDot size={16} />}</span>
                  {group}
                  <ChevronRight size={14} />
                </h2>
                <div className="nav-items">
                  {groupScreens
                    .filter(() => role === "SYSTEM_ADMIN")
                    .map((screen) => (
                      <NavLink
                        key={screen.route}
                        to={screen.route}
                        className={({ isActive }) =>
                          `nav-link ${isActive ? "active" : ""}`
                        }
                      >
                        {screenIcons[screen.archetype]}
                        <span>{screen.title}</span>
                      </NavLink>
                    ))}
                </div>
              </nav>
            );
          })}
          {role !== "SYSTEM_ADMIN" && (
            <div className="sidebar-empty" role="status">
              <AlertCircle size={18} />
              <span>권한 없는 메뉴는 숨김 처리되었습니다.</span>
            </div>
          )}
        </div>
      </aside>
      <div className="app-inset">
        <header className="header">
          <div className="header-title">
            <strong>한국교원대학교 교수업적평가시스템</strong>
            <span>
              {current
                ? `${current.id} · ${current.title}`
                : "공통기능 1차 완료 목표"}
            </span>
          </div>
          <div className="header-actions">
            <label className="global-search" aria-label="전역 검색">
              <Search size={15} />
              <input readOnly placeholder="Search..." value="" />
            </label>
            <select
              aria-label="역할 선택"
              value={role}
              onChange={(event) =>
                updateRole(
                  event.target.value as "SYSTEM_ADMIN" | "SYSTEM_VIEWER",
                )
              }
            >
              <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
              <option value="SYSTEM_VIEWER">SYSTEM_VIEWER</option>
            </select>
          </div>
        </header>
        <main>{children}</main>
      </div>
    </div>
  );
}

function PermissionAwareScreen({ screen }: { screen: ScreenConfig }) {
  return <AdminScreen screen={screen} />;
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <ShellLayout>
        <Routes>
          <Route path="/" element={<Navigate to="/system/users" replace />} />
          {screens.map((screen) => (
            <Route
              key={screen.route}
              path={screen.route}
              element={<PermissionAwareScreen screen={screen} />}
            />
          ))}
          <Route path="*" element={<Navigate to="/system/users" replace />} />
        </Routes>
      </ShellLayout>
    </BrowserRouter>
  );
}

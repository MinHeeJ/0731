import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { screens } from "../routes";
import { endpoints } from "../api/client";

type LayoutProps = {
  permittedRoutes: string[];
  authenticated: boolean;
  onLogout: () => void;
};

const groups = [
  "사용자·조직 관리",
  "역할·권한 관리",
  "메뉴 관리",
  "공통코드 관리",
] as const;

export function Layout({
  permittedRoutes,
  authenticated,
  onLogout,
}: LayoutProps) {
  const navigate = useNavigate();
  const visibleScreens = screens.filter((screen) =>
    permittedRoutes.includes(screen.route),
  );

  async function logout() {
    await endpoints.logout().catch(() => undefined);
    onLogout();
    navigate("/login");
  }

  return (
    <div className="min-h-svh bg-sidebar text-foreground lg:grid lg:grid-cols-[18rem_minmax(0,1fr)]">
      <aside className="hidden border-r border-sidebar-border bg-sidebar text-sidebar-foreground lg:flex lg:min-h-svh lg:flex-col">
        <div className="border-b border-sidebar-border px-5 py-4">
          <div className="flex items-center gap-3">
            <div className="flex size-9 items-center justify-center rounded-lg bg-sidebar-primary text-sm font-bold text-sidebar-primary-foreground">
              KN
            </div>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">
                교수업적평가시스템
              </p>
              <p className="truncate text-xs text-muted-foreground">
                Common Foundation
              </p>
            </div>
          </div>
        </div>
        <nav className="no-scrollbar flex-1 space-y-5 overflow-auto px-3 py-4 text-sm">
          {groups.map((group) => {
            const entries = visibleScreens.filter(
              (screen) => screen.menuGroup === group,
            );
            if (entries.length === 0) return null;
            return (
              <section key={group}>
                <h2 className="mb-2 px-2 text-xs font-medium text-muted-foreground">
                  {group}
                </h2>
                <div className="space-y-1">
                  {entries.map((screen) => (
                    <NavLink
                      key={screen.id}
                      to={screen.route}
                      className={({ isActive }) =>
                        `flex items-center justify-between rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                          isActive
                            ? "bg-sidebar-accent text-sidebar-accent-foreground shadow-xs"
                            : "text-sidebar-foreground/80 hover:bg-sidebar-accent/70 hover:text-sidebar-accent-foreground"
                        }`
                      }
                    >
                      <span>{screen.title}</span>
                      <span className="rounded-full bg-muted px-1.5 py-0.5 text-[10px] text-muted-foreground">
                        {screen.id.split("-")[0]}
                      </span>
                    </NavLink>
                  ))}
                </div>
              </section>
            );
          })}
        </nav>
        <div className="border-t border-sidebar-border p-3">
          <div className="rounded-lg border border-sidebar-border bg-background/70 p-3">
            <p className="text-sm font-medium">시스템관리자</p>
            <p className="mt-1 text-xs text-muted-foreground">
              R09 권한 기반 메뉴 노출
            </p>
          </div>
        </div>
      </aside>
      <main className="min-w-0 bg-background lg:m-2 lg:rounded-xl lg:shadow-sm">
        <header className="sticky top-0 z-50 h-16 border-b bg-background/95 backdrop-blur-sm transition-shadow">
          <div className="flex h-full items-center justify-between gap-3 px-4 sm:px-6">
            <div className="min-w-0">
              <p className="text-xs font-medium text-muted-foreground">
                Dashboard / System Admin
              </p>
              <h2 className="truncate text-lg font-semibold tracking-tight">
                시스템 관리
              </h2>
            </div>
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                {authenticated ? "R09 인증됨" : "Guest"}
              </span>
              {authenticated && (
                <button className="secondary-button h-9" onClick={logout}>
                  Logout
                </button>
              )}
            </div>
          </div>
        </header>
        <div className="px-4 py-6 sm:px-6 @7xl/content:mx-auto @7xl/content:w-full @7xl/content:max-w-7xl">
          <MobileNav
            visibleRoutes={visibleScreens.map((screen) => screen.route)}
          />
          <Outlet />
        </div>
      </main>
    </div>
  );
}

function MobileNav({ visibleRoutes }: { visibleRoutes: string[] }) {
  const entries = screens.filter((screen) =>
    visibleRoutes.includes(screen.route),
  );
  if (entries.length === 0) return null;
  return (
    <div className="mb-4 grid gap-2 lg:hidden">
      <p className="text-xs font-medium text-muted-foreground">
        시스템 관리 메뉴
      </p>
      <div className="no-scrollbar flex gap-2 overflow-x-auto pb-1">
        {entries.map((screen) => (
          <NavLink
            key={screen.id}
            to={screen.route}
            className={({ isActive }) =>
              `shrink-0 rounded-md border px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-primary text-primary-foreground"
                  : "bg-card hover:bg-accent"
              }`
            }
          >
            {screen.title}
          </NavLink>
        ))}
      </div>
    </div>
  );
}

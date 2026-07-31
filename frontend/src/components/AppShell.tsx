import { ChevronDown, LogOut, PanelLeft, Search } from "lucide-react";
import { Fragment, useEffect, useState } from "react";
import {
  Link,
  NavLink,
  Outlet,
  useLocation,
  useNavigate,
} from "react-router-dom";
import { api } from "../api/client";
import { groupedScreens, screens } from "../screens";

export function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [user, setUser] = useState<{
    userId: string;
    name: string;
    roles: string[];
  } | null>(null);
  const current =
    screens.find((screen) => screen.route === location.pathname) ?? screens[0];

  useEffect(() => {
    api
      .me()
      .then(setUser)
      .catch(() => setUser(null));
  }, [location.pathname]);

  const logout = () => {
    api.logout().finally(() => navigate("/login", { replace: true }));
  };

  return (
    <div className="min-h-svh bg-sidebar text-foreground md:grid md:grid-cols-[17rem_1fr]">
      <aside
        className={`${mobileOpen ? "fixed inset-y-0 left-0 z-50 block" : "hidden"} w-72 border-r bg-sidebar p-3 text-sidebar-foreground md:sticky md:top-0 md:block md:h-svh`}
      >
        <div className="mb-3 rounded-lg bg-sidebar-primary p-3 text-sidebar-primary-foreground shadow-sm">
          <Link to="/admin/users" className="flex items-center gap-2">
            <span className="grid size-8 place-items-center rounded-md bg-white/15 font-black">
              K
            </span>
            <span>
              <span className="block text-sm font-semibold">KNUE CMS</span>
              <span className="block text-xs opacity-80">공통기능 1차</span>
            </span>
          </Link>
        </div>
        <nav className="space-y-2">
          {Object.entries(groupedScreens).map(([group, items]) => {
            const groupActive = items.some(
              (item) => item.route === location.pathname,
            );
            return (
              <Fragment key={group}>
                <div className="flex items-center gap-2 px-2 pt-2 text-xs font-medium text-muted-foreground">
                  <ChevronDown
                    className={
                      groupActive
                        ? "rotate-0 transition-transform"
                        : "-rotate-90 transition-transform"
                    }
                    size={14}
                  />
                  {group}
                </div>
                <div className="space-y-1">
                  {items.map((screen) => {
                    const Icon = screen.icon;
                    return (
                      <NavLink
                        key={screen.route}
                        to={screen.route}
                        onClick={() => setMobileOpen(false)}
                        className={({ isActive }) =>
                          `nav-item ${isActive ? "nav-item-active" : ""}`
                        }
                      >
                        <Icon size={16} />
                        <span>{screen.title}</span>
                      </NavLink>
                    );
                  })}
                </div>
              </Fragment>
            );
          })}
        </nav>
      </aside>
      {mobileOpen ? (
        <button
          aria-label="닫기"
          className="fixed inset-0 z-40 bg-black/40 md:hidden"
          onClick={() => setMobileOpen(false)}
        />
      ) : null}
      <div className="min-w-0 bg-background md:m-2 md:ms-0 md:rounded-xl md:shadow-sm">
        <header className="sticky top-0 z-30 h-16 border-b bg-background/95 backdrop-blur-sm">
          <div className="flex h-full items-center gap-3 p-4 sm:gap-4">
            <button
              className="btn btn-outline size-9 p-0 md:hidden"
              onClick={() => setMobileOpen(true)}
            >
              <PanelLeft size={18} />
            </button>
            <div className="hidden h-6 w-px bg-border md:block" />
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs text-muted-foreground">
                {current.menuPath}
              </p>
              <h1 className="truncate text-lg font-semibold tracking-tight">
                {current.title}
              </h1>
            </div>
            <div className="hidden items-center gap-2 rounded-md border px-3 py-2 text-sm text-muted-foreground lg:flex">
              <Search size={15} />
              API 상대경로 /api
            </div>
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium">
                {user?.name ?? "세션 확인 중"}
              </p>
              <p className="text-xs text-muted-foreground">
                {user?.roles?.join(", ") ?? "R09 필요"}
              </p>
            </div>
            <button className="btn btn-outline" onClick={logout}>
              <LogOut size={16} />
              <span className="hidden sm:inline">로그아웃</span>
            </button>
          </div>
        </header>
        <Outlet />
      </div>
    </div>
  );
}

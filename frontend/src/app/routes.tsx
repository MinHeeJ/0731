import type { ComponentType } from "react";
import { CodeDetailsPage, CodeGroupsPage } from "../features/codes/CodePages";
import { LoginPage } from "../features/auth/LoginPage";
import {
  MenuInfoPage,
  MenuPermissionsPage,
  MenuStructurePage,
} from "../features/menus/MenuPages";
import { OrganizationsPage } from "../features/organizations/OrganizationsPage";
import { RolesPage } from "../features/roles/RolesPage";
import { UserRolesPage } from "../features/userRoles/UserRolesPage";
import { UsersPage } from "../features/users/UsersPage";
import { DashboardPage } from "./DashboardPage";
export type AppRoute = {
  path: string;
  label: string;
  group: string;
  component: ComponentType;
};
export const adminRoutes: AppRoute[] = [
  {
    path: "/admin/users",
    label: "사용자 관리",
    group: "사용자·조직 관리",
    component: UsersPage,
  },
  {
    path: "/admin/organizations",
    label: "조직 관리",
    group: "사용자·조직 관리",
    component: OrganizationsPage,
  },
  {
    path: "/admin/roles",
    label: "역할 관리",
    group: "역할·권한 관리",
    component: RolesPage,
  },
  {
    path: "/admin/user-roles",
    label: "사용자 역할 관리",
    group: "역할·권한 관리",
    component: UserRolesPage,
  },
  {
    path: "/admin/menu-permissions",
    label: "메뉴 권한 관리",
    group: "역할·권한 관리",
    component: MenuPermissionsPage,
  },
  {
    path: "/admin/menus/structure",
    label: "메뉴 구조 관리",
    group: "메뉴 관리",
    component: MenuStructurePage,
  },
  {
    path: "/admin/menus/info",
    label: "메뉴 정보 관리",
    group: "메뉴 관리",
    component: MenuInfoPage,
  },
  {
    path: "/admin/code-groups",
    label: "코드그룹 관리",
    group: "공통코드 관리",
    component: CodeGroupsPage,
  },
  {
    path: "/admin/code-details",
    label: "상세코드 관리",
    group: "공통코드 관리",
    component: CodeDetailsPage,
  },
];
export function resolveRoute(path: string): ComponentType {
  if (path === "/login") return LoginPage;
  if (path === "/admin" || path === "/") return DashboardPage;
  return (
    adminRoutes.find((route) => route.path === path)?.component ?? DashboardPage
  );
}

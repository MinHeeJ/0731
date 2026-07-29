export type ScreenConfig = {
  id: string;
  title: string;
  route: string;
  apiPath: string;
  group: string;
  archetype:
    | "SEARCH_LIST_DETAIL"
    | "TREE_EDITOR"
    | "PERMISSION_MATRIX"
    | "CONTENT_EDITOR";
  description: string;
  statusKind:
    | "useStatus"
    | "effectiveStatus"
    | "permissionAction"
    | "displayStatus"
    | "none";
};
export const screens: ScreenConfig[] = [
  {
    id: "SCR-001",
    title: "사용자 관리",
    route: "/system/users",
    apiPath: "/api/admin/users",
    group: "사용자·조직",
    archetype: "SEARCH_LIST_DETAIL",
    description:
      "사용자 조회와 내부 계정 시스템 사용 여부 및 로컬 관리 속성을 관리합니다.",
    statusKind: "useStatus",
  },
  {
    id: "SCR-002",
    title: "조직 관리",
    route: "/system/organizations",
    apiPath: "/api/admin/organizations",
    group: "사용자·조직",
    archetype: "TREE_EDITOR",
    description: "조직 표시·사용 여부와 로컬 관리 속성을 관리합니다.",
    statusKind: "useStatus",
  },
  {
    id: "SCR-003",
    title: "역할 관리",
    route: "/system/roles",
    apiPath: "/api/admin/roles",
    group: "역할·권한",
    archetype: "SEARCH_LIST_DETAIL",
    description: "역할 코드·명칭·설명·사용 여부를 관리합니다.",
    statusKind: "useStatus",
  },
  {
    id: "SCR-004",
    title: "사용자 역할 관리",
    route: "/system/user-roles",
    apiPath: "/api/admin/user-roles",
    group: "역할·권한",
    archetype: "PERMISSION_MATRIX",
    description: "사용자별 역할 부여·회수·유효기간을 관리합니다.",
    statusKind: "effectiveStatus",
  },
  {
    id: "SCR-005",
    title: "메뉴 권한 관리",
    route: "/system/menu-permissions",
    apiPath: "/api/admin/menu-permissions",
    group: "역할·권한",
    archetype: "PERMISSION_MATRIX",
    description: "역할별 메뉴 접근 권한과 동작 권한을 관리합니다.",
    statusKind: "permissionAction",
  },
  {
    id: "SCR-006",
    title: "메뉴 구조 관리",
    route: "/system/menu-structures",
    apiPath: "/api/admin/menu-structures",
    group: "메뉴",
    archetype: "TREE_EDITOR",
    description: "메뉴 계층 구조, 정렬 순서, 상위·하위 관계를 관리합니다.",
    statusKind: "none",
  },
  {
    id: "SCR-007",
    title: "메뉴 정보 관리",
    route: "/system/menus",
    apiPath: "/api/admin/menus",
    group: "메뉴",
    archetype: "CONTENT_EDITOR",
    description: "메뉴명, route, 표시 여부, 설명을 관리합니다.",
    statusKind: "displayStatus",
  },
  {
    id: "SCR-008",
    title: "코드그룹 관리",
    route: "/system/code-groups",
    apiPath: "/api/admin/code-groups",
    group: "공통코드",
    archetype: "SEARCH_LIST_DETAIL",
    description: "공통코드 그룹 코드·명칭·설명·사용 여부를 관리합니다.",
    statusKind: "useStatus",
  },
  {
    id: "SCR-009",
    title: "상세코드 관리",
    route: "/system/detail-codes",
    apiPath: "/api/admin/detail-codes",
    group: "공통코드",
    archetype: "SEARCH_LIST_DETAIL",
    description:
      "코드그룹 하위 상세코드 코드값·명칭·정렬·사용 여부를 관리합니다.",
    statusKind: "useStatus",
  },
];
export function screenForPath(pathname: string) {
  return screens.find((screen) => screen.route === pathname) ?? screens[0];
}

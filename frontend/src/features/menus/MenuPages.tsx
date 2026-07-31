import { ManagementPage } from "../../shared/ManagementPage";

const targetTypeOptions = ["ROLE", "ORG", "USER"].map((value) => ({
  value,
  label: value,
}));
const menuLevelOptions = ["TOP", "MIDDLE", "LEAF"].map((value) => ({
  value,
  label: value,
}));

const menuFields = [
  { key: "menuId", label: "메뉴ID", readonly: true },
  { key: "parentMenuId", label: "상위메뉴ID" },
  { key: "menuName", label: "메뉴명" },
  {
    key: "menuLevel",
    label: "단계",
    type: "select" as const,
    options: menuLevelOptions,
  },
  { key: "displayOrder", label: "순서", type: "number" as const },
  { key: "screenId", label: "화면ID" },
  { key: "urlPath", label: "URL" },
  { key: "iconName", label: "아이콘" },
  { key: "businessCategory", label: "업무구분" },
  { key: "description", label: "설명", type: "textarea" as const },
  { key: "isActive", label: "사용여부", type: "boolean" as const },
];

const menuBody = (_: Record<string, unknown>, f: Record<string, string>) => ({
  parentMenuId: f.parentMenuId || null,
  menuName: f.menuName,
  menuLevel: f.menuLevel || "LEAF",
  displayOrder: Number(f.displayOrder || 1),
  screenId: f.screenId,
  urlPath: f.urlPath,
  iconName: f.iconName,
  businessCategory: f.businessCategory,
  description: f.description,
  isActive: f.isActive !== "false",
});

export function MenuStructurePage() {
  return (
    <ManagementPage
      title="메뉴 구조 관리"
      description="대·중·소 메뉴 tree에서 부모메뉴와 동일 계층 표시순서를 변경합니다."
      queryPath="/api/menus?includeTree=true"
      fields={menuFields}
      actions={[
        {
          label: "메뉴 생성",
          method: "post",
          path: () => "/api/menus",
          body: menuBody,
        },
        {
          label: "부모/순서 저장",
          method: "put",
          path: (s) => `/api/menus/${s.menuId}`,
          body: menuBody,
        },
      ]}
    />
  );
}

export function MenuInfoPage() {
  return (
    <ManagementPage
      title="메뉴 정보 관리"
      description="메뉴명·화면ID·URL·아이콘·업무구분·설명을 편집해 실행 화면을 연결합니다."
      queryPath="/api/menus"
      filters={[
        { key: "menuName", label: "메뉴명" },
        { key: "screenId", label: "화면ID" },
      ]}
      fields={menuFields}
      actions={[
        {
          label: "실행정보 등록",
          method: "post",
          path: () => "/api/menus",
          body: menuBody,
        },
        {
          label: "실행정보 저장",
          method: "put",
          path: (s) => `/api/menus/${s.menuId}`,
          body: menuBody,
        },
      ]}
    />
  );
}

export function MenuPermissionsPage() {
  return (
    <ManagementPage
      title="메뉴 권한 관리"
      description="역할·조직·사용자 대상별 접근 허용 여부를 저장합니다."
      queryPath="/api/menu-permissions"
      filters={[
        {
          key: "targetType",
          label: "대상유형",
          type: "select",
          options: targetTypeOptions,
        },
        { key: "targetId", label: "대상ID" },
      ]}
      fields={[
        { key: "permissionId", label: "권한ID", readonly: true },
        {
          key: "targetType",
          label: "대상유형",
          type: "select",
          options: targetTypeOptions,
        },
        { key: "targetId", label: "대상ID" },
        { key: "menuId", label: "메뉴ID" },
        { key: "allowed", label: "허용", type: "boolean" },
      ]}
      actions={[
        {
          label: "권한 셀 저장",
          method: "put",
          path: () => "/api/menu-permissions",
          body: (_, f) => ({
            targetType: f.targetType || "ROLE",
            targetId: f.targetId || "R09",
            permissions: [{ menuId: f.menuId, allowed: f.allowed === "true" }],
          }),
        },
      ]}
    />
  );
}

import { ManagementPage } from "../../shared/ManagementPage";

const roleOptions = Array.from({ length: 9 }, (_, index) => {
  const value = `R${String(index + 1).padStart(2, "0")}`;
  return { value, label: value };
});

export function RolesPage() {
  const body = (_: Record<string, unknown>, f: Record<string, string>) => ({
    roleCode: f.roleCode,
    roleName: f.roleName,
    purpose: f.purpose,
    grantCriteria: f.grantCriteria,
    defaultDataScope: f.defaultDataScope,
  });
  return (
    <ManagementPage
      title="역할 관리"
      description="R01~R09 역할 코드와 목적, 부여 기준, 데이터 범위 기본값을 관리합니다."
      queryPath="/api/roles"
      filters={[
        {
          key: "roleCode",
          label: "역할코드",
          type: "select",
          options: roleOptions,
        },
        { key: "roleName", label: "역할명" },
      ]}
      fields={[
        {
          key: "roleCode",
          label: "역할코드",
          type: "select",
          options: roleOptions,
        },
        { key: "roleName", label: "역할명" },
        { key: "purpose", label: "목적", type: "textarea" },
        { key: "grantCriteria", label: "부여 기준", type: "textarea" },
        { key: "defaultDataScope", label: "데이터 범위" },
        { key: "isActive", label: "사용여부", type: "boolean" },
      ]}
      actions={[
        { label: "등록", method: "post", path: () => "/api/roles", body },
        {
          label: "저장",
          method: "put",
          path: (s) => `/api/roles/${s.roleCode}`,
          body,
        },
      ]}
    />
  );
}

import { ManagementPage } from "../../shared/ManagementPage";

const roleOptions = Array.from({ length: 9 }, (_, index) => {
  const value = `R${String(index + 1).padStart(2, "0")}`;
  return { value, label: value };
});
const statusOptions = ["ACTIVE", "REVOKED", "EXPIRED"].map((value) => ({
  value,
  label: value,
}));
const assignmentTypeOptions = ["POSITION", "MANUAL"].map((value) => ({
  value,
  label: value,
}));

export function UserRolesPage() {
  return (
    <ManagementPage
      title="사용자 역할 관리"
      description="사용자 역할 유효기간, 승인자, 보직 기반/수동 구분을 저장하고 회수합니다."
      queryPath="/api/user-roles"
      filters={[
        { key: "userId", label: "사용자ID" },
        {
          key: "roleCode",
          label: "역할코드",
          type: "select",
          options: roleOptions,
        },
        {
          key: "status",
          label: "상태",
          type: "select",
          options: statusOptions,
        },
      ]}
      fields={[
        { key: "assignmentId", label: "배정ID", readonly: true },
        { key: "userId", label: "사용자ID" },
        {
          key: "roleCode",
          label: "역할코드",
          type: "select",
          options: roleOptions,
        },
        {
          key: "assignmentType",
          label: "구분",
          type: "select",
          options: assignmentTypeOptions,
        },
        { key: "validFrom", label: "시작일", type: "date" },
        { key: "validTo", label: "종료일", type: "date" },
        { key: "approvedByUserId", label: "승인자", readonly: true },
        { key: "status", label: "상태", readonly: true },
      ]}
      actions={[
        {
          label: "역할 부여",
          method: "post",
          path: () => "/api/user-roles",
          body: (_, f) => ({
            userId: f.userId,
            roleCode: f.roleCode,
            assignmentType: f.assignmentType || "MANUAL",
            validFrom: f.validFrom,
            validTo: f.validTo || null,
          }),
        },
        {
          label: "회수",
          method: "delete",
          path: (s) => `/api/user-roles/${s.assignmentId}`,
          confirm: "선택한 역할을 회수하시겠습니까?",
          variant: "destructive",
        },
      ]}
    />
  );
}

import { ManagementPage } from "../../shared/ManagementPage";
export function UsersPage() {
  return (
    <ManagementPage
      title="사용자 관리"
      description="KORUS 원천정보는 readonly이며 시스템 사용여부와 업무 역할만 저장합니다."
      queryPath="/api/users"
      filters={[
        { key: "staffNo", label: "교번" },
        { key: "staffName", label: "성명" },
        { key: "organizationCode", label: "소속" },
        { key: "jobGrade", label: "직급" },
        { key: "employmentStatus", label: "재직상태" },
        { key: "roleCode", label: "역할" },
        { key: "systemUseEnabled", label: "사용여부", type: "boolean" },
      ]}
      fields={[
        { key: "userId", label: "사용자ID", readonly: true },
        { key: "staffNo", label: "교번", readonly: true },
        { key: "staffName", label: "성명", readonly: true },
        { key: "organizationCode", label: "소속", readonly: true },
        { key: "jobGrade", label: "직급", readonly: true },
        { key: "employmentStatus", label: "재직상태", readonly: true },
        { key: "roleCodes", label: "역할" },
        { key: "systemUseEnabled", label: "사용여부" },
        { key: "positionName", label: "보직", readonly: true },
        { key: "retirementDate", label: "퇴직일자", readonly: true },
        { key: "lastSyncedAt", label: "최종동기화", readonly: true },
      ]}
      actions={[
        {
          label: "사용여부 저장",
          method: "patch",
          path: (s) => `/api/users/${s.userId}/usage`,
          body: (_, f) => ({ systemUseEnabled: f.systemUseEnabled === "true" }),
        },
        {
          label: "업무 역할 저장",
          method: "put",
          path: (s) => `/api/users/${s.userId}/roles`,
          body: (_, f) => ({
            roleCodes: (f.roleCodes || "R09")
              .replace(/[\[\]\s]/g, "")
              .split(",")
              .filter(Boolean),
          }),
        },
      ]}
    />
  );
}

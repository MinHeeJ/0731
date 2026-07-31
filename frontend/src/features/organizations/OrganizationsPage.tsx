import { ManagementPage } from "../../shared/ManagementPage";
export function OrganizationsPage() {
  return (
    <ManagementPage
      title="조직 관리"
      description="조직코드 검색, 계층 조회, 상위조직과 적용기간을 저장합니다."
      queryPath="/api/organizations?includeHierarchy=true"
      filters={[
        { key: "organizationCode", label: "조직코드" },
        { key: "organizationName", label: "조직명" },
      ]}
      fields={[
        { key: "relationId", label: "관계ID", readonly: true },
        { key: "organizationId", label: "조직ID", readonly: true },
        { key: "organizationCode", label: "조직코드", readonly: true },
        { key: "organizationName", label: "조직명", readonly: true },
        { key: "organizationType", label: "조직유형", readonly: true },
        { key: "parentOrganizationId", label: "상위조직ID" },
        { key: "effectiveStartDate", label: "적용시작일" },
        { key: "effectiveEndDate", label: "적용종료일" },
      ]}
      actions={[
        {
          label: "관계/기간 저장",
          method: "put",
          path: (s) => `/api/organization-relations/${s.relationId}`,
          body: (s, f) => ({
            organizationId: s.organizationId,
            parentOrganizationId: f.parentOrganizationId || null,
            effectiveStartDate: f.effectiveStartDate,
            effectiveEndDate: f.effectiveEndDate || null,
          }),
        },
      ]}
    />
  );
}

import { ManagementPage } from "../../shared/ManagementPage";

export function CodeGroupsPage() {
  const body = (_: Record<string, unknown>, f: Record<string, string>) => ({
    groupId: f.groupId,
    groupName: f.groupName,
    description: f.description,
    managingDepartment: f.managingDepartment,
    isActive: f.isActive !== "false",
  });
  return (
    <ManagementPage
      title="코드그룹 관리"
      description="그룹ID·명칭·설명·관리부서를 검색/저장하고 상세코드로 이동합니다."
      queryPath="/api/code-groups"
      filters={[
        { key: "groupId", label: "그룹ID" },
        { key: "groupName", label: "명칭" },
        { key: "managingDepartment", label: "관리부서" },
      ]}
      fields={[
        { key: "groupId", label: "그룹ID" },
        { key: "groupName", label: "명칭" },
        { key: "description", label: "설명", type: "textarea" },
        { key: "managingDepartment", label: "관리부서" },
        { key: "isActive", label: "사용여부", type: "boolean" },
      ]}
      detailLink={{
        label: "상세코드 이동",
        path: (row) =>
          `/admin/code-details?groupId=${encodeURIComponent(String(row.groupId ?? ""))}`,
      }}
      actions={[
        { label: "등록", method: "post", path: () => "/api/code-groups", body },
        {
          label: "저장",
          method: "put",
          path: (s) => `/api/code-groups/${s.groupId}`,
          body,
        },
      ]}
    />
  );
}

export function CodeDetailsPage() {
  const initialGroupId =
    new URLSearchParams(window.location.search).get("groupId") || "";
  const body = (_: Record<string, unknown>, f: Record<string, string>) => ({
    groupId: f.groupId,
    codeValue: f.codeValue,
    codeName: f.codeName,
    parentCodeValue: f.parentCodeValue || null,
    sortOrder: Number(f.sortOrder || 1),
    extraAttributes: {},
    validFrom: f.validFrom || null,
    validTo: f.validTo || null,
    isActive: f.isActive !== "false",
  });
  return (
    <ManagementPage
      title="상세코드 관리"
      description="코드그룹별 상세코드 계층과 코드값·코드명·상위코드·정렬순서를 저장합니다. extraAttributes는 OQ-003에 따라 안전한 빈 객체로 전송합니다."
      queryPath={
        initialGroupId
          ? `/api/code-details?groupId=${encodeURIComponent(initialGroupId)}`
          : "/api/code-details"
      }
      filters={[
        { key: "groupId", label: "그룹ID" },
        { key: "parentCodeValue", label: "상위코드" },
      ]}
      fields={[
        { key: "groupId", label: "그룹ID" },
        { key: "codeValue", label: "코드값" },
        { key: "codeName", label: "코드명" },
        { key: "parentCodeValue", label: "상위코드" },
        { key: "sortOrder", label: "정렬순서", type: "number" },
        { key: "validFrom", label: "시작일", type: "date" },
        { key: "validTo", label: "종료일", type: "date" },
        { key: "isActive", label: "사용여부", type: "boolean" },
      ]}
      actions={[
        {
          label: "등록",
          method: "post",
          path: () => "/api/code-details",
          body,
        },
        {
          label: "저장",
          method: "put",
          path: (s) => `/api/code-details/${s.groupId}/${s.codeValue}`,
          body,
        },
      ]}
    />
  );
}

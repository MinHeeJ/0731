import { useNavigate, useParams } from "react-router-dom";
import { ResourcePage } from "../components/ResourcePage";
import { Badge } from "../components/Ui";
import type {
  Assignment,
  CodeGroup,
  CommonCode,
  Menu,
  Organization,
  Permission,
  Role,
} from "../types";

const yn = { type: "select" as const, options: ["Y", "N"] };
const roleCodes = [
  "R01",
  "R02",
  "R03",
  "R04",
  "R05",
  "R06",
  "R07",
  "R08",
  "R09",
];

export function OrganizationManagementPage() {
  return (
    <ResourcePage<Organization>
      title="조직 관리"
      desc="조직 계층을 조회하고 선택 노드의 상위조직·적용기간 관계를 저장합니다."
      path="/api/admin/organizations"
      filters={[
        { key: "organizationCode", label: "조직코드" },
        { key: "organizationName", label: "조직명" },
        { key: "organizationType", label: "조직유형" },
      ]}
      columns={[
        { key: "organizationCode", label: "조직코드" },
        { key: "organizationName", label: "조직명" },
        { key: "organizationType", label: "유형" },
        { key: "parentOrganizationCode", label: "상위" },
        { key: "effectiveStartDate", label: "시작" },
        { key: "effectiveEndDate", label: "종료" },
        {
          key: "useYn",
          label: "사용",
          render: (r) => (
            <Badge tone={r.useYn === "Y" ? "green" : "gray"}>{r.useYn}</Badge>
          ),
        },
      ]}
      fields={[
        { key: "parentOrganizationCode", label: "상위조직" },
        { key: "effectiveStartDate", label: "적용 시작일", type: "date" },
        { key: "effectiveEndDate", label: "적용 종료일", type: "date" },
      ]}
      emptyDetail="조직을 선택하면 관계 이력 추가 폼이 열립니다."
      saveLabel="관계 저장"
      buildSave={(row, form) => ({
        path: `/api/admin/organizations/${row.organizationCode}/relation`,
        method: "PUT",
        body: form,
      })}
    />
  );
}

export function RoleManagementPage() {
  return (
    <ResourcePage<Role>
      title="역할 관리"
      desc="R01~R09 역할의 목적, 부여기준, 데이터 범위를 관리합니다. roleCode는 수정하지 않습니다."
      path="/api/admin/roles"
      filters={[
        { key: "roleCode", label: "역할코드" },
        { key: "roleName", label: "역할명" },
        { key: "useYn", label: "사용", type: "select", options: ["Y", "N"] },
      ]}
      columns={[
        { key: "roleCode", label: "역할코드" },
        { key: "roleName", label: "역할명" },
        { key: "purpose", label: "목적" },
        { key: "grantCriteria", label: "부여기준" },
        { key: "defaultDataScope", label: "데이터범위" },
        {
          key: "useYn",
          label: "사용",
          render: (r) => (
            <Badge tone={r.useYn === "Y" ? "green" : "gray"}>{r.useYn}</Badge>
          ),
        },
      ]}
      fields={[
        {
          key: "roleCode",
          label: "역할코드",
          type: "select",
          options: roleCodes,
        },
        { key: "roleName", label: "역할명" },
        { key: "purpose", label: "목적", type: "textarea" },
        { key: "grantCriteria", label: "부여기준", type: "textarea" },
        {
          key: "defaultDataScope",
          label: "데이터범위",
          type: "select",
          options: ["ALL", "ORGANIZATION", "SELF"],
        },
        { key: "useYn", label: "사용여부", ...yn },
      ]}
      emptyDetail="역할을 선택하거나 신규 등록을 누르세요."
      buildCreate={(form) => ({
        path: `/api/admin/roles?roleCode=${encodeURIComponent(String(form.roleCode || ""))}`,
        method: "POST",
        body: form,
      })}
      buildSave={(row, form) => ({
        path: `/api/admin/roles/${row.roleCode}`,
        method: "PUT",
        body: { ...form, roleCode: row.roleCode },
      })}
    />
  );
}

export function UserRoleManagementPage() {
  const fields = [
    { key: "userId", label: "사용자" },
    {
      key: "roleCode",
      label: "역할",
      type: "select" as const,
      options: roleCodes,
    },
    {
      key: "grantType",
      label: "부여유형",
      type: "select" as const,
      options: ["POSITION_BASED", "MANUAL"],
    },
    { key: "approverUserId", label: "승인자" },
    { key: "validFrom", label: "유효시작", type: "date" as const },
    { key: "validTo", label: "유효종료", type: "date" as const },
  ];
  return (
    <ResourcePage<Assignment>
      title="사용자 역할 관리"
      desc="사용자별 역할의 유효기간을 부여·변경·회수합니다."
      path="/api/admin/user-roles"
      filters={[
        { key: "userId", label: "사용자" },
        { key: "roleCode", label: "역할", type: "select", options: roleCodes },
        {
          key: "grantType",
          label: "부여유형",
          type: "select",
          options: ["POSITION_BASED", "MANUAL"],
        },
        {
          key: "status",
          label: "상태",
          type: "select",
          options: ["ACTIVE", "REVOKED"],
        },
      ]}
      columns={[
        { key: "userId", label: "사용자" },
        { key: "roleCode", label: "역할" },
        { key: "grantType", label: "부여유형" },
        { key: "approverUserId", label: "승인자" },
        { key: "validFrom", label: "시작" },
        { key: "validTo", label: "종료" },
        {
          key: "status",
          label: "상태",
          render: (r) => (
            <Badge tone={r.status === "ACTIVE" ? "green" : "gray"}>
              {r.status}
            </Badge>
          ),
        },
      ]}
      fields={fields}
      emptyDetail="역할 행을 선택하거나 신규 등록을 누르세요."
      saveLabel="부여/변경 저장"
      deleteLabel="회수"
      buildCreate={(form) => ({
        path: "/api/admin/user-roles",
        method: "POST",
        body: form,
      })}
      buildSave={(row, form) => ({
        path: `/api/admin/user-roles/${row.assignmentId}`,
        method: "PUT",
        body: form,
      })}
      buildDelete={(row, form) => ({
        path: `/api/admin/user-roles/${row.assignmentId}`,
        method: "DELETE",
        body: { changeReason: form.changeReason },
      })}
    />
  );
}

export function MenuPermissionPage() {
  return (
    <ResourcePage<Permission>
      title="메뉴 권한 관리"
      desc="대상별 메뉴 접근권한을 저장하고 메뉴 미노출/서버 차단 계약을 맞춥니다."
      path="/api/admin/menu-permissions"
      filters={[
        {
          key: "targetType",
          label: "대상유형",
          type: "select",
          options: ["ROLE", "ORGANIZATION", "USER"],
        },
        { key: "targetId", label: "대상ID" },
        { key: "menuId", label: "메뉴ID" },
      ]}
      columns={[
        { key: "targetType", label: "대상유형" },
        { key: "targetId", label: "대상" },
        { key: "menuId", label: "메뉴" },
        {
          key: "accessYn",
          label: "접근",
          render: (r) => (
            <Badge tone={r.accessYn === "Y" ? "green" : "red"}>
              {r.accessYn}
            </Badge>
          ),
        },
      ]}
      fields={[
        {
          key: "targetType",
          label: "대상유형",
          type: "select",
          options: ["ROLE", "ORGANIZATION", "USER"],
        },
        { key: "targetId", label: "대상ID" },
        { key: "menuId", label: "메뉴ID" },
        { key: "accessYn", label: "접근허용", ...yn },
      ]}
      emptyDetail="권한 행을 선택하거나 신규 등록을 누르세요."
      buildCreate={(form) => ({
        path: "/api/admin/menu-permissions",
        method: "PUT",
        body: {
          targetType: form.targetType,
          targetId: form.targetId,
          permissions: [{ menuId: form.menuId, accessYn: form.accessYn }],
          changeReason: form.changeReason,
        },
      })}
      buildSave={(row, form) => ({
        path: "/api/admin/menu-permissions",
        method: "PUT",
        body: {
          targetType: row.targetType,
          targetId: row.targetId,
          permissions: [{ menuId: form.menuId, accessYn: form.accessYn }],
          changeReason: form.changeReason,
        },
      })}
    />
  );
}

export function MenuTreePage() {
  return (
    <ResourcePage<Menu>
      title="메뉴 구조 관리"
      desc="동일 parent 하위 메뉴 순서를 저장합니다."
      path="/api/admin/menus/tree"
      columns={[
        { key: "menuId", label: "메뉴ID" },
        { key: "parentMenuId", label: "부모" },
        { key: "menuLevel", label: "레벨" },
        { key: "menuName", label: "메뉴명" },
        { key: "displayOrder", label: "순서" },
        { key: "useYn", label: "사용" },
      ]}
      fields={[
        { key: "parentMenuId", label: "부모메뉴" },
        { key: "orderedMenuIds", label: "정렬 메뉴ID(쉼표 구분)" },
      ]}
      afterSelect={(row) => ({ orderedMenuIds: row.menuId })}
      emptyDetail="메뉴를 선택하면 같은 parent 내 정렬 요청을 작성할 수 있습니다."
      saveLabel="순서 저장"
      buildSave={(_row, form) => ({
        path: "/api/admin/menus/tree/reorder",
        method: "PATCH",
        body: {
          ...form,
          orderedMenuIds: String(form.orderedMenuIds || "")
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean),
        },
      })}
    />
  );
}

export function MenuInfoPage() {
  return (
    <ResourcePage<Menu>
      title="메뉴 정보 관리"
      desc="메뉴 실행정보를 API schema 필드 기준으로 등록·수정합니다."
      path="/api/admin/menus"
      filters={[
        { key: "menuName", label: "메뉴명" },
        { key: "screenId", label: "화면ID" },
        { key: "url", label: "URL" },
        { key: "businessCategory", label: "업무구분" },
        { key: "useYn", label: "사용", type: "select", options: ["Y", "N"] },
      ]}
      columns={[
        { key: "menuId", label: "메뉴ID" },
        { key: "menuName", label: "명칭" },
        { key: "screenId", label: "화면ID" },
        { key: "url", label: "URL" },
        { key: "icon", label: "아이콘" },
        { key: "businessCategory", label: "업무" },
        { key: "useYn", label: "사용" },
      ]}
      fields={[
        { key: "menuId", label: "메뉴ID" },
        { key: "parentMenuId", label: "부모메뉴" },
        { key: "menuName", label: "메뉴명" },
        { key: "screenId", label: "화면ID" },
        { key: "url", label: "URL" },
        { key: "icon", label: "아이콘" },
        { key: "businessCategory", label: "업무구분" },
        { key: "description", label: "설명", type: "textarea" },
        { key: "displayOrder", label: "표시순서", type: "number" },
        { key: "useYn", label: "사용여부", ...yn },
      ]}
      emptyDetail="메뉴 행을 선택하거나 신규 등록을 누르세요."
      buildCreate={(form) => ({
        path: "/api/admin/menus",
        method: "POST",
        body: form,
      })}
      buildSave={(row, form) => ({
        path: `/api/admin/menus/${row.menuId}`,
        method: "PUT",
        body: form,
      })}
    />
  );
}

export function CodeGroupPage() {
  const navigate = useNavigate();
  return (
    <ResourcePage<CodeGroup>
      title="코드그룹 관리"
      desc="코드그룹을 검색·등록·수정하고 상세코드 관리 화면으로 이동합니다."
      path="/api/admin/code-groups"
      filters={[
        { key: "groupId", label: "그룹ID" },
        { key: "groupName", label: "명칭" },
        { key: "managingDepartment", label: "관리부서" },
        { key: "useYn", label: "사용", type: "select", options: ["Y", "N"] },
      ]}
      columns={[
        { key: "groupId", label: "그룹ID" },
        { key: "groupName", label: "명칭" },
        { key: "description", label: "설명" },
        { key: "managingDepartment", label: "관리부서" },
        { key: "useYn", label: "사용" },
      ]}
      fields={[
        { key: "groupId", label: "그룹ID" },
        { key: "groupName", label: "명칭" },
        { key: "description", label: "설명", type: "textarea" },
        { key: "managingDepartment", label: "관리부서" },
        { key: "useYn", label: "사용여부", ...yn },
      ]}
      emptyDetail="코드그룹 행을 선택하거나 신규 등록을 누르세요."
      buildCreate={(form) => ({
        path: `/api/admin/code-groups?groupId=${encodeURIComponent(String(form.groupId || ""))}`,
        method: "POST",
        body: form,
      })}
      buildSave={(row, form) => ({
        path: `/api/admin/code-groups/${row.groupId}`,
        method: "PUT",
        body: form,
      })}
      extraDetail={(row) => (
        <button
          className="btn outline"
          type="button"
          onClick={() => navigate(`/admin/code-groups/${row.groupId}/codes`)}
        >
          상세코드 보기
        </button>
      )}
    />
  );
}

export function CommonCodePage() {
  const { groupId = "" } = useParams();
  return (
    <ResourcePage<CommonCode>
      title={`상세코드 관리 · ${groupId}`}
      desc="그룹별 상세코드 계층, 추가속성, 유효기간과 비활성 정책을 관리합니다."
      path={`/api/admin/code-groups/${groupId}/codes`}
      filters={[
        { key: "codeValue", label: "코드값" },
        { key: "codeName", label: "코드명" },
        { key: "parentCodeValue", label: "상위코드" },
        { key: "useYn", label: "사용", type: "select", options: ["Y", "N"] },
      ]}
      columns={[
        { key: "codeValue", label: "코드값" },
        { key: "codeName", label: "코드명" },
        { key: "parentCodeValue", label: "상위" },
        { key: "sortOrder", label: "순서" },
        { key: "additionalAttributes", label: "추가속성" },
        { key: "validFrom", label: "시작" },
        { key: "validTo", label: "종료" },
        { key: "useYn", label: "사용" },
      ]}
      fields={[
        { key: "codeValue", label: "코드값" },
        { key: "codeName", label: "코드명" },
        { key: "parentCodeValue", label: "상위코드" },
        { key: "sortOrder", label: "정렬순서", type: "number" },
        {
          key: "additionalAttributes",
          label: "추가속성(JSON)",
          type: "textarea",
        },
        { key: "validFrom", label: "유효시작", type: "date" },
        { key: "validTo", label: "유효종료", type: "date" },
        { key: "useYn", label: "사용여부", ...yn },
      ]}
      emptyDetail="상세코드 행을 선택하거나 신규 등록을 누르세요."
      afterSelect={(row) => ({
        additionalAttributes: row.additionalAttributes
          ? JSON.stringify(row.additionalAttributes, null, 2)
          : "",
      })}
      buildCreate={(form) => ({
        path: `/api/admin/code-groups/${groupId}/codes?codeValue=${encodeURIComponent(String(form.codeValue || ""))}`,
        method: "POST",
        body: coerceCode(form),
      })}
      buildSave={(row, form) => ({
        path: `/api/admin/code-groups/${groupId}/codes/${row.codeValue}`,
        method: "PUT",
        body: coerceCode(form),
      })}
    />
  );
}

function coerceCode(form: Record<string, unknown>) {
  const attrs = String(form.additionalAttributes || "").trim();
  let additionalAttributes: Record<string, unknown> | undefined;
  if (attrs) {
    try {
      additionalAttributes = JSON.parse(attrs) as Record<string, unknown>;
    } catch {
      additionalAttributes = { raw: attrs };
    }
  }
  return {
    ...form,
    sortOrder: Number(form.sortOrder || 0),
    additionalAttributes,
  };
}

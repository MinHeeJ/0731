import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";

type UiState = "loading" | "empty" | "error" | "permission" | "success";
type MutationKind = "create" | "update" | "secondary" | "delete";

type MutationConfig = {
  kind: MutationKind;
  label: string;
  method?: "POST" | "PUT" | "PATCH";
  fields?: string[];
  path: (
    selected: Record<string, unknown> | null,
    form: Record<string, string>,
    params: Record<string, string | undefined>,
  ) => string;
  buildBody?: (
    selected: Record<string, unknown> | null,
    form: Record<string, string>,
    params: Record<string, string | undefined>,
  ) => Record<string, unknown>;
  confirm?: string;
};

type ScreenConfig = {
  title: string;
  subtitle: string;
  route: string;
  endpoint: (params: Record<string, string | undefined>) => string;
  entity: string;
  menuPath: string;
  columns: string[];
  readonlyFields: string[];
  defaultFields: string[];
  searchPlaceholder: string;
  mutations: MutationConfig[];
  detailLink?: (row: Record<string, unknown>) => string;
};

const fieldLabels: Record<string, string> = {
  userId: "사용자 ID",
  employeeNo: "교번",
  loginId: "로그인 ID",
  userName: "성명",
  orgCode: "조직코드",
  orgName: "소속",
  orgType: "조직유형",
  parentOrgCode: "상위 조직",
  positionName: "직급",
  dutyName: "보직",
  employmentStatus: "재직상태",
  retirementDate: "퇴직일",
  lastSyncedAt: "동기화시각",
  systemUseYn: "사용여부",
  personnelSource: "인사 원천",
  roleCodes: "업무 역할",
  reason: "변경 사유",
  startDate: "시작일",
  endDate: "종료일",
  status: "상태",
  roleCode: "역할코드",
  roleName: "역할명",
  purpose: "목적",
  grantCriteria: "부여기준",
  defaultDataScope: "기본 데이터 범위",
  useYn: "사용여부",
  assignmentId: "부여 ID",
  validFrom: "유효 시작일",
  validTo: "유효 종료일",
  approverId: "승인자 ID",
  assignmentSource: "부여 출처",
  targetType: "대상 유형",
  targetId: "대상 ID",
  permissionId: "권한 ID",
  menuId: "메뉴 ID",
  menuName: "메뉴명",
  canRead: "조회",
  canCreate: "등록",
  canUpdate: "수정",
  canDisable: "비활성",
  canAccess: "접근",
  screenId: "화면 ID",
  routePath: "라우트",
  iconName: "아이콘",
  businessArea: "업무구분",
  description: "설명",
  sortOrder: "정렬순서",
  groupId: "그룹 ID",
  groupName: "그룹명",
  managingDepartment: "관리부서",
  codeValue: "코드값",
  codeName: "코드명",
  parentCodeValue: "상위 코드",
  extraAttributes: "추가 속성(JSON)",
};

const ynFields = new Set([
  "systemUseYn",
  "useYn",
  "canRead",
  "canCreate",
  "canUpdate",
  "canDisable",
  "canAccess",
]);
const dateFields = new Set([
  "startDate",
  "endDate",
  "validFrom",
  "validTo",
  "retirementDate",
]);
const numberFields = new Set(["sortOrder"]);
const readonlySourceFields = new Set([
  "employeeNo",
  "userName",
  "orgName",
  "positionName",
  "dutyName",
  "retirementDate",
  "lastSyncedAt",
  "employmentStatus",
]);

function label(field: string) {
  return fieldLabels[field] || field;
}

function valueText(value: unknown) {
  if (Array.isArray(value)) return value.join(", ");
  if (value == null) return "";
  return String(value);
}

function splitCsv(value: string) {
  return value
    .split(",")
    .map((part) => part.trim())
    .filter(Boolean);
}

function defaultBody(
  _selected: Record<string, unknown> | null,
  form: Record<string, string>,
) {
  return Object.fromEntries(
    Object.entries(form).filter(([, value]) => value !== ""),
  );
}

export const screens: ScreenConfig[] = [
  {
    title: "사용자 관리",
    subtitle:
      "KORUS 원천 사용자 정보를 조회하고 내부 사용여부와 업무 역할만 관리합니다.",
    route: "/system/users",
    endpoint: () => "/api/admin/users",
    entity: "user_account",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    columns: [
      "employeeNo",
      "userName",
      "orgName",
      "positionName",
      "dutyName",
      "retirementDate",
      "lastSyncedAt",
      "personnelSource",
      "systemUseYn",
      "roleCodes",
    ],
    readonlyFields: [
      "userId",
      "employeeNo",
      "userName",
      "orgCode",
      "orgName",
      "positionName",
      "dutyName",
      "employmentStatus",
      "retirementDate",
      "lastSyncedAt",
      "personnelSource",
    ],
    defaultFields: ["systemUseYn", "roleCodes", "reason"],
    searchPlaceholder: "교번, 성명, 소속, 역할, 사용여부 검색",
    mutations: [
      {
        kind: "update",
        label: "사용여부 저장",
        method: "PATCH",
        fields: ["systemUseYn", "reason"],
        path: (row) => `/api/admin/users/${String(row?.userId)}/usage`,
        buildBody: (_row, form) => ({
          systemUseYn: form.systemUseYn,
          reason: form.reason,
        }),
        confirm: "선택 사용자의 내부 사용여부를 변경합니다.",
      },
      {
        kind: "secondary",
        label: "업무 역할 저장",
        method: "PUT",
        fields: ["roleCodes", "reason"],
        path: (row) => `/api/admin/users/${String(row?.userId)}/roles`,
        buildBody: (_row, form) => ({
          roleCodes: splitCsv(form.roleCodes || ""),
          reason: form.reason,
        }),
        confirm: "쉼표로 입력한 역할 목록으로 현재 업무 역할을 교체합니다.",
      },
    ],
  },
  {
    title: "조직 관리",
    subtitle: "조직 계층을 조회하고 상위 조직 및 적용기간을 저장합니다.",
    route: "/system/organizations",
    endpoint: () => "/api/admin/organizations",
    entity: "organization",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    columns: [
      "orgCode",
      "orgName",
      "orgType",
      "parentOrgCode",
      "startDate",
      "endDate",
      "status",
    ],
    readonlyFields: ["orgCode", "orgName", "orgType", "status", "changedAt"],
    defaultFields: ["parentOrgCode", "startDate", "endDate", "reason"],
    searchPlaceholder: "조직코드, 조직명, 조직유형 검색",
    mutations: [
      {
        kind: "update",
        label: "관계/기간 저장",
        method: "PUT",
        fields: ["parentOrgCode", "startDate", "endDate", "reason"],
        path: (row) =>
          `/api/admin/organizations/${String(row?.orgCode)}/relations`,
        confirm: "조직 관계와 적용기간을 저장합니다.",
      },
    ],
  },
  {
    title: "역할 관리",
    subtitle: "역할 목적, 부여기준, 기본 데이터 범위를 관리합니다.",
    route: "/system/roles",
    endpoint: () => "/api/admin/roles",
    entity: "role",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    columns: [
      "roleCode",
      "roleName",
      "purpose",
      "grantCriteria",
      "defaultDataScope",
      "useYn",
    ],
    readonlyFields: ["roleCode", "useYn"],
    defaultFields: [
      "roleCode",
      "roleName",
      "purpose",
      "grantCriteria",
      "defaultDataScope",
      "reason",
    ],
    searchPlaceholder: "역할코드, 역할명, 목적 검색",
    mutations: [
      {
        kind: "create",
        label: "역할 등록",
        method: "POST",
        fields: [
          "roleCode",
          "roleName",
          "purpose",
          "grantCriteria",
          "defaultDataScope",
          "reason",
        ],
        path: () => "/api/admin/roles",
        confirm: "신규 역할을 등록합니다.",
      },
      {
        kind: "update",
        label: "역할 저장",
        method: "PUT",
        fields: [
          "roleName",
          "purpose",
          "grantCriteria",
          "defaultDataScope",
          "reason",
        ],
        path: (row) => `/api/admin/roles/${String(row?.roleCode)}`,
        confirm: "역할코드는 유지하고 상세 정보를 저장합니다.",
      },
    ],
  },
  {
    title: "사용자 역할 관리",
    subtitle: "사용자별 역할 부여, 기간 변경, 회수 상태를 관리합니다.",
    route: "/system/user-roles",
    endpoint: () => "/api/admin/user-roles",
    entity: "user_role_assignment",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    columns: [
      "assignmentId",
      "employeeNo",
      "userName",
      "roleCode",
      "roleName",
      "validFrom",
      "validTo",
      "assignmentSource",
      "status",
    ],
    readonlyFields: [
      "assignmentId",
      "userId",
      "employeeNo",
      "userName",
      "roleCode",
      "roleName",
      "status",
    ],
    defaultFields: [
      "userId",
      "roleCode",
      "validFrom",
      "validTo",
      "approverId",
      "assignmentSource",
      "reason",
    ],
    searchPlaceholder: "교번, 성명, 역할 검색",
    mutations: [
      {
        kind: "create",
        label: "역할 부여",
        method: "POST",
        fields: [
          "userId",
          "roleCode",
          "validFrom",
          "validTo",
          "approverId",
          "assignmentSource",
          "reason",
        ],
        path: () => "/api/admin/user-roles",
        confirm: "사용자에게 역할을 부여합니다.",
      },
      {
        kind: "update",
        label: "기간 변경",
        method: "PUT",
        fields: [
          "validFrom",
          "validTo",
          "approverId",
          "assignmentSource",
          "reason",
        ],
        path: (row) => `/api/admin/user-roles/${String(row?.assignmentId)}`,
        confirm: "선택 역할의 유효기간을 변경합니다.",
      },
      {
        kind: "delete",
        label: "회수",
        path: (row) => `/api/admin/user-roles/${String(row?.assignmentId)}`,
        confirm: "선택 역할을 회수합니다. 계속할까요?",
      },
    ],
  },
  {
    title: "메뉴 권한 관리",
    subtitle: "역할/조직/사용자 대상별 메뉴 권한 매트릭스를 저장합니다.",
    route: "/system/menu-permissions",
    endpoint: () => "/api/admin/menu-permissions?targetType=ROLE&targetId=R09",
    entity: "menu_permission",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    columns: [
      "targetType",
      "targetId",
      "menuName",
      "canRead",
      "canCreate",
      "canUpdate",
      "canDisable",
      "canAccess",
    ],
    readonlyFields: ["permissionId", "menuId", "menuName", "parentMenuId"],
    defaultFields: [
      "targetType",
      "targetId",
      "menuId",
      "canRead",
      "canCreate",
      "canUpdate",
      "canDisable",
      "canAccess",
      "reason",
    ],
    searchPlaceholder: "기본 조회는 ROLE/R09 입니다",
    mutations: [
      {
        kind: "update",
        label: "권한 저장",
        method: "PUT",
        fields: [
          "targetType",
          "targetId",
          "menuId",
          "canRead",
          "canCreate",
          "canUpdate",
          "canDisable",
          "canAccess",
          "reason",
        ],
        path: () => "/api/admin/menu-permissions",
        confirm: "선택 메뉴 권한을 저장합니다.",
      },
    ],
  },
  {
    title: "메뉴 구조 관리",
    subtitle: "메뉴 계층과 동일 계층 정렬순서를 관리합니다.",
    route: "/system/menu-tree",
    endpoint: () => "/api/admin/menus/tree",
    entity: "menu",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    columns: [
      "menuId",
      "parentMenuId",
      "menuName",
      "routePath",
      "sortOrder",
      "useYn",
      "status",
    ],
    readonlyFields: [
      "menuId",
      "menuName",
      "screenId",
      "routePath",
      "businessArea",
      "status",
    ],
    defaultFields: ["parentMenuId", "sortOrder", "reason"],
    searchPlaceholder: "트리 조회 후 화면 내에서 필터링됩니다",
    mutations: [
      {
        kind: "update",
        label: "상위메뉴 저장",
        method: "PUT",
        fields: ["parentMenuId", "reason"],
        path: (row) => `/api/admin/menus/${String(row?.menuId)}/parent`,
        confirm: "선택 메뉴의 상위 메뉴를 변경합니다.",
      },
      {
        kind: "secondary",
        label: "순서 저장",
        method: "PUT",
        fields: ["menuId", "sortOrder"],
        path: () => "/api/admin/menus/reorder",
        buildBody: (row, form) => ({
          rows: [
            {
              menuId: form.menuId || String(row?.menuId || ""),
              sortOrder: form.sortOrder,
            },
          ],
        }),
        confirm: "선택 메뉴의 표시순서를 저장합니다.",
      },
    ],
  },
  {
    title: "메뉴 정보 관리",
    subtitle: "메뉴 실행 라우트, 화면 ID, 업무구분, 사용여부를 관리합니다.",
    route: "/system/menus",
    endpoint: () => "/api/admin/menus",
    entity: "menu",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    columns: [
      "menuId",
      "menuName",
      "screenId",
      "routePath",
      "iconName",
      "businessArea",
      "useYn",
    ],
    readonlyFields: ["status", "parentMenuId"],
    defaultFields: [
      "menuId",
      "menuName",
      "screenId",
      "routePath",
      "iconName",
      "businessArea",
      "description",
      "sortOrder",
      "useYn",
      "reason",
    ],
    searchPlaceholder: "메뉴명, URL, 사용여부 검색",
    mutations: [
      {
        kind: "create",
        label: "메뉴 등록",
        method: "POST",
        fields: [
          "menuId",
          "parentMenuId",
          "menuName",
          "screenId",
          "routePath",
          "iconName",
          "businessArea",
          "description",
          "sortOrder",
          "useYn",
          "reason",
        ],
        path: () => "/api/admin/menus",
        confirm: "신규 메뉴를 등록합니다.",
      },
      {
        kind: "update",
        label: "메뉴 저장",
        method: "PUT",
        fields: [
          "parentMenuId",
          "menuName",
          "screenId",
          "routePath",
          "iconName",
          "businessArea",
          "description",
          "sortOrder",
          "useYn",
          "reason",
        ],
        path: (row) => `/api/admin/menus/${String(row?.menuId)}`,
        confirm: "선택 메뉴 정보를 저장합니다.",
      },
    ],
  },
  {
    title: "코드그룹 관리",
    subtitle: "코드그룹을 조회하고 관리부서와 설명을 저장합니다.",
    route: "/system/code-groups",
    endpoint: () => "/api/admin/code-groups",
    entity: "code_group",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    columns: [
      "groupId",
      "groupName",
      "managingDepartment",
      "description",
      "useYn",
    ],
    readonlyFields: ["groupId"],
    defaultFields: [
      "groupId",
      "groupName",
      "description",
      "managingDepartment",
      "useYn",
      "reason",
    ],
    searchPlaceholder: "그룹ID, 명칭, 관리부서 검색",
    detailLink: (row) => `/system/code-groups/${String(row.groupId)}/codes`,
    mutations: [
      {
        kind: "create",
        label: "코드그룹 등록",
        method: "POST",
        fields: [
          "groupId",
          "groupName",
          "description",
          "managingDepartment",
          "useYn",
          "reason",
        ],
        path: () => "/api/admin/code-groups",
        confirm: "신규 코드그룹을 등록합니다.",
      },
      {
        kind: "update",
        label: "코드그룹 저장",
        method: "PUT",
        fields: [
          "groupName",
          "description",
          "managingDepartment",
          "useYn",
          "reason",
        ],
        path: (row) => `/api/admin/code-groups/${String(row?.groupId)}`,
        confirm: "선택 코드그룹을 저장합니다.",
      },
    ],
  },
  {
    title: "상세코드 관리",
    subtitle:
      "코드그룹의 상세코드 계층, 정렬, 유효기간, 추가 속성을 관리합니다.",
    route: "/system/code-groups/:groupId/codes",
    endpoint: (params) =>
      `/api/admin/code-groups/${params.groupId || "SYSTEM_STATUS"}/codes`,
    entity: "detail_code",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    columns: [
      "groupId",
      "codeValue",
      "codeName",
      "parentCodeValue",
      "sortOrder",
      "useYn",
      "validFrom",
      "validTo",
    ],
    readonlyFields: ["groupId", "codeValue"],
    defaultFields: [
      "codeValue",
      "codeName",
      "parentCodeValue",
      "sortOrder",
      "useYn",
      "validFrom",
      "validTo",
      "extraAttributes",
      "reason",
    ],
    searchPlaceholder: "상세코드는 현재 그룹 기준으로 조회됩니다",
    mutations: [
      {
        kind: "create",
        label: "상세코드 등록",
        method: "POST",
        fields: [
          "codeValue",
          "codeName",
          "parentCodeValue",
          "sortOrder",
          "useYn",
          "validFrom",
          "validTo",
          "extraAttributes",
          "reason",
        ],
        path: (_row, _form, params) =>
          `/api/admin/code-groups/${params.groupId || "SYSTEM_STATUS"}/codes`,
        confirm: "신규 상세코드를 등록합니다.",
      },
      {
        kind: "update",
        label: "상세코드 저장",
        method: "PUT",
        fields: [
          "codeName",
          "parentCodeValue",
          "sortOrder",
          "useYn",
          "validFrom",
          "validTo",
          "extraAttributes",
          "reason",
        ],
        path: (row) =>
          `/api/admin/code-groups/${String(row?.groupId)}/codes/${String(row?.codeValue)}`,
        confirm: "선택 상세코드를 저장합니다.",
      },
    ],
  },
];

export function configForPath(pathname: string) {
  return (
    screens.find(
      (screen) =>
        screen.route === pathname ||
        (screen.route.includes(":groupId") &&
          pathname.startsWith("/system/code-groups/") &&
          pathname.endsWith("/codes")),
    ) || screens[0]
  );
}

function isPermissionMessage(text: string) {
  return (
    text.includes("권한") ||
    text.includes("401") ||
    text.includes("403") ||
    text.toLowerCase().includes("forbidden") ||
    text.toLowerCase().includes("unauthorized")
  );
}

function cellClass(field: string, value: unknown) {
  const text = valueText(value);
  if (ynFields.has(field))
    return text === "Y" ? "status-badge good" : "status-badge muted";
  if (field === "status")
    return text === "ACTIVE"
      ? "status-badge good"
      : text === "REVOKED" || text === "INACTIVE"
        ? "status-badge muted"
        : "status-badge warn";
  if (field === "roleCodes" || field === "roleCode") return "status-badge info";
  return "";
}

function renderInput(
  field: string,
  value: string,
  onChange: (value: string) => void,
) {
  if (ynFields.has(field)) {
    return (
      <select
        value={value || "Y"}
        onChange={(event) => onChange(event.target.value)}
      >
        <option value="Y">Y · 사용/허용</option>
        <option value="N">N · 미사용/차단</option>
      </select>
    );
  }
  if (dateFields.has(field)) {
    return (
      <input
        type="date"
        value={value || ""}
        onChange={(event) => onChange(event.target.value)}
      />
    );
  }
  if (numberFields.has(field)) {
    return (
      <input
        type="number"
        value={value || ""}
        onChange={(event) => onChange(event.target.value)}
      />
    );
  }
  if (
    field === "description" ||
    field === "purpose" ||
    field === "grantCriteria" ||
    field === "extraAttributes" ||
    field === "reason"
  ) {
    return (
      <textarea
        value={value || ""}
        onChange={(event) => onChange(event.target.value)}
        rows={field === "reason" ? 2 : 3}
      />
    );
  }
  return (
    <input
      value={value || ""}
      onChange={(event) => onChange(event.target.value)}
    />
  );
}

export default function ManagementPage({ pathname }: { pathname: string }) {
  const params = useParams();
  const config = useMemo(() => configForPath(pathname), [pathname]);
  const endpoint = config.endpoint(params);
  const [filter, setFilter] = useState("");
  const [items, setItems] = useState<Array<Record<string, unknown>>>([]);
  const [selected, setSelected] = useState<Record<string, unknown> | null>(
    null,
  );
  const [form, setForm] = useState<Record<string, string>>({
    reason: "관리자 화면 저장",
  });
  const [mode, setMode] = useState<MutationKind>("update");
  const [state, setState] = useState<UiState>("loading");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState("");

  const visibleItems = useMemo(() => {
    if (!filter || endpoint.includes("filter=")) return items;
    const term = filter.toLowerCase();
    return items.filter((row) =>
      Object.values(row).some((value) =>
        valueText(value).toLowerCase().includes(term),
      ),
    );
  }, [endpoint, filter, items]);

  async function load() {
    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      const data = await api.list(
        endpoint,
        endpoint.includes("?") ? "" : filter,
      );
      setItems(data.items);
      const first = data.items[0] || null;
      setSelected(first);
      hydrateForm(first, mode);
      setState(data.items.length ? "success" : "empty");
    } catch (err) {
      const text = err instanceof Error ? err.message : "조회 실패";
      setMessage(text);
      setState(isPermissionMessage(text) ? "permission" : "error");
    }
  }

  function hydrateForm(
    row: Record<string, unknown> | null,
    nextMode: MutationKind,
  ) {
    const mutation = mutationForMode(nextMode);
    const fields = mutation?.fields || config.defaultFields;
    const next: Record<string, string> = { reason: "관리자 화면 저장" };
    fields.forEach((field) => {
      next[field] =
        row?.[field] == null ? next[field] || "" : valueText(row[field]);
    });
    if (nextMode === "create") {
      config.readonlyFields.forEach((field) => {
        if (readonlySourceFields.has(field)) return;
        if (!fields.includes(field)) return;
        next[field] = "";
      });
      if (config.route.includes(":groupId"))
        next.groupId = params.groupId || "SYSTEM_STATUS";
    }
    setForm(next);
  }

  function mutationForMode(nextMode: MutationKind) {
    return (
      config.mutations.find((mutation) => mutation.kind === nextMode) ||
      config.mutations.find((mutation) => mutation.kind === "update") ||
      config.mutations[0]
    );
  }

  function select(row: Record<string, unknown>) {
    setSelected(row);
    const nextMode = mode === "create" ? "update" : mode;
    setMode(nextMode);
    hydrateForm(row, nextMode);
  }

  function switchMode(nextMode: MutationKind) {
    setMode(nextMode);
    hydrateForm(nextMode === "create" ? null : selected, nextMode);
  }

  function validate(fields: string[]) {
    const required = fields.filter(
      (field) =>
        field !== "reason" &&
        field !== "validTo" &&
        field !== "endDate" &&
        field !== "parentOrgCode" &&
        field !== "parentCodeValue" &&
        field !== "description" &&
        field !== "extraAttributes" &&
        field !== "iconName" &&
        field !== "routePath" &&
        field !== "screenId" &&
        field !== "approverId",
    );
    const missing = required.find((field) => !String(form[field] || "").trim());
    if (missing) return `${label(missing)} 값이 필요합니다.`;
    if (form.validFrom && form.validTo && form.validTo < form.validFrom)
      return "유효 종료일은 시작일보다 빠를 수 없습니다.";
    if (form.startDate && form.endDate && form.endDate < form.startDate)
      return "종료일은 시작일보다 빠를 수 없습니다.";
    if (form.extraAttributes) {
      try {
        JSON.parse(form.extraAttributes);
      } catch {
        return "추가 속성은 올바른 JSON이어야 합니다.";
      }
    }
    return "";
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    const mutation = mutationForMode(mode);
    if (!mutation) return;
    if (mutation.kind !== "create" && !selected) return;
    const fields = mutation.fields || config.defaultFields;
    const validation = mutation.kind === "delete" ? "" : validate(fields);
    if (validation) {
      setFieldError(validation);
      return;
    }
    const confirmText =
      mutation.confirm || `${mutation.label} 처리하시겠습니까?`;
    if (!window.confirm(confirmText)) return;
    setState("loading");
    setFieldError("");
    try {
      if (mutation.kind === "delete") {
        await api.remove(mutation.path(selected, form, params));
      } else if (mutation.method) {
        const body = mutation.buildBody
          ? mutation.buildBody(selected, form, params)
          : defaultBody(selected, form);
        if (mutation.label === "순서 저장" && "rows" in body) {
          await api.save(
            mutation.path(selected, form, params),
            mutation.method,
            body.rows as unknown as Record<string, unknown>,
          );
        } else {
          await api.save(
            mutation.path(selected, form, params),
            mutation.method,
            body,
          );
        }
      }
      setMessage(`${mutation.label} 완료`);
      await load();
      setState("success");
    } catch (err) {
      const text = err instanceof Error ? err.message : "처리 실패";
      setMessage(text);
      setState(isPermissionMessage(text) ? "permission" : "error");
    }
  }

  useEffect(() => {
    void load();
  }, [endpoint]);

  const activeMutation = mutationForMode(mode);
  const activeFields = activeMutation?.fields || config.defaultFields;

  return (
    <section className="management-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">{config.menuPath}</p>
          <h2>{config.title}</h2>
          <p className="page-description">{config.subtitle}</p>
        </div>
        <form
          className="toolbar"
          onSubmit={(event) => {
            event.preventDefault();
            void load();
          }}
        >
          <input
            value={filter}
            onChange={(event) => setFilter(event.target.value)}
            placeholder={config.searchPlaceholder}
            aria-label="검색조건"
          />
          <button type="submit">검색</button>
          <button
            type="button"
            className="secondary"
            onClick={() => {
              setFilter("");
              void load();
            }}
          >
            새로고침
          </button>
        </form>
      </div>

      <div className={`state-banner ${state}`} role="status">
        {state === "loading" && "데이터를 불러오거나 처리하는 중입니다..."}
        {state === "empty" &&
          "조회 결과가 없습니다. 검색어를 바꾸거나 등록 버튼을 사용하세요."}
        {state === "error" && message}
        {state === "permission" &&
          "권한이 없어 화면을 표시할 수 없습니다. R09 권한과 세션을 확인하세요."}
        {state === "success" &&
          (message || `${config.title} 데이터가 준비되었습니다.`)}
      </div>

      <div className="content-grid">
        <article className="card table-card">
          <div className="card-header compact">
            <div>
              <h3>목록</h3>
              <p>
                {visibleItems.length}건 표시 · API {endpoint}
              </p>
            </div>
            {config.mutations.some(
              (mutation) => mutation.kind === "create",
            ) && (
              <button
                type="button"
                className="outline"
                onClick={() => switchMode("create")}
              >
                등록 모드
              </button>
            )}
          </div>
          <div className="data-table-wrap">
            <table>
              <thead>
                <tr>
                  {config.columns.map((column) => (
                    <th key={column}>{label(column)}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {state === "loading" &&
                  Array.from({ length: 5 }).map((_, index) => (
                    <tr key={`skeleton-${index}`}>
                      {config.columns.map((column) => (
                        <td key={column}>
                          <span className="skeleton" />
                        </td>
                      ))}
                    </tr>
                  ))}
                {state !== "loading" &&
                  visibleItems.map((row, index) => (
                    <tr
                      key={`${config.entity}-${index}`}
                      onClick={() => select(row)}
                      className={row === selected ? "selected" : ""}
                    >
                      {config.columns.map((column) => (
                        <td key={column}>
                          <span className={cellClass(column, row[column])}>
                            {valueText(row[column]) || "-"}
                          </span>
                        </td>
                      ))}
                    </tr>
                  ))}
                {state !== "loading" && visibleItems.length === 0 && (
                  <tr>
                    <td colSpan={config.columns.length} className="empty-row">
                      표시할 데이터가 없습니다.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </article>

        <aside className="card detail-card">
          <div className="card-header compact">
            <div>
              <h3>{mode === "create" ? "등록 폼" : "상세/수정"}</h3>
              <p>{activeMutation?.label || "선택 행 작업"}</p>
            </div>
          </div>
          <div className="readonly-grid">
            {(selected && mode !== "create" ? config.readonlyFields : []).map(
              (field) => (
                <div key={field}>
                  <span>{label(field)}</span>
                  <strong>{valueText(selected?.[field]) || "-"}</strong>
                </div>
              ),
            )}
            {(!selected || mode === "create") && (
              <p className="empty-note">
                {mode === "create"
                  ? "필수값을 입력해 새 항목을 등록하세요."
                  : "목록에서 행을 선택하면 상세 정보가 표시됩니다."}
              </p>
            )}
          </div>
          <form className="form-grid" onSubmit={submit}>
            <div className="mode-tabs" role="tablist" aria-label="작업 선택">
              {config.mutations.map((mutation) => (
                <button
                  key={mutation.label}
                  type="button"
                  className={mode === mutation.kind ? "tab active" : "tab"}
                  onClick={() => switchMode(mutation.kind)}
                >
                  {mutation.label}
                </button>
              ))}
            </div>
            {activeFields.map((field) => (
              <label key={field}>
                {label(field)}
                {renderInput(field, form[field] || "", (value) =>
                  setForm({ ...form, [field]: value }),
                )}
              </label>
            ))}
            {fieldError && <div className="field-error">{fieldError}</div>}
            <div className="action-row">
              {config.detailLink && selected && mode !== "create" && (
                <Link
                  className="button-like outline"
                  to={config.detailLink(selected)}
                >
                  상세코드 이동
                </Link>
              )}
              <button
                className={mode === "delete" ? "danger" : "primary"}
                type="submit"
                disabled={mode !== "create" && !selected}
              >
                {activeMutation?.label || "저장"}
              </button>
            </div>
          </form>
        </aside>
      </div>
    </section>
  );
}

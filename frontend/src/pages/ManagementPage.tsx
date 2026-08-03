import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getList, mutate } from "../api/adminApi";
import { FieldError, StateBanner } from "../components/State";

type Row = Record<string, unknown>;
type Config = {
  title: string;
  goal: string;
  archetype: string;
  path: string;
  columns: string[];
  filters: string[];
  fields: string[];
  readonly?: string[];
  identity?: string[];
  allowCreate?: boolean;
  primary: string;
  savePath: (row: Row, form: Record<string, string>) => string;
  method: (row: Row) => string;
  empty: string;
  detailLink?: (row: Row) => string;
  note?: string;
};

const labels: Record<string, string> = {
  staffNo: "교번",
  staffName: "성명",
  organizationCode: "조직코드",
  organizationName: "조직명",
  organizationType: "조직유형",
  jobTitle: "직급",
  employmentStatus: "재직상태",
  roleCodes: "업무 역할",
  useYn: "사용여부",
  positionName: "보직",
  retirementDate: "퇴직일자",
  lastSyncedAt: "최종 동기화일시",
  name: "성명",
  position: "직급",
  roleCode: "역할코드",
  roleName: "역할명",
  purpose: "목적",
  assignmentCriteria: "부여 기준",
  defaultDataScope: "데이터 범위 기본값",
  changeReason: "변경사유",
  parentOrganizationCode: "상위조직",
  effectiveStartDate: "적용 시작일",
  effectiveEndDate: "적용 종료일",
  assignmentId: "부여ID",
  userId: "사용자ID",
  assignmentType: "부여유형",
  approvedBy: "승인자",
  status: "상태",
  targetType: "대상 유형",
  targetId: "대상 ID",
  menuId: "메뉴ID",
  menuName: "메뉴명",
  decision: "권한 결정",
  apiPathPattern: "API path pattern",
  permissionId: "권한ID",
  parentMenuId: "상위 메뉴ID",
  menuType: "메뉴유형",
  displayOrder: "표시순서",
  screenId: "화면ID",
  url: "URL",
  icon: "아이콘",
  businessCategory: "업무구분",
  description: "설명",
  groupId: "코드그룹ID",
  groupName: "코드그룹명",
  managingDepartment: "관리부서",
  codeValue: "코드값",
  codeName: "코드명",
  parentCodeValue: "상위코드",
  additionalAttributes: "추가속성(JSON)",
};

const selectOptions: Record<string, string[]> = {
  useYn: ["Y", "N"],
  status: ["ACTIVE", "INACTIVE", "REVOKED"],
  targetType: ["ROLE", "ORGANIZATION", "USER"],
  decision: ["ALLOW", "DENY"],
  assignmentType: ["POSITION", "MANUAL"],
  employmentStatus: ["ACTIVE", "RETIRED", "LEAVE"],
};

const configs: Record<string, Config> = {
  users: {
    title: "사용자 관리",
    goal: "교번·성명·소속·직급·재직상태·역할·사용여부로 검색하고 사용여부와 업무 역할만 저장합니다.",
    archetype: "SEARCH_LIST_DETAIL",
    path: "/api/users",
    columns: [
      "staffNo",
      "staffName",
      "organizationName",
      "jobTitle",
      "employmentStatus",
      "roleCodes",
      "useYn",
      "positionName",
      "retirementDate",
      "lastSyncedAt",
    ],
    filters: [
      "staffNo",
      "name",
      "organizationCode",
      "position",
      "employmentStatus",
      "roleCode",
      "useYn",
    ],
    fields: [
      "staffNo",
      "staffName",
      "organizationCode",
      "jobTitle",
      "employmentStatus",
      "useYn",
      "roleCodes",
      "changeReason",
    ],
    readonly: [
      "staffNo",
      "staffName",
      "organizationCode",
      "jobTitle",
      "employmentStatus",
    ],
    identity: ["staffNo"],
    primary: "userId",
    savePath: (r) => `/api/users/${r.userId}/usage`,
    method: () => "PATCH",
    empty: "조건에 맞는 사용자가 없습니다",
    note: "KORUS 원천 인사정보는 읽기 전용이며 저장 payload에서 제외됩니다.",
  },
  organizations: {
    title: "조직 관리",
    goal: "조직 기본정보는 보존하고 선택 조직의 상위조직·적용기간 관계만 저장합니다.",
    archetype: "TREE_EDITOR",
    path: "/api/organizations",
    columns: [
      "organizationCode",
      "organizationName",
      "organizationType",
      "status",
      "useYn",
    ],
    filters: ["organizationCode"],
    fields: [
      "organizationCode",
      "organizationName",
      "organizationType",
      "parentOrganizationCode",
      "effectiveStartDate",
      "effectiveEndDate",
      "changeReason",
    ],
    readonly: ["organizationCode", "organizationName", "organizationType"],
    primary: "organizationCode",
    savePath: (r) => `/api/organizations/${r.organizationCode}/relations`,
    method: () => "PUT",
    empty: "조직 계층이 없습니다",
    note: "종료일이 시작일보다 빠르면 backend field 오류가 표시됩니다.",
  },
  roles: {
    title: "역할 관리",
    goal: "R01~R09 역할 목적·부여 기준·데이터 범위 기본값을 조회하고 role_code lifecycle identity를 유지합니다.",
    archetype: "SEARCH_LIST_DETAIL",
    path: "/api/roles",
    columns: [
      "roleCode",
      "roleName",
      "purpose",
      "assignmentCriteria",
      "defaultDataScope",
      "useYn",
    ],
    filters: ["roleCode", "roleName"],
    fields: [
      "roleCode",
      "roleName",
      "purpose",
      "assignmentCriteria",
      "defaultDataScope",
      "useYn",
      "changeReason",
    ],
    identity: ["roleCode"],
    allowCreate: true,
    primary: "roleCode",
    savePath: (r) => (r.roleCode ? `/api/roles/${r.roleCode}` : "/api/roles"),
    method: (r) => (r.roleCode ? "PUT" : "POST"),
    empty: "역할이 없습니다",
  },
  "user-roles": {
    title: "사용자 역할 관리",
    goal: "사용자별 현재 역할을 보고 MANUAL 역할 유효기간·승인자를 부여/변경/회수합니다.",
    archetype: "EFFECTIVE_PERIOD_FORM",
    path: "/api/user-roles",
    columns: [
      "assignmentId",
      "userId",
      "roleCode",
      "assignmentType",
      "approvedBy",
      "effectiveStartDate",
      "effectiveEndDate",
      "status",
    ],
    filters: ["userId", "roleCode", "status"],
    fields: [
      "userId",
      "roleCode",
      "approvedBy",
      "effectiveStartDate",
      "effectiveEndDate",
      "changeReason",
    ],
    readonly: [],
    allowCreate: true,
    primary: "assignmentId",
    savePath: (r) =>
      r.assignmentId ? `/api/user-roles/${r.assignmentId}` : "/api/user-roles",
    method: (r) => (r.assignmentId ? "PUT" : "POST"),
    empty: "부여된 역할이 없습니다",
    note: "POSITION 역할은 직접 변경/회수할 수 없습니다. 회수는 확인 후 DELETE로 status=REVOKED를 기록합니다.",
  },
  "menu-permissions": {
    title: "메뉴 권한 관리",
    goal: "역할·조직·사용자 대상과 메뉴 행을 ALLOW/DENY 매트릭스로 저장합니다.",
    archetype: "PERMISSION_MATRIX",
    path: "/api/menu-permissions",
    columns: [
      "targetType",
      "targetId",
      "menuId",
      "decision",
      "apiPathPattern",
      "useYn",
    ],
    filters: ["targetType", "targetId"],
    fields: [
      "targetType",
      "targetId",
      "menuId",
      "decision",
      "apiPathPattern",
      "changeReason",
    ],
    primary: "permissionId",
    allowCreate: true,
    savePath: () => "/api/menu-permissions",
    method: () => "PUT",
    empty: "권한 설정이 없습니다",
    note: "권한 우선순위: USER > ORGANIZATION > ROLE, DENY 우선.",
  },
  "menu-structure": {
    title: "메뉴 구조 관리",
    goal: "메뉴 계층에서 부모메뉴 지정과 동일 계층 표시순서를 저장합니다.",
    archetype: "TREE_EDITOR",
    path: "/api/menus/tree",
    columns: [
      "menuId",
      "parentMenuId",
      "menuType",
      "menuName",
      "displayOrder",
      "url",
    ],
    filters: ["menuName", "menuId"],
    fields: ["menuId", "parentMenuId", "displayOrder", "changeReason"],
    readonly: ["menuId"],
    identity: ["menuId"],
    primary: "menuId",
    savePath: (r) => `/api/menus/${r.menuId}/parent`,
    method: () => "PUT",
    empty: "메뉴 계층이 없습니다",
    note: "자기 자신을 parent_menu_id로 지정하면 backend field 오류가 표시됩니다.",
  },
  menus: {
    title: "메뉴 정보 관리",
    goal: "메뉴명·화면ID·URL·아이콘·업무구분·설명 실행정보를 등록/수정합니다.",
    archetype: "SEARCH_LIST_DETAIL",
    path: "/api/menus",
    columns: [
      "menuId",
      "menuName",
      "screenId",
      "url",
      "icon",
      "businessCategory",
      "description",
      "useYn",
    ],
    filters: ["menuName", "screenId", "url", "businessCategory"],
    fields: [
      "menuId",
      "menuName",
      "screenId",
      "url",
      "icon",
      "businessCategory",
      "description",
      "useYn",
      "changeReason",
    ],
    identity: ["menuId"],
    allowCreate: true,
    primary: "menuId",
    savePath: (r) => (r.menuId ? `/api/menus/${r.menuId}` : "/api/menus"),
    method: (r) => (r.menuId ? "PUT" : "POST"),
    empty: "메뉴 실행정보가 없습니다",
    note: "ui-design 범위 밖 route placeholder는 만들지 않습니다.",
  },
  "code-groups": {
    title: "코드그룹 관리",
    goal: "코드그룹을 등록/수정하고 선택 group_id를 상세코드 화면으로 전달합니다.",
    archetype: "SEARCH_LIST_DETAIL",
    path: "/api/code-groups",
    columns: [
      "groupId",
      "groupName",
      "description",
      "managingDepartment",
      "status",
      "useYn",
    ],
    filters: ["groupId", "groupName", "managingDepartment", "status"],
    fields: [
      "groupId",
      "groupName",
      "description",
      "managingDepartment",
      "status",
      "useYn",
      "changeReason",
    ],
    identity: ["groupId"],
    allowCreate: true,
    primary: "groupId",
    savePath: (r) =>
      r.groupId ? `/api/code-groups/${r.groupId}` : "/api/code-groups",
    method: (r) => (r.groupId ? "PUT" : "POST"),
    empty: "코드그룹이 없습니다",
    detailLink: (r) => `/system/code-groups/${r.groupId}/detail-codes`,
  },
  "detail-codes": {
    title: "상세코드 관리",
    goal: "코드그룹별 상세코드 계층과 코드값·상위코드·정렬순서·추가속성을 관리합니다.",
    archetype: "TREE_EDITOR",
    path: "",
    columns: [
      "groupId",
      "codeValue",
      "codeName",
      "parentCodeValue",
      "displayOrder",
      "useYn",
      "effectiveStartDate",
      "effectiveEndDate",
    ],
    filters: ["groupId", "codeValue", "status"],
    fields: [
      "codeValue",
      "codeName",
      "parentCodeValue",
      "displayOrder",
      "additionalAttributes",
      "effectiveStartDate",
      "effectiveEndDate",
      "status",
      "useYn",
      "changeReason",
    ],
    identity: ["codeValue"],
    allowCreate: true,
    primary: "codeValue",
    savePath: (r, f) =>
      r.codeValue
        ? `/api/code-groups/${f.groupId}/detail-codes/${r.codeValue}`
        : `/api/code-groups/${f.groupId}/detail-codes`,
    method: (r) => (r.codeValue ? "PUT" : "POST"),
    empty: "상세코드가 없습니다",
    note: "상위코드는 같은 group_id 안의 상세코드만 입력해야 합니다.",
  },
};

function optionsFor(type: keyof typeof configs, field: string) {
  if (field !== "status") return selectOptions[field];
  if (type === "user-roles") return ["ACTIVE", "REVOKED", "EXPIRED"];
  if (type === "code-groups" || type === "detail-codes")
    return ["ACTIVE", "INACTIVE"];
  return selectOptions[field];
}

function isPermission(message: string) {
  return (
    message.includes("권한") ||
    message.includes("401") ||
    message.includes("403") ||
    message.includes("인증")
  );
}

function normalizeBody(
  cfg: Config,
  type: string,
  form: Record<string, string>,
  selected: Row | null,
) {
  const body: Record<string, unknown> = {};
  cfg.fields.forEach((field) => {
    if (cfg.readonly?.includes(field)) return;
    if (selected?.[cfg.primary] && cfg.identity?.includes(field)) return;
    if (type === "users" && field === "roleCodes") return;
    if (field === "roleCodes") {
      body[field] =
        form[field]
          ?.split(",")
          .map((v) => v.trim())
          .filter(Boolean) || [];
    } else {
      body[field] = form[field] ?? "";
    }
  });
  if (type === "user-roles" && !selected?.[cfg.primary]) {
    body.assignmentType = "MANUAL";
  }
  if (type === "menu-permissions") return { ...body, permissions: [body] };
  return body;
}

function formatCell(value: unknown) {
  if (Array.isArray(value)) return value.join(", ");
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

export function ManagementPage({ type }: { type: keyof typeof configs }) {
  const params = useParams();
  const cfg = configs[type];
  const defaultGroupId = params.groupId || "EVAL_AREA";
  const [filters, setFilters] = useState<Record<string, string>>({
    groupId: defaultGroupId,
  });
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [form, setForm] = useState<Record<string, string>>({
    groupId: defaultGroupId,
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [state, setState] = useState<
    "empty" | "error" | "success" | "permission" | ""
  >("");
  const [err, setErr] = useState<Record<string, string>>({});
  const [confirmRevoke, setConfirmRevoke] = useState(false);

  const path = useMemo(
    () =>
      type === "detail-codes"
        ? `/api/code-groups/${filters.groupId || defaultGroupId}/detail-codes`
        : cfg.path,
    [type, filters.groupId, defaultGroupId, cfg.path],
  );

  async function load() {
    setLoading(true);
    setMessage("");
    setState("");
    const response = await getList(path, filters);
    setLoading(false);
    if (response.success) {
      const data = response.data || [];
      setRows(data);
      setState(data.length === 0 ? "empty" : "");
      setMessage(data.length === 0 ? cfg.empty : "");
    } else {
      const msg = response.error?.message || "조회 오류";
      setMessage(msg);
      setState(isPermission(msg) ? "permission" : "error");
    }
  }

  useEffect(() => {
    load();
  }, [path]);

  function choose(row: Row) {
    setSelected(row);
    setErr({});
    setForm({
      ...Object.fromEntries(
        Object.entries(row).map(([k, v]) => [
          k,
          Array.isArray(v) ? v.join(",") : String(v ?? ""),
        ]),
      ),
      groupId: filters.groupId || defaultGroupId || String(row.groupId || ""),
    });
  }

  function resetForm() {
    setSelected(null);
    setErr({});
    setForm({ groupId: filters.groupId || defaultGroupId });
  }

  async function save(e: FormEvent) {
    e.preventDefault();
    if (!selected && !cfg.allowCreate) {
      setState("error");
      setMessage("목록에서 저장할 행을 먼저 선택하세요.");
      return;
    }
    if (selected?.assignmentType === "POSITION") {
      setState("error");
      setMessage("POSITION 역할은 직접 변경할 수 없습니다.");
      return;
    }
    const target = cfg.savePath(selected || {}, form);
    const body = normalizeBody(cfg, type, form, selected);
    let response = await mutate(target, cfg.method(selected || {}), body);

    if (
      response.success &&
      type === "users" &&
      selected?.userId &&
      form.roleCodes !== undefined
    ) {
      response = await mutate(`/api/users/${selected.userId}/roles`, "PUT", {
        roleCodes: form.roleCodes
          .split(",")
          .map((v) => v.trim())
          .filter(Boolean),
        changeReason: form.changeReason || "",
      });
    }

    if (response.success) {
      setMessage("저장되었습니다. 최신 목록을 재조회했습니다.");
      setState("success");
      setErr({});
      await load();
      setMessage("저장되었습니다. 최신 목록을 재조회했습니다.");
      setState("success");
    } else {
      const msg = response.error?.message || "저장 오류";
      setMessage(msg);
      setState(isPermission(msg) ? "permission" : "error");
      setErr(response.error?.fields || {});
    }
  }

  async function revoke() {
    if (!selected?.assignmentId) return;
    const response = await mutate(
      `/api/user-roles/${selected.assignmentId}`,
      "DELETE",
      { changeReason: form.changeReason || "" },
    );
    setConfirmRevoke(false);
    if (response.success) {
      setMessage("회수되었습니다. status=REVOKED 목록을 재조회했습니다.");
      setState("success");
      await load();
    } else {
      const msg = response.error?.message || "회수 오류";
      setMessage(msg);
      setState("error");
      setErr(response.error?.fields || {});
    }
  }

  async function saveOrder() {
    const response = await mutate("/api/menus/reorder", "PUT", {
      items: rows.map((r) => ({
        menuId: r.menuId,
        displayOrder: r.displayOrder,
      })),
      changeReason: form.changeReason || "",
    });
    if (response.success) {
      setMessage("표시순서를 저장했습니다.");
      setState("success");
      await load();
    } else {
      setMessage(response.error?.message || "표시순서 저장 오류");
      setState("error");
      setErr(response.error?.fields || {});
    }
  }

  const checklist = [
    `route: ${type === "detail-codes" ? "/system/code-groups/:groupId/detail-codes" : location.pathname}`,
    `archetype: ${cfg.archetype}`,
    `CTA: 조회 → 선택 → 저장${type === "user-roles" ? " / 회수 확인" : ""}`,
    "states: loading / empty / error / permission / success",
  ];

  return (
    <section className="page-stack" aria-busy={loading}>
      <div className="page-heading">
        <div>
          <span className="eyebrow">{cfg.archetype}</span>
          <h1>{cfg.title}</h1>
          <p>{cfg.goal}</p>
        </div>
        <div className="checklist-card" aria-label="UI Contract checklist">
          {checklist.map((item) => (
            <span key={item}>{item}</span>
          ))}
        </div>
      </div>

      {loading && (
        <StateBanner
          type="loading"
          message="조회 중입니다. 데이터 도착 전 skeleton 상태입니다."
        />
      )}
      {message && state && <StateBanner type={state} message={message} />}
      {cfg.note && <div className="info-note">{cfg.note}</div>}

      <div className="card search-card">
        <div className="card-header-row">
          <div>
            <h2>검색조건</h2>
            <p>값이 있는 조건만 query string으로 전달합니다.</p>
          </div>
          <button className="button" type="button" onClick={load}>
            검색
          </button>
        </div>
        <div className="form-grid compact">
          {cfg.filters.map((field) => {
            const options = optionsFor(type, field);
            return (
              <label className="field" key={field}>
                <span>{labels[field] || field}</span>
                {options ? (
                  <select
                    value={filters[field] || ""}
                    onChange={(e) =>
                      setFilters({ ...filters, [field]: e.target.value })
                    }
                  >
                    <option value="">전체</option>
                    {options.map((option) => (
                      <option key={option} value={option}>
                        {option}
                      </option>
                    ))}
                  </select>
                ) : (
                  <input
                    value={filters[field] || ""}
                    onChange={(e) =>
                      setFilters({ ...filters, [field]: e.target.value })
                    }
                  />
                )}
              </label>
            );
          })}
        </div>
      </div>

      <div className="card table-card">
        <div className="card-header-row">
          <div>
            <h2>{cfg.archetype.includes("TREE") ? "계층/목록" : "목록"}</h2>
            <p>{rows.length}건 조회됨</p>
          </div>
          {type === "menu-structure" && (
            <button
              type="button"
              className="button button-outline"
              onClick={saveOrder}
            >
              표시순서 저장
            </button>
          )}
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                {cfg.columns.map((column) => (
                  <th key={column}>{labels[column] || column}</th>
                ))}
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td className="empty-row" colSpan={cfg.columns.length + 1}>
                    {cfg.empty}
                  </td>
                </tr>
              ) : (
                rows.map((row, index) => (
                  <tr
                    key={`${row[cfg.primary] || index}`}
                    className={selected === row ? "selected" : ""}
                    onClick={() => choose(row)}
                  >
                    {cfg.columns.map((column) => (
                      <td key={column} data-label={labels[column] || column}>
                        {formatCell(row[column])}
                      </td>
                    ))}
                    <td>
                      <div className="row-actions">
                        {cfg.detailLink && (
                          <Link
                            className="button button-link"
                            to={cfg.detailLink(row)}
                          >
                            상세코드 이동
                          </Link>
                        )}
                        <button
                          className="button button-outline"
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            choose(row);
                          }}
                        >
                          선택
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <form className="card editor-card" onSubmit={save}>
        <div className="card-header-row">
          <div>
            <h2>{selected ? "상세/편집" : "신규 등록"}</h2>
            <p>
              읽기 전용/lifecycle identity 필드는 계약에 따라 저장 payload에서
              제외됩니다.
            </p>
          </div>
          {selected && (
            <span className="badge">
              선택: {formatCell(selected[cfg.primary])}
            </span>
          )}
        </div>
        <div className="form-grid">
          {cfg.fields.map((field) => {
            const readonly =
              cfg.readonly?.includes(field) ||
              (selected?.[cfg.primary] && cfg.identity?.includes(field));
            const options = optionsFor(type, field);
            return (
              <label className="field" key={field}>
                <span>
                  {labels[field] || field}
                  {readonly ? " (readonly)" : ""}
                </span>
                {options && !readonly ? (
                  <select
                    value={form[field] || ""}
                    onChange={(e) =>
                      setForm({ ...form, [field]: e.target.value })
                    }
                  >
                    <option value="">선택</option>
                    {options.map((option) => (
                      <option key={option} value={option}>
                        {option}
                      </option>
                    ))}
                  </select>
                ) : field === "changeReason" ||
                  field === "description" ||
                  field === "additionalAttributes" ? (
                  <textarea
                    className={readonly ? "readonly" : ""}
                    readOnly={!!readonly}
                    rows={3}
                    value={form[field] || ""}
                    onChange={(e) =>
                      setForm({ ...form, [field]: e.target.value })
                    }
                  />
                ) : (
                  <input
                    className={readonly ? "readonly" : ""}
                    readOnly={!!readonly}
                    value={form[field] || ""}
                    onChange={(e) =>
                      setForm({ ...form, [field]: e.target.value })
                    }
                  />
                )}
                <FieldError name={field} fields={err} />
              </label>
            );
          })}
        </div>
        {type === "user-roles" && selected?.assignmentType === "POSITION" && (
          <p className="badge badge-warning">
            POSITION 역할은 직접 변경/회수할 수 없습니다.
          </p>
        )}
        {type === "menu-permissions" && (
          <div className="matrix-note">
            ALLOW/DENY 중 하나를 decision에 입력해 저장합니다. 저장 후 canonical
            GET으로 재조회됩니다.
          </div>
        )}
        <div className="action-bar">
          <button className="button" type="submit">
            {selected ? "저장" : "등록"}
          </button>
          {type === "user-roles" &&
            selected?.assignmentType === "MANUAL" &&
            Boolean(selected?.assignmentId) && (
              <button
                className="button button-danger"
                type="button"
                onClick={() => setConfirmRevoke(true)}
              >
                회수
              </button>
            )}
          <button
            className="button button-outline"
            type="button"
            onClick={resetForm}
          >
            취소
          </button>
        </div>
      </form>

      {confirmRevoke && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal card">
            <h2>MANUAL 역할 회수 확인</h2>
            <p>
              선택한 역할을 REVOKED 상태로 기록합니다. 이 작업은 backend DELETE
              /api/user-roles/{"{assignmentId}"}를 호출합니다.
            </p>
            <div className="action-bar">
              <button
                className="button button-danger"
                type="button"
                onClick={revoke}
              >
                확인
              </button>
              <button
                className="button button-outline"
                type="button"
                onClick={() => setConfirmRevoke(false)}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

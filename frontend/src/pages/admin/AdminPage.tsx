import {
  AlertTriangle,
  Building2,
  ChevronRight,
  Database,
  KeyRound,
  LayoutDashboard,
  Loader2,
  Lock,
  Menu as MenuIcon,
  RefreshCw,
  Save,
  Search,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import {
  NavLink,
  useLocation,
  useNavigate,
  useSearchParams,
} from "react-router-dom";
import { api } from "../../api/client";

type Row = Record<string, unknown>;
type Column = {
  key: string;
  label: string;
  editable?: boolean;
  kind?: "text" | "select" | "date" | "number" | "textarea" | "checkbox";
  options?: string[];
};
type Action = {
  label: string;
  path: (row: Row) => string;
  method: string;
  body: (row: Row) => Row;
  requiresSelection?: boolean;
  confirm: string;
};
export type ScreenConfig = {
  route: string;
  title: string;
  menuPath: string;
  entity: string;
  archetype: string;
  listPath: (params: URLSearchParams) => string;
  columns: Column[];
  actions: Action[];
  helper: string;
  emptyText: string;
  createDraft?: Row;
};

const useYn = ["Y", "N"];
export const roleCodes = [
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
const statusCodes = ["ACTIVE", "INACTIVE", "REVOKED", "EXPIRED"];
const menuTypes = ["TOP", "MIDDLE", "SCREEN"];
const targetTypes = ["ROLE", "ORG", "USER"];
const assignmentTypes = ["POSITION_BASED", "MANUAL"];

export const screenConfigs: ScreenConfig[] = [
  {
    route: "/admin/users",
    title: "사용자 관리",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    entity: "app_user",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: () => "/api/admin/users",
    helper:
      "KORUS 원천 필드는 readonly이며 로컬 사용여부와 업무 역할만 저장합니다.",
    emptyText: "조건에 맞는 사용자가 없습니다.",
    columns: [
      c("userId", "교번"),
      c("name", "성명"),
      c("orgName", "소속"),
      c("rankName", "직급"),
      c("employmentStatus", "재직상태"),
      c("roleCodes", "역할", true),
      c("useYn", "사용여부", true, "select", useYn),
      c("positionName", "보직"),
      c("retiredAt", "퇴직일자"),
      c("lastSyncedAt", "동기화"),
    ],
    actions: [
      {
        label: "사용여부 저장",
        method: "PATCH",
        requiresSelection: true,
        confirm: "로컬 사용여부만 변경합니다.",
        path: (r) => `/api/admin/users/${r.userId}/account`,
        body: (r) => ({ useYn: r.useYn, reason: r.reason || "사용여부 변경" }),
      },
      {
        label: "업무역할 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "업무 역할 배정을 저장합니다.",
        path: (r) => `/api/admin/users/${r.userId}/roles`,
        body: (r) => ({
          roleCodes: String(r.roleCodes || "")
            .split(",")
            .map((v) => v.trim())
            .filter(Boolean),
          reason: r.reason || "역할 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/orgs",
    title: "조직 관리",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    entity: "org",
    archetype: "TREE_EDITOR",
    listPath: () => "/api/admin/orgs/tree",
    helper: "조직 트리에서 선택 노드의 상위조직과 적용기간을 관리합니다.",
    emptyText: "조직 트리 데이터가 없습니다.",
    columns: [
      c("orgCode", "조직코드"),
      c("orgName", "조직명"),
      c("orgType", "조직유형"),
      c("parentOrgCode", "상위조직", true),
      c("validFrom", "적용시작", true, "date"),
      c("validTo", "종료", true, "date"),
    ],
    actions: [
      {
        label: "관계 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "선택 조직의 상위조직과 적용기간을 저장합니다.",
        path: (r) => `/api/admin/org-relations/${r.relationId}`,
        body: (r) => ({
          parentOrgCode: r.parentOrgCode,
          validFrom: r.validFrom,
          validTo: r.validTo,
          reason: r.reason || "조직 관계 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/roles",
    title: "역할 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    entity: "role",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: () => "/api/admin/roles",
    helper:
      "R01~R09 역할코드는 불변이며 역할명·목적·부여기준·데이터 범위를 관리합니다.",
    emptyText: "역할 데이터가 없습니다.",
    columns: [
      c("role_code", "역할코드"),
      c("role_name", "역할명", true),
      c("purpose", "목적", true, "textarea"),
      c("grant_criteria", "부여기준", true, "textarea"),
      c("default_data_scope", "범위", true),
      c("use_yn", "사용여부", true, "select", useYn),
    ],
    actions: [
      {
        label: "저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "역할코드 외 관리 필드를 저장합니다.",
        path: (r) => `/api/admin/roles/${r.role_code}`,
        body: (r) => ({
          roleName: r.role_name,
          purpose: r.purpose,
          grantCriteria: r.grant_criteria,
          defaultDataScope: r.default_data_scope,
          useYn: r.use_yn,
          reason: r.reason || "역할 정보 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/user-roles",
    title: "사용자 역할 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    entity: "user_role",
    archetype: "EFFECTIVE_PERIOD_FORM",
    listPath: () => "/api/admin/user-roles",
    helper: "사용자별 역할 부여·변경·회수와 유효기간/승인자를 기록합니다.",
    emptyText: "사용자 역할 배정 데이터가 없습니다.",
    createDraft: {
      user_id: "",
      role_code: "R01",
      assignment_type: "MANUAL",
      valid_from: "",
      valid_to: "",
      status: "ACTIVE",
    },
    columns: [
      c("assignment_id", "배정ID"),
      c("user_id", "사용자", true),
      c("role_code", "역할", true, "select", roleCodes),
      c("assignment_type", "부여유형", true, "select", assignmentTypes),
      c("valid_from", "시작", true, "date"),
      c("valid_to", "종료", true, "date"),
      c("approver_id", "승인자"),
      c("status", "상태", true, "select", statusCodes),
    ],
    actions: [
      {
        label: "부여",
        method: "POST",
        confirm: "입력한 사용자에게 역할을 부여합니다.",
        path: () => "/api/admin/user-roles",
        body: (r) => ({
          userId: r.user_id,
          roleCode: r.role_code,
          assignmentType: r.assignment_type,
          validFrom: r.valid_from,
          validTo: r.valid_to,
          reason: r.reason || "역할 부여",
        }),
      },
      {
        label: "변경 저장",
        method: "PATCH",
        requiresSelection: true,
        confirm: "선택 배정의 유효기간과 상태를 저장합니다.",
        path: (r) => `/api/admin/user-roles/${r.assignment_id}`,
        body: (r) => ({
          validFrom: r.valid_from,
          validTo: r.valid_to,
          status: r.status,
          roleCode: r.role_code,
          userId: r.user_id,
          reason: r.reason || "역할 배정 변경",
        }),
      },
      {
        label: "회수",
        method: "DELETE",
        requiresSelection: true,
        confirm: "선택 역할을 REVOKED 처리합니다.",
        path: (r) => `/api/admin/user-roles/${r.assignment_id}`,
        body: () => ({}),
      },
    ],
  },
  {
    route: "/admin/menu-permissions",
    title: "메뉴 권한 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    entity: "menu_permission",
    archetype: "PERMISSION_MATRIX",
    listPath: (p) => {
      const targetType = p.get("targetType") || "ROLE";
      const targetId = p.get("targetId") || "";
      const query = new URLSearchParams({ targetType });
      if (targetId) query.set("targetId", targetId);
      return `/api/admin/menu-permissions?${query.toString()}`;
    },
    helper:
      "역할·조직·사용자 대상의 접근 허용 matrix를 화면 노출과 서버 접근통제에 동일 적용합니다.",
    emptyText: "대상에 매핑된 메뉴 권한이 없습니다.",
    columns: [
      c("targetType", "대상유형", true, "select", targetTypes),
      c("targetId", "대상", true),
      c("menuId", "메뉴ID"),
      c("menuName", "메뉴"),
      c("menuType", "유형"),
      c("url", "URL"),
      c("accessAllowed", "접근허용", true, "checkbox"),
    ],
    actions: [
      {
        label: "권한 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "현재 선택 행 기준으로 접근 허용값을 저장합니다.",
        path: () => "/api/admin/menu-permissions",
        body: (r) => ({
          targetType: r.targetType,
          targetId: r.targetId,
          items: [
            { menuId: r.menuId, accessAllowed: Boolean(r.accessAllowed) },
          ],
          reason: r.reason || "메뉴 권한 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/menu-structure",
    title: "메뉴 구조 관리",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    entity: "menu",
    archetype: "TREE_EDITOR",
    listPath: () => "/api/admin/menus/tree",
    helper: "메뉴 부모-자식 관계와 동일 계층 표시순서를 관리합니다.",
    emptyText: "메뉴 계층 데이터가 없습니다.",
    columns: [
      c("menu_id", "메뉴ID"),
      c("parent_menu_id", "부모", true, "number"),
      c("menu_type", "유형", true, "select", menuTypes),
      c("menu_name", "메뉴명"),
      c("display_order", "순서", true, "number"),
      c("url", "URL"),
    ],
    actions: [
      {
        label: "부모 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "부모 메뉴와 표시순서를 저장합니다.",
        path: (r) => `/api/admin/menus/${r.menu_id}/structure`,
        body: (r) => ({
          parentMenuId: r.parent_menu_id || null,
          displayOrder: r.display_order,
          reason: r.reason || "메뉴 구조 변경",
        }),
      },
      {
        label: "동일계층 순서 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "현재 행의 표시순서를 저장합니다.",
        path: () => "/api/admin/menus/reorder",
        body: (r) => ({
          items: [{ menuId: r.menu_id, displayOrder: r.display_order }],
          reason: r.reason || "메뉴 순서 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/menu-info",
    title: "메뉴 정보 관리",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    entity: "menu",
    archetype: "CONTENT_EDITOR",
    listPath: () => "/api/admin/menus",
    helper:
      "1차 범위 메뉴만 실행정보로 연결하고 범위 밖 업무 API는 만들지 않습니다.",
    emptyText: "메뉴 정보가 없습니다.",
    createDraft: {
      menu_name: "",
      screen_id: "",
      url: "/admin/",
      icon: "",
      business_category: "",
      description: "",
      display_order: 0,
      use_yn: "Y",
      menu_type: "SCREEN",
    },
    columns: [
      c("menu_id", "ID"),
      c("menu_name", "메뉴명", true),
      c("screen_id", "화면ID", true),
      c("url", "URL", true),
      c("icon", "아이콘", true),
      c("business_category", "업무구분", true),
      c("description", "설명", true, "textarea"),
      c("use_yn", "사용여부", true, "select", useYn),
    ],
    actions: [
      {
        label: "등록",
        method: "POST",
        confirm: "입력한 메뉴 실행정보를 등록합니다.",
        path: () => "/api/admin/menus",
        body: (r) => ({
          menuName: r.menu_name,
          screenId: r.screen_id,
          url: r.url,
          icon: r.icon,
          businessCategory: r.business_category,
          description: r.description,
          displayOrder: r.display_order || 0,
          menuType: r.menu_type || "SCREEN",
          useYn: r.use_yn,
          reason: r.reason || "메뉴 등록",
        }),
      },
      {
        label: "수정 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "선택 메뉴 실행정보를 저장합니다.",
        path: (r) => `/api/admin/menus/${r.menu_id}`,
        body: (r) => ({
          menuName: r.menu_name,
          screenId: r.screen_id,
          url: r.url,
          icon: r.icon,
          businessCategory: r.business_category,
          description: r.description,
          useYn: r.use_yn,
          reason: r.reason || "메뉴 정보 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/code-groups",
    title: "코드그룹 관리",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    entity: "code_group",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: () => "/api/admin/code-groups",
    helper:
      "그룹ID·명칭·설명·관리부서를 관리하고 상세코드 화면으로 이동합니다.",
    emptyText: "코드그룹이 없습니다.",
    createDraft: {
      group_id: "",
      group_name: "",
      description: "",
      managing_department: "",
      use_yn: "Y",
    },
    columns: [
      c("group_id", "그룹ID", true),
      c("group_name", "명칭", true),
      c("description", "설명", true, "textarea"),
      c("managing_department", "관리부서", true),
      c("use_yn", "사용여부", true, "select", useYn),
      c("created_at", "생성일시"),
      c("updated_at", "수정일시"),
    ],
    actions: [
      {
        label: "등록",
        method: "POST",
        confirm: "입력한 코드그룹을 등록합니다.",
        path: () => "/api/admin/code-groups",
        body: (r) => ({
          groupId: r.group_id,
          groupName: r.group_name,
          description: r.description,
          managingDepartment: r.managing_department,
          useYn: r.use_yn,
          reason: r.reason || "코드그룹 등록",
        }),
      },
      {
        label: "수정 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "선택 코드그룹을 저장합니다.",
        path: (r) => `/api/admin/code-groups/${r.group_id}`,
        body: (r) => ({
          groupName: r.group_name,
          description: r.description,
          managingDepartment: r.managing_department,
          useYn: r.use_yn,
          reason: r.reason || "코드그룹 변경",
        }),
      },
    ],
  },
  {
    route: "/admin/code-details",
    title: "상세코드 관리",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    entity: "code_detail",
    archetype: "TREE_EDITOR",
    listPath: (p) => {
      const groupId = p.get("groupId");
      return groupId
        ? `/api/admin/code-groups/${encodeURIComponent(groupId)}/codes`
        : "/api/admin/code-groups";
    },
    helper: "코드그룹별 코드값·코드명·정렬순서·유효기간·사용여부를 관리합니다.",
    emptyText: "선택 코드그룹에 상세코드가 없습니다.",
    createDraft: {
      group_id: "",
      code_value: "",
      code_name: "",
      parent_code_value: "",
      sort_order: 0,
      valid_from: "",
      valid_to: "",
      use_yn: "Y",
      extra_attributes: "{}",
    },
    columns: [
      c("group_id", "그룹ID"),
      c("code_value", "코드값", true),
      c("code_name", "코드명", true),
      c("parent_code_value", "상위코드", true),
      c("sort_order", "정렬", true, "number"),
      c("extra_attributes", "추가속성", true, "textarea"),
      c("valid_from", "시작", true, "date"),
      c("valid_to", "종료", true, "date"),
      c("use_yn", "사용여부", true, "select", useYn),
    ],
    actions: [
      {
        label: "등록",
        method: "POST",
        confirm: "입력한 상세코드를 등록합니다.",
        path: (r) => `/api/admin/code-groups/${r.group_id}/codes`,
        body: (r) => ({
          codeValue: r.code_value,
          codeName: r.code_name,
          parentCodeValue: r.parent_code_value || null,
          sortOrder: r.sort_order || 0,
          extraAttributes: r.extra_attributes || "{}",
          validFrom: r.valid_from || null,
          validTo: r.valid_to || null,
          useYn: r.use_yn,
          reason: r.reason || "상세코드 등록",
        }),
      },
      {
        label: "수정 저장",
        method: "PUT",
        requiresSelection: true,
        confirm: "선택 상세코드를 저장합니다.",
        path: (r) =>
          `/api/admin/code-groups/${r.group_id}/codes/${r.code_value}`,
        body: (r) => ({
          codeName: r.code_name,
          parentCodeValue: r.parent_code_value || null,
          sortOrder: r.sort_order || 0,
          extraAttributes: r.extra_attributes || "{}",
          validFrom: r.valid_from || null,
          validTo: r.valid_to || null,
          useYn: r.use_yn,
          reason: r.reason || "상세코드 변경",
        }),
      },
    ],
  },
];
function c(
  key: string,
  label: string,
  editable = false,
  kind: Column["kind"] = "text",
  options?: string[],
): Column {
  return { key, label, editable, kind, options };
}

const groups = [
  {
    title: "사용자·조직 관리",
    icon: Users,
    routes: ["/admin/users", "/admin/orgs"],
  },
  {
    title: "역할·권한 관리",
    icon: ShieldCheck,
    routes: ["/admin/roles", "/admin/user-roles", "/admin/menu-permissions"],
  },
  {
    title: "메뉴 관리",
    icon: MenuIcon,
    routes: ["/admin/menu-structure", "/admin/menu-info"],
  },
  {
    title: "공통코드 관리",
    icon: Database,
    routes: ["/admin/code-groups", "/admin/code-details"],
  },
];

export function AdminPage({
  config,
  menus,
}: {
  config: ScreenConfig;
  menus: Array<Row>;
}) {
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [draft, setDraft] = useState<Row>({});
  const [q, setQ] = useState("");
  const [state, setState] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [params] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();

  const allowedRoutes = useMemo(() => {
    const urls = menus.map((m) => String(m.url || "")).filter(Boolean);
    return urls.length
      ? new Set(urls)
      : new Set(screenConfigs.map((s) => s.route));
  }, [menus]);

  const load = async () => {
    setState("loading");
    setMessage("");
    setFieldErrors({});
    const base = config.listPath(params);
    const delimiter = base.includes("?") ? "&" : "?";
    const result = await api
      .get<Row[]>(`${base}${q ? `${delimiter}q=${encodeURIComponent(q)}` : ""}`)
      .catch((e) => ({
        success: false,
        data: null,
        error: { code: "ERROR", message: String(e) },
      }));
    if (result.error?.code === "FORBIDDEN") setState("permission");
    else if (!result.success) {
      setState("error");
      setMessage(result.error?.message || "조회 오류");
    } else {
      const data = result.data || [];
      setRows(data);
      const first = data[0] || null;
      setSelected(first);
      setDraft(
        first
          ? { ...first, reason: "" }
          : { ...(config.createDraft || {}), reason: "" },
      );
      setState(data.length ? "success" : "empty");
    }
  };

  useEffect(() => {
    load();
  }, [config.route, location.search]);
  useEffect(() => {
    setDraft(
      selected
        ? { ...selected, reason: "" }
        : { ...(config.createDraft || {}), reason: "" },
    );
  }, [selected, config.route]);

  const editableColumns = config.columns.filter((col) => col.editable);
  const selectedKey = (row: Row) =>
    String(
      row.userId ??
        row.menu_id ??
        row.group_id ??
        row.code_value ??
        row.assignment_id ??
        row.relationId ??
        JSON.stringify(row),
    );

  async function runAction(action: Action) {
    if (action.requiresSelection && !selected) {
      setMessage("선택 행이 없습니다.");
      return;
    }
    if (action.method === "POST" && !config.createDraft && !selected) {
      setMessage("등록 가능한 화면이 아닙니다.");
      return;
    }
    if (!window.confirm(action.confirm)) return;
    setMessage("");
    setFieldErrors({});
    const result = await api.send<Row>(
      action.path(draft),
      action.method,
      action.body(draft),
    );
    if (result.success) {
      setMessage("저장되었습니다. 최신 목록으로 갱신했습니다.");
      await load();
      return;
    }
    const nextErrors: Record<string, string> = {};
    result.error?.errors?.forEach((e) => {
      nextErrors[e.field] = e.reason;
    });
    setFieldErrors(nextErrors);
    setState(result.error?.code === "FORBIDDEN" ? "permission" : "error");
    setMessage(
      result.error?.errors?.[0]?.reason || result.error?.message || "저장 오류",
    );
  }

  const startCreate = () => {
    setSelected(null);
    setDraft({
      ...(config.createDraft || {}),
      group_id: params.get("groupId") || config.createDraft?.group_id,
      reason: "",
    });
    setMessage("신규 입력 모드입니다. 필수값을 입력한 뒤 등록하세요.");
  };

  return (
    <div className="admin-shell">
      <aside className="sidebar" aria-label="시스템 관리 메뉴">
        <div className="brand">
          <LayoutDashboard size={20} /> KNUE Admin
          <span>교수업적평가 공통기능</span>
        </div>
        {groups.map((group) => {
          const Icon = group.icon;
          const open = group.routes.includes(config.route);
          return (
            <nav
              className="nav-group"
              key={group.title}
              aria-label={group.title}
            >
              <p>
                <Icon size={15} /> {group.title}
                <ChevronRight
                  className={open ? "chevron open" : "chevron"}
                  size={14}
                />
              </p>
              {group.routes.map((route) => {
                const item = screenConfigs.find((s) => s.route === route)!;
                if (!allowedRoutes.has(route)) return null;
                return (
                  <NavLink
                    key={route}
                    to={route}
                    className={({ isActive }) => (isActive ? "active" : "")}
                  >
                    {item.title}
                  </NavLink>
                );
              })}
            </nav>
          );
        })}
      </aside>
      <section className="workspace">
        <header className="topbar">
          <div>
            <span>{config.menuPath}</span>
            <h1>{config.title}</h1>
            <p>{config.helper}</p>
          </div>
          <button className="secondary" onClick={() => navigate("/login")}>
            <Lock size={15} /> 로그아웃
          </button>
        </header>
        <div className="blocker">
          <AlertTriangle size={16} /> clarification_required: 실행·영속성 계약
          명확화 전 build-ready handoff 차단
        </div>
        <div className="cards">
          <Info label="화면 유형" value={config.archetype} />
          <Info label="주요 엔티티" value={config.entity} />
          <Info label="상태" value={state} />
          <Info label="조회 건수" value={String(rows.length)} />
        </div>
        <section className="panel search-panel">
          <label>
            <span>통합 검색</span>
            <input
              aria-label="검색어"
              placeholder="식별자·명칭·상태 검색"
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
          </label>
          {config.route === "/admin/menu-permissions" && <TargetFilter />}
          {config.route === "/admin/code-details" && <CodeGroupFilter />}
          <button onClick={load}>
            <Search size={15} /> 검색
          </button>
          <button className="secondary" onClick={load}>
            <RefreshCw size={15} /> 새로고침
          </button>
        </section>
        {message && (
          <div className={message.includes("저장") ? "toast" : "notice"}>
            {message}
          </div>
        )}
        {state === "loading" && <LoadingPanel />}
        {state === "empty" && (
          <section className="panel empty">
            <Database size={34} />
            <strong>{config.emptyText}</strong>
            <span>
              검색 조건을 바꾸거나 신규 등록이 가능한 화면이면 입력을
              시작하세요.
            </span>
            {config.createDraft && (
              <button onClick={startCreate}>신규 입력</button>
            )}
          </section>
        )}
        {state === "error" && (
          <section className="panel error">
            <AlertTriangle size={18} />
            {message || "서버 오류가 발생했습니다."}
          </section>
        )}
        {state === "permission" && (
          <section className="panel permission">
            <Lock size={32} />
            <strong>권한 없음</strong>
            <span>
              R09 시스템관리자 권한이 필요합니다. 401 인증 필요와 403 권한
              없음을 구분해 표시합니다.
            </span>
          </section>
        )}
        {(state === "success" || state === "empty") && (
          <section className="content-grid">
            <div className="panel table-wrap">
              <div className="table-title">
                <strong>{config.title} 목록</strong>
                {config.createDraft && (
                  <button className="secondary" onClick={startCreate}>
                    신규 입력
                  </button>
                )}
              </div>
              {rows.length ? (
                <table>
                  <thead>
                    <tr>
                      {config.columns.map((col) => (
                        <th key={col.key}>{col.label}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((row) => (
                      <tr
                        key={selectedKey(row)}
                        onClick={() => setSelected(row)}
                        className={
                          selectedKey(row) ===
                          (selected ? selectedKey(selected) : "")
                            ? "selected"
                            : ""
                        }
                      >
                        {config.columns.map((col) => (
                          <td key={col.key}>{formatValue(row[col.key])}</td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <div className="inline-empty">{config.emptyText}</div>
              )}
            </div>
            <aside className="panel detail">
              <h2>{selected ? "선택 상세" : "신규/입력 상세"}</h2>
              <div className="readonly-grid">
                {config.columns
                  .filter((col) => !col.editable)
                  .slice(0, 6)
                  .map((col) => (
                    <Readonly
                      key={col.key}
                      label={col.label}
                      value={draft[col.key]}
                    />
                  ))}
              </div>
              <div className="form-grid">
                {editableColumns.map((col) => (
                  <Field
                    key={col.key}
                    col={col}
                    value={draft[col.key]}
                    error={
                      fieldErrors[toCamel(col.key)] || fieldErrors[col.key]
                    }
                    onChange={(value) =>
                      setDraft((prev) => ({ ...prev, [col.key]: value }))
                    }
                  />
                ))}
                <label>
                  <span>변경사유</span>
                  <textarea
                    value={String(draft.reason || "")}
                    onChange={(e) =>
                      setDraft((prev) => ({ ...prev, reason: e.target.value }))
                    }
                    placeholder="감사 추적용 변경 사유"
                  />
                </label>
              </div>
              <div className="actions">
                {config.actions.map((action) => (
                  <button key={action.label} onClick={() => runAction(action)}>
                    <Save size={15} /> {action.label}
                  </button>
                ))}
                <button
                  className="secondary"
                  onClick={() =>
                    setDraft(
                      selected
                        ? { ...selected, reason: "" }
                        : { ...(config.createDraft || {}), reason: "" },
                    )
                  }
                >
                  취소
                </button>
              </div>
            </aside>
          </section>
        )}
      </section>
    </div>
  );
}
function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
function Readonly({ label, value }: { label: string; value: unknown }) {
  return (
    <div className="readonly">
      <span>{label}</span>
      <strong>{formatValue(value) || "-"}</strong>
    </div>
  );
}
function Field({
  col,
  value,
  error,
  onChange,
}: {
  col: Column;
  value: unknown;
  error?: string;
  onChange: (value: unknown) => void;
}) {
  return (
    <label>
      <span>{col.label}</span>
      {col.kind === "textarea" ? (
        <textarea
          value={String(value || "")}
          onChange={(e) => onChange(e.target.value)}
        />
      ) : col.kind === "select" ? (
        <select
          value={String(value || "")}
          onChange={(e) => onChange(e.target.value)}
        >
          {(col.options || []).map((o) => (
            <option key={o} value={o}>
              {o}
            </option>
          ))}
        </select>
      ) : col.kind === "checkbox" ? (
        <input
          type="checkbox"
          checked={Boolean(value)}
          onChange={(e) => onChange(e.target.checked)}
        />
      ) : (
        <input
          type={col.kind || "text"}
          value={String(value || "")}
          onChange={(e) => onChange(e.target.value)}
        />
      )}
      {error && <em>{error}</em>}
    </label>
  );
}
function LoadingPanel() {
  return (
    <section className="panel skeleton" aria-label="loading spinner">
      <Loader2 className="spin" size={24} />
      <div />
      <div />
      <div />
    </section>
  );
}
function TargetFilter() {
  const [params, setParams] = useSearchParams();
  return (
    <>
      <label>
        <span>대상유형</span>
        <select
          value={params.get("targetType") || "ROLE"}
          onChange={(e) =>
            setParams({
              targetType: e.target.value,
              targetId: params.get("targetId") || "",
            })
          }
        >
          {targetTypes.map((v) => (
            <option key={v}>{v}</option>
          ))}
        </select>
      </label>
      <label>
        <span>대상ID</span>
        <input
          value={params.get("targetId") || ""}
          onChange={(e) =>
            setParams({
              targetType: params.get("targetType") || "ROLE",
              targetId: e.target.value,
            })
          }
        />
      </label>
    </>
  );
}
function CodeGroupFilter() {
  const [params, setParams] = useSearchParams();
  return (
    <label>
      <span>코드그룹</span>
      <input
        value={params.get("groupId") || ""}
        onChange={(e) => setParams({ groupId: e.target.value })}
      />
    </label>
  );
}
function formatValue(value: unknown) {
  if (value === null || value === undefined) return "";
  if (typeof value === "boolean") return value ? "허용" : "차단";
  return String(value);
}
function toCamel(key: string) {
  return key.replace(/_([a-z])/g, (_, c: string) => c.toUpperCase());
}

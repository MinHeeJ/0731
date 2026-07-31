import {
  Building2,
  FileCode2,
  FolderTree,
  KeyRound,
  ListTree,
  ShieldCheck,
  Tags,
  UserCog,
  Users,
} from "lucide-react";
import { api, Row } from "./api/client";

export type FieldType =
  | "text"
  | "textarea"
  | "select"
  | "date"
  | "number"
  | "checkbox";

export type FieldConfig = {
  key: string;
  label: string;
  type?: FieldType;
  readonly?: boolean;
  options?: string[];
  help?: string;
};

export type ScreenConfig = {
  route: string;
  title: string;
  menuGroup: string;
  menuPath: string;
  archetype: string;
  operationIds: string[];
  icon: typeof Users;
  read: (params?: Record<string, string>) => Promise<Row[]>;
  save?: (draft: Row, selected?: Row) => Promise<unknown>;
  create?: (draft: Row) => Promise<unknown>;
  remove?: (draft: Row, reason?: string) => Promise<unknown>;
  columns: FieldConfig[];
  filters: FieldConfig[];
  editableFields: FieldConfig[];
  readonlyFields: FieldConfig[];
  emptyText: string;
  guidance: string;
  idField?: string;
  supportsCreate?: boolean;
  supportsMatrix?: boolean;
  defaultParams?: Record<string, string>;
};

export const roleOptions = [
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
export const useYnOptions = ["Y", "N"];

const reasonField: FieldConfig = {
  key: "reason",
  label: "변경사유",
  type: "textarea",
  help: "change_history 추적을 위해 저장 사유를 입력합니다.",
};

export const screens: ScreenConfig[] = [
  {
    route: "/admin/users",
    title: "사용자 관리",
    menuGroup: "사용자·조직 관리",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    archetype: "SEARCH_LIST_DETAIL",
    operationIds: [
      "listUsers",
      "updateUserAccount",
      "replaceUserBusinessRoles",
    ],
    icon: Users,
    read: (params = {}) => api.listUsers(params),
    save: async (draft) => {
      const userId = String(draft.userId ?? "");
      await api.updateUserAccount(userId, {
        useYn: draft.useYn,
        reason: draft.reason,
      });
      const roleCodes = String(draft.roleCodes ?? "")
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean);
      if (roleCodes.length > 0) {
        await api.replaceUserRoles(userId, { roleCodes, reason: draft.reason });
      }
    },
    columns: [
      { key: "userId", label: "교번" },
      { key: "name", label: "성명" },
      { key: "orgName", label: "소속" },
      { key: "rankName", label: "직급" },
      { key: "employmentStatus", label: "재직상태" },
      { key: "roleCodes", label: "역할" },
      { key: "useYn", label: "사용" },
      { key: "positionName", label: "보직" },
      { key: "retiredAt", label: "퇴직일자" },
      { key: "lastSyncedAt", label: "동기화" },
    ],
    filters: [
      { key: "q", label: "통합 검색" },
      { key: "roleCode", label: "역할", type: "select", options: roleOptions },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
    ],
    readonlyFields: [
      { key: "userId", label: "교번", readonly: true },
      { key: "name", label: "성명", readonly: true },
      { key: "orgName", label: "소속", readonly: true },
      { key: "rankName", label: "직급", readonly: true },
      { key: "positionName", label: "보직", readonly: true },
      { key: "retiredAt", label: "퇴직일자", readonly: true },
      { key: "lastSyncedAt", label: "최종동기화", readonly: true },
    ],
    editableFields: [
      {
        key: "useYn",
        label: "로컬 사용여부",
        type: "select",
        options: useYnOptions,
      },
      {
        key: "roleCodes",
        label: "업무 역할",
        help: "복수 역할은 쉼표로 구분합니다. 예: R01,R09",
      },
      reasonField,
    ],
    emptyText: "조건에 맞는 사용자가 없습니다.",
    guidance:
      "KORUS 원천정보는 읽기 전용이며 로컬 계정 사용여부와 업무 역할만 저장합니다.",
    idField: "userId",
  },
  {
    route: "/admin/orgs",
    title: "조직 관리",
    menuGroup: "사용자·조직 관리",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    archetype: "TREE_EDITOR",
    operationIds: [
      "listOrganizations",
      "getOrganizationTree",
      "updateOrganizationRelation",
    ],
    icon: Building2,
    read: (params = {}) => api.getOrganizationTree(params),
    save: (draft) =>
      api.updateOrganizationRelation(String(draft.relationId ?? ""), draft),
    columns: [
      { key: "orgCode", label: "조직코드" },
      { key: "orgName", label: "조직명" },
      { key: "orgType", label: "유형" },
      { key: "parentOrgCode", label: "상위조직" },
      { key: "validFrom", label: "시작일" },
      { key: "validTo", label: "종료일" },
    ],
    filters: [{ key: "q", label: "조직 검색" }],
    readonlyFields: [
      { key: "orgCode", label: "조직코드", readonly: true },
      { key: "orgName", label: "조직명", readonly: true },
      { key: "orgType", label: "조직유형", readonly: true },
    ],
    editableFields: [
      { key: "parentOrgCode", label: "상위조직" },
      { key: "validFrom", label: "적용시작", type: "date" },
      { key: "validTo", label: "적용종료", type: "date" },
      reasonField,
    ],
    emptyText: "조직 트리 데이터가 없습니다.",
    guidance:
      "선택 조직의 상위조직과 유효기간을 API로 저장합니다. 종료일은 시작일보다 빠를 수 없습니다.",
    idField: "relationId",
  },
  {
    route: "/admin/roles",
    title: "역할 관리",
    menuGroup: "역할·권한 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    archetype: "SEARCH_LIST_DETAIL",
    operationIds: ["listRoles", "updateRole"],
    icon: ShieldCheck,
    read: (params = {}) => api.listRoles(params),
    save: (draft) => api.updateRole(String(draft.roleCode ?? ""), draft),
    columns: [
      { key: "roleCode", label: "역할코드" },
      { key: "roleName", label: "역할명" },
      { key: "purpose", label: "목적" },
      { key: "grantCriteria", label: "부여기준" },
      { key: "defaultDataScope", label: "데이터범위" },
      { key: "useYn", label: "사용" },
    ],
    filters: [
      { key: "q", label: "역할 검색" },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
    ],
    readonlyFields: [{ key: "roleCode", label: "역할코드", readonly: true }],
    editableFields: [
      { key: "roleName", label: "역할명" },
      { key: "purpose", label: "목적", type: "textarea" },
      { key: "grantCriteria", label: "부여기준", type: "textarea" },
      { key: "defaultDataScope", label: "데이터범위" },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
      reasonField,
    ],
    emptyText: "역할 데이터가 없습니다.",
    guidance:
      "R01~R09 역할코드는 변경할 수 없고 역할 운영 설명 필드만 수정합니다.",
    idField: "roleCode",
  },
  {
    route: "/admin/user-roles",
    title: "사용자 역할 관리",
    menuGroup: "역할·권한 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    archetype: "EFFECTIVE_PERIOD_FORM",
    operationIds: [
      "listUserRoles",
      "grantUserRole",
      "updateUserRole",
      "revokeUserRole",
    ],
    icon: UserCog,
    read: (params = {}) => api.listUserRoles(params),
    save: (draft) =>
      api.updateUserRole(String(draft.assignmentId ?? ""), draft),
    create: (draft) => api.grantUserRole(draft),
    remove: (draft, reason) =>
      api.revokeUserRole(String(draft.assignmentId ?? ""), reason),
    columns: [
      { key: "assignmentId", label: "배정ID" },
      { key: "userId", label: "교번" },
      { key: "name", label: "성명" },
      { key: "roleCode", label: "역할" },
      { key: "assignmentType", label: "부여유형" },
      { key: "validFrom", label: "시작" },
      { key: "validTo", label: "종료" },
      { key: "approverId", label: "승인자" },
      { key: "status", label: "상태" },
    ],
    filters: [
      { key: "q", label: "사용자 검색" },
      { key: "roleCode", label: "역할", type: "select", options: roleOptions },
      {
        key: "status",
        label: "상태",
        type: "select",
        options: ["ACTIVE", "REVOKED", "EXPIRED"],
      },
    ],
    readonlyFields: [{ key: "assignmentId", label: "배정ID", readonly: true }],
    editableFields: [
      { key: "userId", label: "대상 사용자" },
      { key: "roleCode", label: "역할", type: "select", options: roleOptions },
      {
        key: "assignmentType",
        label: "부여유형",
        type: "select",
        options: ["POSITION_BASED", "MANUAL"],
      },
      { key: "validFrom", label: "유효시작", type: "date" },
      { key: "validTo", label: "유효종료", type: "date" },
      { key: "approverId", label: "승인자" },
      {
        key: "status",
        label: "상태",
        type: "select",
        options: ["ACTIVE", "REVOKED", "EXPIRED"],
      },
      reasonField,
    ],
    emptyText: "사용자 역할 배정 내역이 없습니다.",
    guidance:
      "유효기간과 승인자를 기록하며 회수는 REVOKED 상태 전이로 처리됩니다.",
    idField: "assignmentId",
    supportsCreate: true,
  },
  {
    route: "/admin/menu-permissions",
    title: "메뉴 권한 관리",
    menuGroup: "역할·권한 관리",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    archetype: "PERMISSION_MATRIX",
    operationIds: ["listMenuPermissions", "saveMenuPermissions", "getMyMenus"],
    icon: KeyRound,
    read: (params = {}) =>
      api.listMenuPermissions(
        params.targetType || "ROLE",
        params.targetId || "R09",
      ),
    save: (draft) => api.saveMenuPermissions(draft),
    columns: [
      { key: "targetType", label: "대상유형" },
      { key: "targetId", label: "대상ID" },
      { key: "menuId", label: "메뉴ID" },
      { key: "menuName", label: "메뉴명" },
      { key: "menuType", label: "유형" },
      { key: "url", label: "URL" },
      { key: "accessAllowed", label: "접근허용" },
    ],
    filters: [
      {
        key: "targetType",
        label: "대상",
        type: "select",
        options: ["ROLE", "ORG", "USER"],
      },
      { key: "targetId", label: "대상ID" },
    ],
    readonlyFields: [],
    editableFields: [reasonField],
    emptyText: "대상별 메뉴 권한 데이터가 없습니다.",
    guidance:
      "체크박스 변경 후 저장하면 sidebar 노출과 서버 접근통제에 동일 적용됩니다.",
    supportsMatrix: true,
    defaultParams: { targetType: "ROLE", targetId: "R09" },
  },
  {
    route: "/admin/menu-structure",
    title: "메뉴 구조 관리",
    menuGroup: "메뉴 관리",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    archetype: "TREE_EDITOR",
    operationIds: ["getMenuTree", "updateMenuStructure", "reorderMenus"],
    icon: FolderTree,
    read: () => api.getMenuTree(),
    save: (draft) => api.updateMenuStructure(String(draft.menuId ?? ""), draft),
    columns: [
      { key: "menuId", label: "메뉴ID" },
      { key: "parentMenuId", label: "부모ID" },
      { key: "menuType", label: "유형" },
      { key: "menuName", label: "메뉴명" },
      { key: "displayOrder", label: "순서" },
      { key: "url", label: "URL" },
    ],
    filters: [],
    readonlyFields: [{ key: "menuId", label: "메뉴ID", readonly: true }],
    editableFields: [
      { key: "parentMenuId", label: "부모메뉴ID", type: "number" },
      { key: "displayOrder", label: "표시순서", type: "number" },
      reasonField,
    ],
    emptyText: "메뉴 트리 데이터가 없습니다.",
    guidance:
      "부모-자식 관계와 동일 계층 순서를 관리합니다. 사용 중 메뉴는 삭제하지 않습니다.",
    idField: "menuId",
  },
  {
    route: "/admin/menu-info",
    title: "메뉴 정보 관리",
    menuGroup: "메뉴 관리",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    archetype: "CONTENT_EDITOR",
    operationIds: ["listMenus", "createMenu", "updateMenu"],
    icon: FileCode2,
    read: (params = {}) => api.listMenus(params),
    save: (draft) => api.updateMenu(String(draft.menuId ?? ""), draft),
    create: (draft) => api.createMenu(draft),
    columns: [
      { key: "menuId", label: "메뉴ID" },
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "icon", label: "아이콘" },
      { key: "businessCategory", label: "업무구분" },
      { key: "useYn", label: "사용" },
    ],
    filters: [{ key: "q", label: "메뉴 검색" }],
    readonlyFields: [{ key: "menuId", label: "메뉴ID", readonly: true }],
    editableFields: [
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "icon", label: "아이콘" },
      { key: "businessCategory", label: "업무구분" },
      { key: "description", label: "설명", type: "textarea" },
      { key: "displayOrder", label: "표시순서", type: "number" },
      {
        key: "menuType",
        label: "메뉴유형",
        type: "select",
        options: ["TOP", "MIDDLE", "SCREEN"],
      },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
      reasonField,
    ],
    emptyText: "메뉴 실행정보가 없습니다.",
    guidance:
      "1차 범위 메뉴의 실행정보를 조회·등록·수정합니다. 범위 밖 업무 API를 임의 생성하지 않습니다.",
    idField: "menuId",
    supportsCreate: true,
  },
  {
    route: "/admin/code-groups",
    title: "코드그룹 관리",
    menuGroup: "공통코드 관리",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    archetype: "SEARCH_LIST_DETAIL",
    operationIds: ["listCodeGroups", "createCodeGroup", "updateCodeGroup"],
    icon: Tags,
    read: (params = {}) => api.listCodeGroups(params),
    save: (draft) => api.updateCodeGroup(String(draft.groupId ?? ""), draft),
    create: (draft) => api.createCodeGroup(draft),
    columns: [
      { key: "groupId", label: "그룹ID" },
      { key: "groupName", label: "명칭" },
      { key: "description", label: "설명" },
      { key: "managingDepartment", label: "관리부서" },
      { key: "useYn", label: "사용" },
      { key: "createdAt", label: "생성일시" },
      { key: "updatedAt", label: "수정일시" },
    ],
    filters: [
      { key: "q", label: "코드그룹 검색" },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
    ],
    readonlyFields: [{ key: "groupId", label: "그룹ID", readonly: true }],
    editableFields: [
      { key: "groupId", label: "그룹ID" },
      { key: "groupName", label: "명칭" },
      { key: "managingDepartment", label: "관리부서" },
      { key: "description", label: "설명", type: "textarea" },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
      reasonField,
    ],
    emptyText: "코드그룹이 없습니다.",
    guidance:
      "그룹ID·명칭·설명·관리부서와 공통 메타필드를 표시하고 상세코드 화면으로 연결합니다.",
    idField: "groupId",
    supportsCreate: true,
  },
  {
    route: "/admin/code-details",
    title: "상세코드 관리",
    menuGroup: "공통코드 관리",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    archetype: "TREE_EDITOR",
    operationIds: ["listCodeDetails", "createCodeDetail", "updateCodeDetail"],
    icon: ListTree,
    read: (params = {}) =>
      api.listCodeDetails(params.groupId || "EVAL_AREA", params),
    save: (draft) =>
      api.updateCodeDetail(
        String(draft.groupId ?? "EVAL_AREA"),
        String(draft.codeValue ?? ""),
        draft,
      ),
    create: (draft) =>
      api.createCodeDetail(String(draft.groupId ?? "EVAL_AREA"), draft),
    columns: [
      { key: "groupId", label: "그룹ID" },
      { key: "codeValue", label: "코드값" },
      { key: "codeName", label: "코드명" },
      { key: "parentCodeValue", label: "상위코드" },
      { key: "sortOrder", label: "정렬" },
      { key: "validFrom", label: "시작" },
      { key: "validTo", label: "종료" },
      { key: "useYn", label: "사용" },
    ],
    filters: [
      { key: "groupId", label: "코드그룹" },
      { key: "q", label: "상세코드 검색" },
    ],
    readonlyFields: [
      { key: "groupId", label: "그룹ID", readonly: true },
      { key: "codeValue", label: "코드값", readonly: true },
    ],
    editableFields: [
      { key: "groupId", label: "코드그룹" },
      { key: "codeValue", label: "코드값" },
      { key: "codeName", label: "코드명" },
      { key: "parentCodeValue", label: "상위코드" },
      { key: "sortOrder", label: "정렬순서", type: "number" },
      { key: "extraAttributes", label: "추가속성 JSON", type: "textarea" },
      { key: "validFrom", label: "유효시작", type: "date" },
      { key: "validTo", label: "유효종료", type: "date" },
      {
        key: "useYn",
        label: "사용여부",
        type: "select",
        options: useYnOptions,
      },
      reasonField,
    ],
    emptyText: "상세코드가 없습니다.",
    guidance: "물리삭제 대신 사용여부 또는 유효종료일로 비활성화합니다.",
    idField: "codeValue",
    supportsCreate: true,
    defaultParams: { groupId: "EVAL_AREA" },
  },
];

export const groupedScreens = screens.reduce<Record<string, ScreenConfig[]>>(
  (acc, screen) => {
    acc[screen.menuGroup] = [...(acc[screen.menuGroup] ?? []), screen];
    return acc;
  },
  {},
);

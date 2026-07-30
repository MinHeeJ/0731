export type FieldKind = "text" | "select" | "date" | "number" | "textarea";

export type ScreenField = {
  name: string;
  label: string;
  placeholder?: string;
  kind?: FieldKind;
  options?: string[];
  required?: boolean;
  readonly?: boolean;
  helper?: string;
};

export type ScreenColumn = {
  key: string;
  label: string;
  width?: string;
  badge?: boolean;
};

export type ScreenConfig = {
  id: string;
  title: string;
  route: string;
  menuPath: string;
  menuGroup:
    | "사용자·조직 관리"
    | "역할·권한 관리"
    | "메뉴 관리"
    | "공통코드 관리";
  primaryEntity: string;
  listPath: string;
  detailPath?: (item: Record<string, unknown>) => string | null;
  saveLabel: string;
  description: string;
  searchFields: ScreenField[];
  editFields: ScreenField[];
  detailFields: ScreenField[];
  columns: ScreenColumn[];
  actions: string[];
  destructiveAction?: string;
};

const useYnOptions = ["Y", "N"];
const roleOptions = [
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
const targetTypeOptions = ["ROLE", "ORGANIZATION", "USER"];
const assignmentTypeOptions = ["POSITION_BASED", "MANUAL"];

export const screens: ScreenConfig[] = [
  {
    id: "USR-001",
    title: "사용자 관리",
    route: "/system/users",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    menuGroup: "사용자·조직 관리",
    primaryEntity: "internal_user",
    listPath: "/api/users",
    detailPath: (item) =>
      item.userId ? `/api/users/${String(item.userId)}` : null,
    saveLabel: "사용여부 저장",
    description:
      "KORUS 원천 사용자 정보를 조회하고 내부 시스템 사용여부와 업무 역할을 이력 기반으로 관리합니다.",
    searchFields: [
      { name: "employeeNo", label: "교번" },
      { name: "name", label: "성명" },
      { name: "departmentCode", label: "소속코드" },
      { name: "rankName", label: "직급" },
      { name: "employmentStatus", label: "재직상태" },
      {
        name: "roleCode",
        label: "역할",
        kind: "select",
        options: ["", ...roleOptions],
      },
      {
        name: "useYn",
        label: "사용여부",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      {
        name: "useYn",
        label: "사용여부",
        kind: "select",
        options: useYnOptions,
        required: true,
      },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
        placeholder: "사용여부 변경 사유",
      },
      {
        name: "roleCode",
        label: "업무 역할",
        kind: "select",
        options: roleOptions,
        helper: "역할 저장 시 선택 역할 1건으로 재부여합니다.",
      },
      {
        name: "assignmentType",
        label: "부여유형",
        kind: "select",
        options: assignmentTypeOptions,
      },
      { name: "approvedBy", label: "승인자 ID" },
      { name: "validFrom", label: "유효 시작일", kind: "date" },
      { name: "validTo", label: "유효 종료일", kind: "date" },
    ],
    detailFields: [
      { name: "employeeNo", label: "교번", readonly: true },
      { name: "personName", label: "성명", readonly: true },
      { name: "departmentName", label: "소속", readonly: true },
      { name: "rankName", label: "직급", readonly: true },
      { name: "employmentStatus", label: "재직상태", readonly: true },
      { name: "positionName", label: "보직", readonly: true },
      { name: "retiredAt", label: "퇴직일자", readonly: true },
      { name: "lastSyncedAt", label: "최종 동기화", readonly: true },
    ],
    columns: [
      { key: "employeeNo", label: "교번" },
      { key: "personName", label: "성명" },
      { key: "departmentName", label: "소속" },
      { key: "rankName", label: "직급" },
      { key: "employmentStatus", label: "재직" },
      { key: "roleCodes", label: "역할", badge: true },
      { key: "useYn", label: "사용", badge: true },
    ],
    actions: ["상세 조회", "사용여부 저장", "업무 역할 저장"],
  },
  {
    id: "ORG-001",
    title: "조직 관리",
    route: "/system/organizations",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    menuGroup: "사용자·조직 관리",
    primaryEntity: "organization_relation_history",
    listPath: "/api/organizations",
    saveLabel: "관계 저장",
    description:
      "조직 검색 결과와 현재 조직 계층을 함께 보며 상위조직 및 적용기간 이력을 저장합니다.",
    searchFields: [
      { name: "orgCode", label: "조직코드" },
      { name: "orgName", label: "조직명" },
      { name: "orgType", label: "유형" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      { name: "orgCode", label: "조직코드", required: true },
      { name: "parentOrgCode", label: "상위조직코드", required: true },
      {
        name: "effectiveStartDate",
        label: "적용 시작일",
        kind: "date",
        required: true,
      },
      { name: "effectiveEndDate", label: "적용 종료일", kind: "date" },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
      },
    ],
    detailFields: [
      { name: "orgCode", label: "조직코드", readonly: true },
      { name: "orgName", label: "조직명", readonly: true },
      { name: "orgType", label: "조직유형", readonly: true },
      { name: "useYn", label: "사용", readonly: true },
      { name: "parentOrgCode", label: "현재 상위조직", readonly: true },
    ],
    columns: [
      { key: "orgCode", label: "조직코드" },
      { key: "orgName", label: "조직명" },
      { key: "orgType", label: "유형", badge: true },
      { key: "useYn", label: "사용", badge: true },
      { key: "parentOrgCode", label: "상위조직" },
    ],
    actions: ["조직 검색", "계층 새로고침", "관계 저장"],
  },
  {
    id: "ROL-001",
    title: "역할 관리",
    route: "/system/roles",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    menuGroup: "역할·권한 관리",
    primaryEntity: "role",
    listPath: "/api/roles",
    saveLabel: "역할 정책 저장",
    description:
      "R01~R09 역할 목적, 부여 기준, 기본 데이터 범위를 저장합니다. 역할코드는 불변입니다.",
    searchFields: [
      {
        name: "roleCode",
        label: "역할",
        kind: "select",
        options: ["", ...roleOptions],
      },
      { name: "roleName", label: "역할명" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      {
        name: "roleCode",
        label: "역할코드",
        kind: "select",
        options: roleOptions,
        required: true,
      },
      { name: "roleName", label: "역할명", required: true },
      { name: "purpose", label: "목적", kind: "textarea", required: true },
      {
        name: "assignmentCriteria",
        label: "부여 기준",
        kind: "textarea",
        required: true,
      },
      { name: "defaultDataScope", label: "기본 데이터 범위", required: true },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: useYnOptions,
        required: true,
      },
    ],
    detailFields: [
      { name: "roleCode", label: "역할코드", readonly: true },
      { name: "roleName", label: "역할명", readonly: true },
      { name: "purpose", label: "목적", readonly: true },
      { name: "assignmentCriteria", label: "부여 기준", readonly: true },
      { name: "defaultDataScope", label: "기본 데이터 범위", readonly: true },
    ],
    columns: [
      { key: "roleCode", label: "역할", badge: true },
      { key: "roleName", label: "역할명" },
      { key: "purpose", label: "목적" },
      { key: "assignmentCriteria", label: "부여 기준" },
      { key: "defaultDataScope", label: "데이터 범위" },
      { key: "useYn", label: "사용", badge: true },
    ],
    actions: ["역할 조회", "역할 정책 저장"],
  },
  {
    id: "URO-001",
    title: "사용자 역할 관리",
    route: "/system/user-roles",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    menuGroup: "역할·권한 관리",
    primaryEntity: "user_role",
    listPath: "/api/user-roles",
    saveLabel: "역할 부여",
    destructiveAction: "선택 assignment 회수",
    description:
      "사용자별 복수 역할을 부여·변경·회수하고 승인자와 유효기간을 기록합니다.",
    searchFields: [
      { name: "userId", label: "사용자 ID" },
      {
        name: "roleCode",
        label: "역할",
        kind: "select",
        options: ["", ...roleOptions],
      },
      { name: "validOn", label: "기준일", kind: "date" },
      {
        name: "assignmentType",
        label: "부여유형",
        kind: "select",
        options: ["", ...assignmentTypeOptions],
      },
    ],
    editFields: [
      { name: "userId", label: "사용자 ID", required: true },
      {
        name: "roleCode",
        label: "역할",
        kind: "select",
        options: roleOptions,
        required: true,
      },
      {
        name: "assignmentType",
        label: "부여유형",
        kind: "select",
        options: assignmentTypeOptions,
        required: true,
      },
      { name: "approvedBy", label: "승인자 ID", required: true },
      { name: "validFrom", label: "유효 시작일", kind: "date", required: true },
      { name: "validTo", label: "유효 종료일", kind: "date" },
      {
        name: "changeReason",
        label: "회수 사유",
        kind: "textarea",
        placeholder: "회수 시 필수",
      },
    ],
    detailFields: [
      { name: "userRoleId", label: "assignmentId", readonly: true },
      { name: "userId", label: "사용자 ID", readonly: true },
      { name: "personName", label: "성명", readonly: true },
      { name: "roleCode", label: "역할", readonly: true },
      { name: "status", label: "상태", readonly: true },
    ],
    columns: [
      { key: "userRoleId", label: "assignmentId" },
      { key: "userId", label: "사용자" },
      { key: "personName", label: "성명" },
      { key: "roleCode", label: "역할", badge: true },
      { key: "assignmentType", label: "부여유형", badge: true },
      { key: "validFrom", label: "시작" },
      { key: "validTo", label: "종료" },
      { key: "status", label: "상태", badge: true },
    ],
    actions: ["사용자 역할 조회", "역할 부여", "선택 assignment 회수"],
  },
  {
    id: "MPM-001",
    title: "메뉴 권한 관리",
    route: "/system/menu-permissions",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    menuGroup: "역할·권한 관리",
    primaryEntity: "menu_permission",
    listPath: "/api/menu-permissions/matrix",
    saveLabel: "권한 저장",
    description:
      "역할·조직·사용자 대상별 메뉴 접근 허용값과 기능 권한을 서버 접근통제와 동일하게 저장합니다.",
    searchFields: [
      {
        name: "targetType",
        label: "대상 유형",
        kind: "select",
        options: targetTypeOptions,
        placeholder: "ROLE",
        required: true,
      },
      {
        name: "targetId",
        label: "대상 ID",
        placeholder: "R09",
        required: true,
      },
    ],
    editFields: [
      {
        name: "targetType",
        label: "대상 유형",
        kind: "select",
        options: targetTypeOptions,
        required: true,
      },
      { name: "targetId", label: "대상 ID", required: true },
      {
        name: "functionPermissions",
        label: "기능 권한",
        placeholder: "read,write",
      },
    ],
    detailFields: [
      { name: "menuId", label: "메뉴 ID", readonly: true },
      { name: "menuName", label: "메뉴명", readonly: true },
      { name: "screenId", label: "화면ID", readonly: true },
      { name: "url", label: "URL", readonly: true },
    ],
    columns: [
      { key: "menuId", label: "메뉴 ID" },
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "accessAllowedYn", label: "접근", badge: true },
      { key: "functionPermissions", label: "기능 권한" },
    ],
    actions: ["matrix 조회", "권한 저장"],
  },
  {
    id: "MST-001",
    title: "메뉴 구조 관리",
    route: "/system/menu-structure",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    menuGroup: "메뉴 관리",
    primaryEntity: "menu",
    listPath: "/api/menus/tree",
    saveLabel: "부모 변경 저장",
    description:
      "대/중/소메뉴 부모-자식 관계와 동일 계층 표시 순서를 3단계 제한 내에서 관리합니다.",
    searchFields: [],
    editFields: [
      { name: "menuId", label: "메뉴 ID", required: true },
      {
        name: "parentMenuId",
        label: "상위 메뉴 ID",
        helper: "최상위 메뉴는 비워둘 수 있습니다.",
      },
      {
        name: "displayOrder",
        label: "표시순서",
        kind: "number",
        required: true,
      },
      {
        name: "orderedMenuIds",
        label: "동일 계층 순서",
        placeholder: "M01,M02,M03",
      },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
      },
    ],
    detailFields: [
      { name: "menuId", label: "메뉴 ID", readonly: true },
      { name: "parentMenuId", label: "상위 메뉴", readonly: true },
      { name: "menuLevel", label: "레벨", readonly: true },
      { name: "menuName", label: "메뉴명", readonly: true },
      { name: "url", label: "URL", readonly: true },
    ],
    columns: [
      { key: "menuId", label: "메뉴 ID" },
      { key: "parentMenuId", label: "상위" },
      { key: "menuLevel", label: "레벨", badge: true },
      { key: "menuName", label: "메뉴명" },
      { key: "displayOrder", label: "순서" },
      { key: "url", label: "URL" },
    ],
    actions: ["tree 새로고침", "부모 변경 저장", "순서 저장"],
  },
  {
    id: "MIN-001",
    title: "메뉴 정보 관리",
    route: "/system/menu-info",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    menuGroup: "메뉴 관리",
    primaryEntity: "menu",
    listPath: "/api/menu-info",
    saveLabel: "메뉴 정보 저장",
    description:
      "메뉴명, 화면ID, URL, 아이콘, 업무구분, 설명과 실행 화면 연결을 저장합니다.",
    searchFields: [
      { name: "menuName", label: "메뉴명" },
      { name: "screenId", label: "화면ID" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      { name: "menuId", label: "메뉴 ID", required: true },
      { name: "menuName", label: "메뉴명", required: true },
      { name: "screenId", label: "화면ID", required: true },
      { name: "url", label: "URL", required: true, placeholder: "/system/..." },
      { name: "icon", label: "아이콘" },
      { name: "businessDomain", label: "업무구분" },
      { name: "description", label: "설명", kind: "textarea" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: useYnOptions,
        required: true,
      },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
      },
    ],
    detailFields: [
      { name: "menuId", label: "메뉴 ID", readonly: true },
      { name: "screenId", label: "화면ID", readonly: true },
      { name: "url", label: "URL", readonly: true },
      { name: "businessDomain", label: "업무구분", readonly: true },
    ],
    columns: [
      { key: "menuId", label: "메뉴 ID" },
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "businessDomain", label: "업무구분" },
      { key: "useYn", label: "사용", badge: true },
    ],
    actions: ["메뉴 정보 검색", "메뉴 정보 저장"],
  },
  {
    id: "CGP-001",
    title: "코드그룹 관리",
    route: "/system/code-groups",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    menuGroup: "공통코드 관리",
    primaryEntity: "code_group",
    listPath: "/api/code-groups",
    saveLabel: "코드그룹 저장",
    description:
      "코드그룹 ID, 명칭, 설명, 관리부서, 사용여부를 저장하고 상세코드 화면으로 이어집니다.",
    searchFields: [
      { name: "groupId", label: "그룹 ID" },
      { name: "groupName", label: "명칭" },
      { name: "managingDepartment", label: "관리부서" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      { name: "groupId", label: "그룹 ID", required: true },
      { name: "groupName", label: "명칭", required: true },
      { name: "managingDepartment", label: "관리부서", required: true },
      { name: "description", label: "설명", kind: "textarea" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: useYnOptions,
        required: true,
      },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
      },
    ],
    detailFields: [
      { name: "groupId", label: "그룹 ID", readonly: true },
      { name: "groupName", label: "명칭", readonly: true },
      { name: "managingDepartment", label: "관리부서", readonly: true },
      { name: "description", label: "설명", readonly: true },
    ],
    columns: [
      { key: "groupId", label: "그룹 ID" },
      { key: "groupName", label: "명칭" },
      { key: "description", label: "설명" },
      { key: "managingDepartment", label: "관리부서" },
      { key: "useYn", label: "사용", badge: true },
    ],
    actions: ["코드그룹 저장", "상세코드로 이동"],
  },
  {
    id: "CDT-001",
    title: "상세코드 관리",
    route: "/system/code-details",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    menuGroup: "공통코드 관리",
    primaryEntity: "code_detail",
    listPath: "/api/code-details",
    saveLabel: "상세코드 저장",
    description:
      "코드그룹별 코드값, 코드명, 상위코드, 정렬순서, 추가속성을 관리합니다.",
    searchFields: [
      {
        name: "groupId",
        label: "그룹 ID",
        placeholder: "USE_YN",
        required: true,
      },
      { name: "codeValue", label: "코드값" },
      { name: "codeName", label: "코드명" },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: ["", ...useYnOptions],
      },
    ],
    editFields: [
      { name: "groupId", label: "그룹 ID", required: true },
      { name: "codeValue", label: "코드값", required: true },
      { name: "codeName", label: "코드명", required: true },
      { name: "parentCodeValue", label: "상위코드" },
      {
        name: "displayOrder",
        label: "정렬순서",
        kind: "number",
        required: true,
      },
      {
        name: "extraAttributes",
        label: "추가속성(JSON)",
        kind: "textarea",
        placeholder: "{}",
      },
      {
        name: "useYn",
        label: "사용",
        kind: "select",
        options: useYnOptions,
        required: true,
      },
      { name: "validFrom", label: "유효 시작일", kind: "date" },
      { name: "validTo", label: "유효 종료일", kind: "date" },
      {
        name: "changeReason",
        label: "변경사유",
        kind: "textarea",
        required: true,
      },
    ],
    detailFields: [
      { name: "groupId", label: "그룹 ID", readonly: true },
      { name: "codeValue", label: "코드값", readonly: true },
      { name: "codeName", label: "코드명", readonly: true },
      { name: "parentCodeValue", label: "상위코드", readonly: true },
    ],
    columns: [
      { key: "groupId", label: "그룹 ID" },
      { key: "codeValue", label: "코드값" },
      { key: "codeName", label: "코드명" },
      { key: "parentCodeValue", label: "상위코드" },
      { key: "displayOrder", label: "정렬" },
      { key: "useYn", label: "사용", badge: true },
    ],
    actions: ["상세코드 검색", "상세코드 저장"],
  },
];

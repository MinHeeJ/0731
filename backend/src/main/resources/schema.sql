CREATE TABLE IF NOT EXISTS korus_person_snapshot (
  employee_no varchar(20) PRIMARY KEY,
  person_name varchar(100) NOT NULL,
  org_code varchar(20) NOT NULL,
  rank_name varchar(100),
  employment_status varchar(30) NOT NULL,
  position_name varchar(100),
  retired_at date,
  last_synced_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_person_snapshot IS 'KORUS 교직원 원천 정보를 조회 전용 Mock snapshot으로 보관한다. 내부 관리 화면은 이 테이블의 인사 원천 필드를 직접 수정하지 않는다.';
COMMENT ON COLUMN korus_person_snapshot.org_code IS 'organization.org_code 참조 의도 (FK 미선언: Mock snapshot 수신 순서 허용)';
COMMENT ON COLUMN korus_person_snapshot.employment_status IS 'ACTIVE:재직|LEAVE:휴직|RETIRED:퇴직';
COMMENT ON COLUMN korus_person_snapshot.last_synced_at IS 'KORUS Mock snapshot 적재 시 애플리케이션 또는 migration seed가 갱신';

CREATE TABLE IF NOT EXISTS internal_user (
  user_id varchar(64) PRIMARY KEY,
  employee_no varchar(20) NOT NULL REFERENCES korus_person_snapshot(employee_no),
  login_id varchar(100) NOT NULL UNIQUE,
  password_hash varchar(128) NOT NULL,
  system_use_yn char(1) NOT NULL CHECK (system_use_yn IN ('Y','N')),
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','INACTIVE')),
  last_login_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64)
);
COMMENT ON TABLE internal_user IS '내부 시스템 로그인 계정과 시스템 사용여부를 관리한다. KORUS 원천 인사정보와 분리된 로컬 관리 대상이다.';
COMMENT ON COLUMN internal_user.system_use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN internal_user.status IS 'ACTIVE:활성|INACTIVE:비활성';
COMMENT ON COLUMN internal_user.password_hash IS 'PasswordHashService 로그인 검증 시 저장·조회하는 단방향 해시';
COMMENT ON COLUMN internal_user.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN internal_user.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS organization (
  org_code varchar(20) PRIMARY KEY,
  org_name varchar(200) NOT NULL,
  org_type varchar(30) NOT NULL,
  display_order integer NOT NULL DEFAULT 1,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  source_type varchar(20) NOT NULL CHECK (source_type IN ('KORUS_MOCK','LOCAL')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64)
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서 조직 기준정보를 관리한다. 조직 관계 이력은 별도 테이블에서 기간별로 보존한다.';
COMMENT ON COLUMN organization.org_type IS 'UNIVERSITY:대학|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';
COMMENT ON COLUMN organization.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN organization.source_type IS 'KORUS_MOCK:KORUS모의원천|LOCAL:로컬관리';
COMMENT ON COLUMN organization.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN organization.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS organization_relation_history (
  relation_id varchar(80) PRIMARY KEY,
  org_code varchar(20) NOT NULL REFERENCES organization(org_code),
  parent_org_code varchar(20) REFERENCES organization(org_code),
  effective_start_date date NOT NULL,
  effective_end_date date,
  change_reason text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64),
  CONSTRAINT ck_org_relation_period CHECK (effective_end_date IS NULL OR effective_start_date <= effective_end_date),
  CONSTRAINT ck_org_relation_self CHECK (parent_org_code IS NULL OR org_code <> parent_org_code)
);
COMMENT ON TABLE organization_relation_history IS '조직의 상위조직 관계와 적용기간 변경 이력을 보존한다. 조직 개편 시 이전 관계를 덮어쓰지 않고 새 이력 행을 추가한다.';
COMMENT ON COLUMN organization_relation_history.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN organization_relation_history.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS role (
  role_code varchar(3) PRIMARY KEY CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09')),
  role_name varchar(100) NOT NULL,
  purpose text NOT NULL,
  assignment_criteria text NOT NULL,
  default_data_scope text NOT NULL,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64)
);
COMMENT ON TABLE role IS 'R01부터 R09까지 고정 역할코드와 역할 정책을 관리한다. 역할명은 변경 가능하지만 role_code는 불변이다.';
COMMENT ON COLUMN role.role_code IS 'R01:교원|R02:학과장|R03:단과대학(원)행정실|R04:교수지원과|R05:산학협력단|R06:입학인재관리과|R07:실적부서|R08:점수산출감사자|R09:시스템관리자';
COMMENT ON COLUMN role.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN role.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN role.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS user_role (
  user_role_id varchar(80) PRIMARY KEY,
  user_id varchar(64) NOT NULL REFERENCES internal_user(user_id),
  role_code varchar(3) NOT NULL REFERENCES role(role_code),
  assignment_type varchar(30) NOT NULL CHECK (assignment_type IN ('POSITION_BASED','MANUAL')),
  approved_by varchar(64) NOT NULL,
  valid_from date NOT NULL,
  valid_to date,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','EXPIRED','REVOKED')),
  revoked_at timestamptz,
  revoked_by varchar(64),
  change_reason text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64),
  CONSTRAINT ck_user_role_period CHECK (valid_to IS NULL OR valid_from <= valid_to),
  CONSTRAINT uq_user_role_period UNIQUE (user_id, role_code, valid_from, valid_to)
);
COMMENT ON TABLE user_role IS '사용자별 역할 부여·회수 lifecycle와 승인자 및 유효기간을 관리한다. 보직 기반 역할과 수동 역할을 구분해 기록한다.';
COMMENT ON COLUMN user_role.assignment_type IS 'POSITION_BASED:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_role.status IS 'ACTIVE:활성|EXPIRED:기간만료|REVOKED:회수';
COMMENT ON COLUMN user_role.approved_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_role.revoked_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_role.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_role.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS menu (
  menu_id varchar(80) PRIMARY KEY,
  parent_menu_id varchar(80) REFERENCES menu(menu_id),
  menu_level integer NOT NULL CHECK (menu_level BETWEEN 1 AND 3),
  menu_name varchar(200) NOT NULL,
  screen_id varchar(50) NOT NULL,
  url varchar(300) NOT NULL,
  icon varchar(100),
  business_domain varchar(100) NOT NULL,
  description text,
  display_order integer NOT NULL CHECK (display_order > 0),
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64),
  CONSTRAINT ck_menu_url CHECK (url = '#' OR url LIKE '/system/%'),
  CONSTRAINT uq_menu_sibling_order UNIQUE (parent_menu_id, display_order)
);
COMMENT ON TABLE menu IS '대메뉴·중메뉴·소메뉴 계층과 실행 화면 정보를 관리한다. 사용 중인 메뉴는 물리삭제 대신 use_yn으로 비활성화한다.';
COMMENT ON COLUMN menu.parent_menu_id IS 'menu.menu_id 참조 의도 (self FK 선언)';
COMMENT ON COLUMN menu.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN menu.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id varchar(80) PRIMARY KEY,
  target_type varchar(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
  target_id varchar(80) NOT NULL,
  menu_id varchar(80) NOT NULL REFERENCES menu(menu_id),
  access_allowed_yn char(1) NOT NULL CHECK (access_allowed_yn IN ('Y','N')),
  function_permissions jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64),
  CONSTRAINT uq_menu_permission_target UNIQUE (target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자별 메뉴 접근 허용값과 기능 권한을 관리한다. 화면 메뉴 노출과 서버 접근통제의 동일 source로 사용한다.';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.target_id IS 'role.role_code 또는 organization.org_code 또는 internal_user.user_id 참조 의도 (다형 FK 미선언)';
COMMENT ON COLUMN menu_permission.access_allowed_yn IS 'Y:허용|N:차단';
COMMENT ON COLUMN menu_permission.function_permissions IS 'AdminService.savePermissionMatrix 저장 시 요청 기능 권한 배열로 갱신';
COMMENT ON COLUMN menu_permission.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu_permission.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(80) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description text,
  managing_department varchar(100) NOT NULL,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64)
);
COMMENT ON TABLE code_group IS '공통코드 그룹의 명칭, 설명, 관리부서와 사용여부를 관리한다. 상세코드 화면 이동의 기준 엔티티다.';
COMMENT ON COLUMN code_group.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN code_group.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN code_group.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS code_detail (
  group_id varchar(80) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(80) NOT NULL,
  code_name varchar(200) NOT NULL,
  parent_code_value varchar(80),
  display_order integer NOT NULL CHECK (display_order > 0),
  extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  valid_from date,
  valid_to date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by varchar(64),
  updated_by varchar(64),
  PRIMARY KEY (group_id, code_value),
  CONSTRAINT fk_code_detail_parent FOREIGN KEY (group_id, parent_code_value) REFERENCES code_detail(group_id, code_value),
  CONSTRAINT ck_code_detail_period CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to)
);
COMMENT ON TABLE code_detail IS '코드그룹별 상세코드 값, 명칭, 상위코드, 정렬순서와 추가속성을 관리한다. 사용 중인 상세코드는 물리삭제 대신 use_yn으로 비활성화한다.';
COMMENT ON COLUMN code_detail.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN code_detail.extra_attributes IS 'AdminService.upsertCodeDetail 저장 시 요청 JSON 추가속성으로 갱신';
COMMENT ON COLUMN code_detail.created_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN code_detail.updated_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS session (
  session_id varchar(80) PRIMARY KEY,
  user_id varchar(64) NOT NULL REFERENCES internal_user(user_id),
  issued_at timestamptz NOT NULL,
  expires_at timestamptz NOT NULL,
  invalidated_at timestamptz,
  status varchar(20) NOT NULL CHECK (status IN ('AUTHENTICATED','EXPIRED'))
);
COMMENT ON TABLE session IS 'HttpOnly 세션 쿠키와 연결된 인증 세션 상태를 저장한다. 로그인·로그아웃 lifecycle 검증의 영속 source다.';
COMMENT ON COLUMN session.status IS 'AUTHENTICATED:인증됨|EXPIRED:만료';

CREATE TABLE IF NOT EXISTS change_history (
  history_id varchar(80) PRIMARY KEY,
  entity_name varchar(100) NOT NULL,
  entity_id varchar(120) NOT NULL,
  operation_type varchar(40) NOT NULL,
  before_value jsonb NOT NULL,
  after_value jsonb NOT NULL,
  changed_by varchar(64) NOT NULL,
  changed_at timestamptz NOT NULL DEFAULT now(),
  change_reason text NOT NULL
);
COMMENT ON TABLE change_history IS '관리성 등록·수정·삭제 작업의 변경 전후 값과 처리자 및 사유를 추적한다. 감사로그 UI는 현재 미사용 — 추후 연동 예정이다.';
COMMENT ON COLUMN change_history.operation_type IS 'UPDATE_USAGE:사용여부변경|REPLACE:대체|UPSERT:저장|ASSIGN:부여|REVOKE:회수|MOVE:이동|REORDER:재정렬|UPSERT_INFO:실행정보저장';
COMMENT ON COLUMN change_history.before_value IS '각 Service mutating method 성공 시 변경 전 JSON 값으로 갱신';
COMMENT ON COLUMN change_history.after_value IS '각 Service mutating method 성공 시 변경 후 JSON 값으로 갱신';
COMMENT ON COLUMN change_history.changed_by IS 'internal_user.user_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS idx_internal_user_login_id ON internal_user(login_id);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON session(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_role_user_status ON user_role(user_id, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_code_detail_group_order ON code_detail(group_id, display_order);
CREATE INDEX IF NOT EXISTS idx_org_relation_current ON organization_relation_history(org_code, effective_end_date);

INSERT INTO organization(org_code, org_name, org_type, display_order, use_yn, source_type, created_by, updated_by) VALUES
('KNUE', '한국교원대학교', 'UNIVERSITY', 1, 'Y', 'KORUS_MOCK', 'seed', 'seed'),
('COL-EDU', '교육과학대학', 'COLLEGE', 1, 'Y', 'KORUS_MOCK', 'seed', 'seed'),
('DEP-COM', '컴퓨터교육과', 'DEPARTMENT', 1, 'Y', 'KORUS_MOCK', 'seed', 'seed'),
('ADM-FA', '교수지원과', 'OFFICE', 2, 'Y', 'KORUS_MOCK', 'seed', 'seed')
ON CONFLICT (org_code) DO NOTHING;

INSERT INTO korus_person_snapshot(employee_no, person_name, org_code, rank_name, employment_status, position_name, retired_at, last_synced_at) VALUES
('100001', '관리자', 'ADM-FA', '행정주사', 'ACTIVE', '시스템관리자', NULL, now()),
('100101', '홍길동', 'DEP-COM', '교수', 'ACTIVE', '학과장', NULL, now())
ON CONFLICT (employee_no) DO NOTHING;

INSERT INTO internal_user(user_id, employee_no, login_id, password_hash, system_use_yn, status, created_by, updated_by) VALUES
('U-ADMIN', '100001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Y', 'ACTIVE', 'seed', 'seed'),
('U-FACULTY', '100101', 'faculty', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Y', 'ACTIVE', 'seed', 'seed')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO role(role_code, role_name, purpose, assignment_criteria, default_data_scope, use_yn, created_by, updated_by) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','SELF','Y','seed','seed'),
('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직자','DEPARTMENT','Y','seed','seed'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','COLLEGE','Y','seed','seed'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','ALL','Y','seed','seed'),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','RESEARCH','Y','seed','seed'),
('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','ADMISSION','Y','seed','seed'),
('R07','실적부서','담당 실적 자료 관리','실적 담당부서 사용자','DOMAIN','Y','seed','seed'),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사 담당자','AUDIT','Y','seed','seed'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 운영 관리자','ALL','Y','seed','seed')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_role(user_role_id, user_id, role_code, assignment_type, approved_by, valid_from, valid_to, status, change_reason, created_by, updated_by) VALUES
('UR-ADMIN-R09','U-ADMIN','R09','MANUAL','seed','2026-01-01',NULL,'ACTIVE','시드 관리자 권한','seed','seed'),
('UR-FACULTY-R01','U-FACULTY','R01','POSITION_BASED','seed','2026-01-01',NULL,'ACTIVE','시드 교원 권한','seed','seed')
ON CONFLICT (user_role_id) DO NOTHING;

INSERT INTO organization_relation_history(relation_id, org_code, parent_org_code, effective_start_date, effective_end_date, change_reason, created_by, updated_by) VALUES
('ORH-COL-EDU','COL-EDU','KNUE','2026-01-01',NULL,'초기 조직 관계','seed','seed'),
('ORH-DEP-COM','DEP-COM','COL-EDU','2026-01-01',NULL,'초기 조직 관계','seed','seed'),
('ORH-ADM-FA','ADM-FA','KNUE','2026-01-01',NULL,'초기 조직 관계','seed','seed')
ON CONFLICT (relation_id) DO NOTHING;

INSERT INTO menu(menu_id,parent_menu_id,menu_level,menu_name,screen_id,url,icon,business_domain,description,display_order,use_yn,created_by,updated_by) VALUES
('SYS',NULL,1,'시스템 관리','SYS','#','settings','SYSTEM','시스템 관리',1,'Y','seed','seed'),
('SYS-UO','SYS',2,'사용자·조직 관리','SYS-UO','#','users','SYSTEM','사용자·조직 관리',1,'Y','seed','seed'),
('SYS-RA','SYS',2,'역할·권한 관리','SYS-RA','#','shield','SYSTEM','역할·권한 관리',2,'Y','seed','seed'),
('SYS-MN','SYS',2,'메뉴 관리','SYS-MN','#','menu','SYSTEM','메뉴 관리',3,'Y','seed','seed'),
('SYS-CD','SYS',2,'공통코드 관리','SYS-CD','#','code','SYSTEM','공통코드 관리',4,'Y','seed','seed'),
('M-USR','SYS-UO',3,'사용자 관리','USR-001','/system/users','user','SYSTEM','사용자 관리',1,'Y','seed','seed'),
('M-ORG','SYS-UO',3,'조직 관리','ORG-001','/system/organizations','org','SYSTEM','조직 관리',2,'Y','seed','seed'),
('M-ROL','SYS-RA',3,'역할 관리','ROL-001','/system/roles','role','SYSTEM','역할 관리',1,'Y','seed','seed'),
('M-URO','SYS-RA',3,'사용자 역할 관리','URO-001','/system/user-roles','key','SYSTEM','사용자 역할 관리',2,'Y','seed','seed'),
('M-MPM','SYS-RA',3,'메뉴 권한 관리','MPM-001','/system/menu-permissions','lock','SYSTEM','메뉴 권한 관리',3,'Y','seed','seed'),
('M-MST','SYS-MN',3,'메뉴 구조 관리','MST-001','/system/menu-structure','tree','SYSTEM','메뉴 구조 관리',1,'Y','seed','seed'),
('M-MIN','SYS-MN',3,'메뉴 정보 관리','MIN-001','/system/menu-info','info','SYSTEM','메뉴 정보 관리',2,'Y','seed','seed'),
('M-CGP','SYS-CD',3,'코드그룹 관리','CGP-001','/system/code-groups','folder','SYSTEM','코드그룹 관리',1,'Y','seed','seed'),
('M-CDT','SYS-CD',3,'상세코드 관리','CDT-001','/system/code-details','list','SYSTEM','상세코드 관리',2,'Y','seed','seed')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission(permission_id,target_type,target_id,menu_id,access_allowed_yn,function_permissions,created_by,updated_by)
SELECT 'MP-R09-' || menu_id, 'ROLE', 'R09', menu_id, 'Y', '["read","write"]'::jsonb, 'seed', 'seed' FROM menu
ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

INSERT INTO code_group(group_id, group_name, description, managing_department, use_yn, created_by, updated_by) VALUES
('USE_YN','사용여부','공통 사용 여부 코드','교수지원과','Y','seed','seed'),
('EMP_STATUS','재직상태','KORUS Mock 재직 상태 코드','교수지원과','Y','seed','seed')
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO code_detail(group_id, code_value, code_name, parent_code_value, display_order, extra_attributes, use_yn, valid_from, valid_to, created_by, updated_by) VALUES
('USE_YN','Y','사용',NULL,1,'{}'::jsonb,'Y','2026-01-01',NULL,'seed','seed'),
('USE_YN','N','미사용',NULL,2,'{}'::jsonb,'Y','2026-01-01',NULL,'seed','seed'),
('EMP_STATUS','ACTIVE','재직',NULL,1,'{}'::jsonb,'Y','2026-01-01',NULL,'seed','seed'),
('EMP_STATUS','LEAVE','휴직',NULL,2,'{}'::jsonb,'Y','2026-01-01',NULL,'seed','seed'),
('EMP_STATUS','RETIRED','퇴직',NULL,3,'{}'::jsonb,'Y','2026-01-01',NULL,'seed','seed')
ON CONFLICT (group_id, code_value) DO NOTHING;

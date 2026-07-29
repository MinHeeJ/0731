CREATE TABLE IF NOT EXISTS organization (
  organization_code varchar(30) PRIMARY KEY,
  organization_name varchar(200) NOT NULL,
  organization_type varchar(30) NOT NULL CHECK (organization_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','OFFICE')),
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서의 기준 조직 정보를 관리한다.';
COMMENT ON COLUMN organization.organization_type IS 'UNIVERSITY:대학교|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';
COMMENT ON COLUMN organization.use_yn IS 'Y:사용|N:미사용';
CREATE TABLE IF NOT EXISTS korus_person_snapshot (
  person_id varchar(30) PRIMARY KEY,
  person_name varchar(100) NOT NULL,
  organization_code varchar(30) NOT NULL REFERENCES organization(organization_code),
  position_name varchar(100),
  job_grade varchar(50),
  employment_status varchar(20) NOT NULL CHECK (employment_status IN ('ACTIVE','RETIRED','LEAVE')),
  retired_at date,
  last_synced_at timestamp with time zone NOT NULL DEFAULT now(),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_person_snapshot IS 'KORUS 원천 인사 정보를 조회 전용 Mock snapshot으로 보관한다.';
COMMENT ON COLUMN korus_person_snapshot.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';
CREATE TABLE IF NOT EXISTS user_account (
  user_id varchar(50) PRIMARY KEY,
  person_id varchar(30) NOT NULL UNIQUE REFERENCES korus_person_snapshot(person_id),
  password_hash varchar(255) NOT NULL,
  system_use_yn char(1) NOT NULL CHECK (system_use_yn IN ('Y','N')),
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','DISABLED','LOCKED')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_account IS '내부 로그인 계정과 시스템 사용 여부를 관리한다.';
COMMENT ON COLUMN user_account.system_use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN user_account.status IS 'ACTIVE:활성|DISABLED:비활성|LOCKED:잠김';
CREATE TABLE IF NOT EXISTS organization_relation_history (
  relation_id bigserial PRIMARY KEY,
  organization_code varchar(30) NOT NULL REFERENCES organization(organization_code),
  parent_organization_code varchar(30) REFERENCES organization(organization_code),
  effective_start_date date NOT NULL,
  effective_end_date date,
  change_reason varchar(500) NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization_relation_history IS '조직 상하위 관계와 적용기간 이력을 보존한다.';
CREATE TABLE IF NOT EXISTS role (
  role_code varchar(3) PRIMARY KEY CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09','R10')),
  role_name varchar(100) NOT NULL,
  purpose varchar(500) NOT NULL,
  grant_criteria varchar(500) NOT NULL,
  default_data_scope varchar(100) NOT NULL CHECK (default_data_scope IN ('SELF','DEPARTMENT','COLLEGE','ALL')),
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE role IS 'R01~R10 업무 역할의 목적, 부여 기준, 기본 데이터 범위를 관리한다.';
COMMENT ON COLUMN role.role_code IS 'R01:교원|R02:학과장|R03:단과대학원행정실|R04:교수지원과|R05:산학협력단|R06:입학인재관리과|R07:실적부서|R08:점수산출감사자|R09:시스템관리자|R10:확장역할';
COMMENT ON COLUMN role.default_data_scope IS 'SELF:본인|DEPARTMENT:학과|COLLEGE:단과대학|ALL:전체';
COMMENT ON COLUMN role.use_yn IS 'Y:사용|N:미사용';
CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id bigserial PRIMARY KEY,
  user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  role_code varchar(3) NOT NULL REFERENCES role(role_code),
  grant_type varchar(20) NOT NULL CHECK (grant_type IN ('POSITION_BASED','MANUAL')),
  approver_user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  valid_from date NOT NULL,
  valid_to date,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','REVOKED','EXPIRED')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_role_assignment IS '사용자별 역할 부여·변경·회수와 승인자 및 유효기간을 기록한다.';
COMMENT ON COLUMN user_role_assignment.grant_type IS 'POSITION_BASED:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_role_assignment.status IS 'ACTIVE:활성|REVOKED:회수|EXPIRED:만료';
CREATE TABLE IF NOT EXISTS menu (
  menu_id varchar(50) PRIMARY KEY,
  parent_menu_id varchar(50) REFERENCES menu(menu_id),
  menu_level int NOT NULL,
  menu_name varchar(200) NOT NULL,
  screen_id varchar(100),
  url varchar(300),
  icon varchar(100),
  business_category varchar(100) NOT NULL,
  description varchar(1000),
  display_order int NOT NULL,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE menu IS '시스템 관리 메뉴 구조와 실행 화면 연결 정보를 관리한다.';
COMMENT ON COLUMN menu.use_yn IS 'Y:사용|N:미사용';
CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id bigserial PRIMARY KEY,
  target_type varchar(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
  target_id varchar(50) NOT NULL,
  menu_id varchar(50) NOT NULL REFERENCES menu(menu_id),
  access_yn char(1) NOT NULL CHECK (access_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  UNIQUE(target_type,target_id,menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 단위 메뉴 접근 권한을 관리한다.';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.target_id IS 'role.role_code 또는 organization.organization_code 또는 user_account.user_id 참조 의도 (다형 대상)';
COMMENT ON COLUMN menu_permission.access_yn IS 'Y:허용|N:차단';
CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(50) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description varchar(1000),
  managing_department varchar(200) NOT NULL,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_group IS '공통 상세코드를 묶는 코드그룹 기준정보를 관리한다.';
COMMENT ON COLUMN code_group.use_yn IS 'Y:사용|N:미사용';
CREATE TABLE IF NOT EXISTS common_code (
  group_id varchar(50) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(50) NOT NULL,
  code_name varchar(200) NOT NULL,
  parent_code_value varchar(50),
  sort_order int NOT NULL,
  additional_attributes jsonb,
  valid_from date NOT NULL,
  valid_to date,
  use_yn char(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  PRIMARY KEY(group_id, code_value)
);
COMMENT ON TABLE common_code IS '코드값, 코드명, 계층, 정렬순서, 유효기간과 추가속성을 관리한다.';
COMMENT ON COLUMN common_code.parent_code_value IS 'common_code.code_value 참조 의도 (동일 group_id 내 상위코드)';
COMMENT ON COLUMN common_code.additional_attributes IS 'CodeService.create/update 시 요청 JSON 추가속성으로 갱신';
COMMENT ON COLUMN common_code.use_yn IS 'Y:사용|N:미사용';
CREATE TABLE IF NOT EXISTS session (
  session_id varchar(128) PRIMARY KEY,
  user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  expires_at timestamp with time zone NOT NULL,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','EXPIRED','LOGGED_OUT')),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE session IS 'HttpOnly SameSite 세션 쿠키와 서버 세션 상태를 저장한다.';
COMMENT ON COLUMN session.status IS 'ACTIVE:활성|EXPIRED:만료|LOGGED_OUT:로그아웃';
CREATE TABLE IF NOT EXISTS change_history (
  history_id bigserial PRIMARY KEY,
  entity_name varchar(100) NOT NULL,
  entity_key varchar(200) NOT NULL,
  before_value jsonb,
  after_value jsonb,
  changed_by varchar(50) NOT NULL REFERENCES user_account(user_id),
  changed_at timestamp with time zone NOT NULL DEFAULT now(),
  change_reason varchar(500) NOT NULL
);
COMMENT ON TABLE change_history IS '등록·수정·회수성 처리의 변경 전후 값, 처리자, 처리일시, 사유를 보존한다.';
CREATE INDEX IF NOT EXISTS idx_person_org ON korus_person_snapshot(organization_code);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role_assignment(user_id);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_history_entity ON change_history(entity_name, entity_key);

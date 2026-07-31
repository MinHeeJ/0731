CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
  staff_id varchar(64) PRIMARY KEY,
  staff_no varchar(64) UNIQUE NOT NULL,
  staff_name varchar(100) NOT NULL,
  organization_code varchar(64) NOT NULL,
  position_name varchar(100),
  job_grade varchar(100),
  employment_status varchar(32) NOT NULL,
  retirement_date date,
  last_synced_at timestamptz NOT NULL DEFAULT now(),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);
COMMENT ON TABLE korus_staff_snapshot IS 'KORUS 교직원 Mock snapshot 조회 전용 기준정보.';
COMMENT ON COLUMN korus_staff_snapshot.employment_status IS 'ACTIVE:재직|LEAVE:휴직|RETIRED:퇴직';
COMMENT ON COLUMN korus_staff_snapshot.organization_code IS 'organization.organization_code 참조 의도 (FK 미선언: snapshot 원천코드 보존)';
COMMENT ON COLUMN korus_staff_snapshot.last_synced_at IS 'Mock snapshot seed 또는 동기화 adapter 실행 시 갱신';

CREATE TABLE IF NOT EXISTS user_account (
  user_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  login_id varchar(64) UNIQUE NOT NULL,
  password_hash varchar(255) NOT NULL,
  korus_staff_id varchar(64) UNIQUE REFERENCES korus_staff_snapshot(staff_id),
  system_use_enabled boolean NOT NULL DEFAULT true,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);
COMMENT ON TABLE user_account IS '내부 로그인 계정과 시스템 사용여부를 관리하는 사용자 계정.';
COMMENT ON COLUMN user_account.status IS 'ACTIVE:활성|DISABLED:비활성|LOCKED:잠김';

CREATE TABLE IF NOT EXISTS organization (
  organization_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  organization_code varchar(64) UNIQUE NOT NULL,
  organization_name varchar(200) NOT NULL,
  organization_type varchar(32) NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서 기준 조직.';
COMMENT ON COLUMN organization.status IS 'ACTIVE:사용|INACTIVE:미사용';
COMMENT ON COLUMN organization.organization_type IS 'UNIVERSITY:대학|GRADUATE:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';

CREATE TABLE IF NOT EXISTS organization_relation (
  relation_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  parent_organization_id uuid REFERENCES organization(organization_id),
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  CONSTRAINT organization_relation_date_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);
COMMENT ON TABLE organization_relation IS '조직 상하위 관계와 적용기간 이력.';

CREATE TABLE IF NOT EXISTS organization_user_assignment (
  assignment_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES user_account(user_id),
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  position_name varchar(100),
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);
COMMENT ON TABLE organization_user_assignment IS '사용자와 조직/보직의 유효기간 매핑.';

CREATE TABLE IF NOT EXISTS role (
  role_code varchar(16) PRIMARY KEY,
  role_name varchar(100) NOT NULL,
  purpose text NOT NULL,
  grant_criteria text,
  default_data_scope text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  CONSTRAINT role_code_ck CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09'))
);
COMMENT ON TABLE role IS 'R01~R09 업무 역할 정의와 부여 기준.';

CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES user_account(user_id),
  role_code varchar(16) NOT NULL REFERENCES role(role_code),
  assignment_type varchar(32) NOT NULL DEFAULT 'MANUAL',
  valid_from date NOT NULL,
  valid_to date,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  approved_by_user_id uuid NOT NULL REFERENCES user_account(user_id),
  processed_at timestamptz NOT NULL DEFAULT now(),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  CONSTRAINT user_role_status_ck CHECK (status IN ('ACTIVE','REVOKED','EXPIRED')),
  CONSTRAINT user_role_assignment_type_ck CHECK (assignment_type IN ('POSITION','MANUAL'))
);
COMMENT ON TABLE user_role_assignment IS '사용자별 역할 유효기간과 회수 상태.';
COMMENT ON COLUMN user_role_assignment.status IS 'ACTIVE:활성|REVOKED:회수|EXPIRED:만료';
COMMENT ON COLUMN user_role_assignment.assignment_type IS 'POSITION:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_role_assignment.approved_by_user_id IS 'user_account.user_id 참조: 승인자/처리자';

CREATE TABLE IF NOT EXISTS menu (
  menu_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  parent_menu_id uuid REFERENCES menu(menu_id),
  menu_name varchar(200) NOT NULL,
  menu_level varchar(32) NOT NULL,
  display_order integer NOT NULL,
  screen_id varchar(100),
  url_path varchar(255),
  icon_name varchar(100),
  business_category varchar(100),
  description text,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT menu_not_self_parent_ck CHECK (parent_menu_id IS NULL OR parent_menu_id <> menu_id)
);
COMMENT ON TABLE menu IS '시스템 관리 대·중·소 메뉴 구조와 실행 화면 정보.';
COMMENT ON COLUMN menu.menu_level IS 'TOP:대메뉴|MIDDLE:중메뉴|LEAF:소메뉴';

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  target_type varchar(32) NOT NULL,
  target_id varchar(64) NOT NULL,
  menu_id uuid NOT NULL REFERENCES menu(menu_id),
  allowed boolean NOT NULL DEFAULT false,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  CONSTRAINT menu_permission_target_ck CHECK (target_type IN ('ROLE','ORG','USER')),
  CONSTRAINT menu_permission_unique UNIQUE (target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 단위 메뉴 접근권한.';
COMMENT ON COLUMN menu_permission.status IS 'ACTIVE:활성|INACTIVE:비활성';
COMMENT ON COLUMN menu_permission.target_id IS 'role.role_code 또는 organization.organization_id 또는 user_account.user_id 참조 의도 (다형 참조)';

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(64) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description text,
  managing_department varchar(200),
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_group IS '공통 상세코드의 상위 코드그룹.';

CREATE TABLE IF NOT EXISTS code_detail (
  group_id varchar(64) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(64) NOT NULL,
  code_name varchar(200) NOT NULL,
  parent_code_value varchar(64),
  sort_order integer NOT NULL,
  extra_attributes jsonb,
  valid_from date,
  valid_to date,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (group_id, code_value),
  CONSTRAINT code_detail_not_self_parent_ck CHECK (parent_code_value IS NULL OR parent_code_value <> code_value),
  CONSTRAINT code_detail_date_ck CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)
);
COMMENT ON TABLE code_detail IS '공통 코드값·코드명·계층·유효기간 상세코드.';
COMMENT ON COLUMN code_detail.extra_attributes IS 'CodeDetail API 저장 시 애플리케이션에서 갱신하는 연계 매핑 JSON';

CREATE TABLE IF NOT EXISTS auth_session (
  session_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES user_account(user_id),
  session_token_hash varchar(255) UNIQUE NOT NULL,
  expires_at timestamptz NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE auth_session IS 'HttpOnly cookie 기반 인증 세션.';
COMMENT ON COLUMN auth_session.status IS 'ACTIVE:활성|EXPIRED:만료|LOGGED_OUT:로그아웃';

CREATE INDEX IF NOT EXISTS idx_user_account_login_id ON user_account(login_id);
CREATE INDEX IF NOT EXISTS idx_korus_staff_search ON korus_staff_snapshot(staff_no, staff_name, organization_code);
CREATE INDEX IF NOT EXISTS idx_org_code ON organization(organization_code);
CREATE INDEX IF NOT EXISTS idx_org_relation_child ON organization_relation(organization_id, is_active);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role_assignment(user_id, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id, allowed);
CREATE INDEX IF NOT EXISTS idx_code_detail_group_sort ON code_detail(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_auth_session_hash ON auth_session(session_token_hash, status);


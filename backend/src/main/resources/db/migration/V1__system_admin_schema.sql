CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS korus_user_snapshot (
  korus_user_id varchar(50) PRIMARY KEY,
  name varchar(120) NOT NULL,
  organization_id varchar(50),
  position_name varchar(120),
  loaded_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_user_snapshot IS 'KORUS 원천 사용자 조회 전용 Mock snapshot. 애플리케이션 변경 API를 제공하지 않는다.';
COMMENT ON COLUMN korus_user_snapshot.organization_id IS 'korus_organization_snapshot.organization_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS korus_organization_snapshot (
  organization_id varchar(50) PRIMARY KEY,
  name varchar(120) NOT NULL,
  parent_organization_id varchar(50),
  loaded_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_organization_snapshot IS 'KORUS 원천 조직 조회 전용 Mock snapshot. 애플리케이션 변경 API를 제공하지 않는다.';
COMMENT ON COLUMN korus_organization_snapshot.parent_organization_id IS 'korus_organization_snapshot.organization_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS local_user_account (
  local_user_account_id uuid PRIMARY KEY,
  korus_user_id varchar(50),
  name varchar(120) NOT NULL,
  use_status varchar(20) NOT NULL CHECK (use_status IN ('ENABLED','DISABLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE local_user_account IS '시스템 로그인과 사용 여부를 관리하는 로컬 사용자 계정. KORUS 원천 사용자 정보는 직접 수정하지 않는다.';
COMMENT ON COLUMN local_user_account.korus_user_id IS 'korus_user_snapshot.korus_user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN local_user_account.use_status IS 'ENABLED:사용|DISABLED:미사용';
CREATE INDEX IF NOT EXISTS idx_local_user_account_name ON local_user_account(name);

CREATE TABLE IF NOT EXISTS local_organization_setting (
  local_organization_setting_id uuid PRIMARY KEY,
  organization_id varchar(50),
  name varchar(120) NOT NULL,
  use_status varchar(20) NOT NULL CHECK (use_status IN ('ENABLED','DISABLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE local_organization_setting IS 'KORUS 조직에 대한 로컬 표시·사용 설정. 원천 조직 snapshot은 조회 전용으로 유지한다.';
COMMENT ON COLUMN local_organization_setting.organization_id IS 'korus_organization_snapshot.organization_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN local_organization_setting.use_status IS 'ENABLED:사용|DISABLED:미사용';
CREATE INDEX IF NOT EXISTS idx_local_organization_setting_name ON local_organization_setting(name);

CREATE TABLE IF NOT EXISTS role (
  role_id uuid PRIMARY KEY,
  role_code varchar(60) UNIQUE,
  name varchar(120) NOT NULL,
  description varchar(500),
  use_status varchar(20) NOT NULL CHECK (use_status IN ('ENABLED','DISABLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE role IS '시스템 관리 메뉴와 API 접근 제어에 사용하는 역할 기준정보.';
COMMENT ON COLUMN role.use_status IS 'ENABLED:사용|DISABLED:미사용';
CREATE INDEX IF NOT EXISTS idx_role_name ON role(name);

CREATE TABLE IF NOT EXISTS menu (
  menu_id uuid PRIMARY KEY,
  parent_menu_id uuid REFERENCES menu(menu_id),
  name varchar(120) NOT NULL,
  route_path varchar(200),
  description varchar(500),
  sort_order integer NOT NULL DEFAULT 0,
  display_status varchar(20) NOT NULL DEFAULT 'VISIBLE' CHECK (display_status IN ('VISIBLE','HIDDEN')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE menu IS '시스템 관리 메뉴 구조와 메뉴 정보 메타데이터. 메뉴 구조 관리와 메뉴 정보 관리 API가 공유한다.';
COMMENT ON COLUMN menu.display_status IS 'VISIBLE:표시|HIDDEN:숨김';
CREATE INDEX IF NOT EXISTS idx_menu_name ON menu(name);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON menu(parent_menu_id);

CREATE TABLE IF NOT EXISTS user_role_assignment (
  user_role_assignment_id uuid PRIMARY KEY,
  local_user_account_id uuid REFERENCES local_user_account(local_user_account_id),
  role_id uuid REFERENCES role(role_id),
  name varchar(120) NOT NULL,
  effective_status varchar(20) NOT NULL CHECK (effective_status IN ('ACTIVE','EXPIRED','REVOKED')),
  valid_from date,
  valid_to date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE user_role_assignment IS '사용자별 역할 부여, 회수, 유효기간을 관리하는 연결 정보.';
COMMENT ON COLUMN user_role_assignment.effective_status IS 'ACTIVE:유효|EXPIRED:만료|REVOKED:회수';
CREATE INDEX IF NOT EXISTS idx_user_role_assignment_name ON user_role_assignment(name);

CREATE TABLE IF NOT EXISTS menu_permission (
  menu_permission_id uuid PRIMARY KEY,
  role_id uuid REFERENCES role(role_id),
  menu_id uuid REFERENCES menu(menu_id),
  name varchar(120) NOT NULL,
  permission_action varchar(20) NOT NULL CHECK (permission_action IN ('READ','CREATE','UPDATE','DELETE')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE menu_permission IS '역할별 메뉴 접근 및 동작 권한 매트릭스.';
COMMENT ON COLUMN menu_permission.permission_action IS 'READ:조회|CREATE:등록|UPDATE:수정|DELETE:삭제';
CREATE INDEX IF NOT EXISTS idx_menu_permission_name ON menu_permission(name);

CREATE TABLE IF NOT EXISTS code_group (
  code_group_id uuid PRIMARY KEY,
  group_code varchar(60) UNIQUE,
  name varchar(120) NOT NULL,
  description varchar(500),
  use_status varchar(20) NOT NULL CHECK (use_status IN ('ENABLED','DISABLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE code_group IS '공통코드 그룹 코드, 명칭, 설명, 사용 여부 기준정보.';
COMMENT ON COLUMN code_group.use_status IS 'ENABLED:사용|DISABLED:미사용';
CREATE INDEX IF NOT EXISTS idx_code_group_name ON code_group(name);

CREATE TABLE IF NOT EXISTS detail_code (
  detail_code_id uuid PRIMARY KEY,
  code_group_id uuid REFERENCES code_group(code_group_id),
  code_value varchar(60),
  name varchar(120) NOT NULL,
  sort_order integer NOT NULL DEFAULT 0,
  use_status varchar(20) NOT NULL CHECK (use_status IN ('ENABLED','DISABLED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
COMMENT ON TABLE detail_code IS '코드그룹 하위 상세코드 값, 명칭, 정렬, 사용 여부 기준정보.';
COMMENT ON COLUMN detail_code.use_status IS 'ENABLED:사용|DISABLED:미사용';
CREATE INDEX IF NOT EXISTS idx_detail_code_name ON detail_code(name);
CREATE INDEX IF NOT EXISTS idx_detail_code_group ON detail_code(code_group_id);

CREATE TABLE IF NOT EXISTS change_history (
  change_history_id uuid PRIMARY KEY,
  target_table varchar(80) NOT NULL,
  target_id uuid NOT NULL,
  before_value jsonb,
  after_value jsonb,
  changed_by uuid REFERENCES local_user_account(local_user_account_id),
  changed_at timestamptz NOT NULL DEFAULT now(),
  change_reason varchar(500) NOT NULL
);
COMMENT ON TABLE change_history IS '등록·수정·삭제성 처리의 변경 전후 값, 처리자, 처리일시, 사유를 보존하는 변경 이력.';
COMMENT ON COLUMN change_history.target_table IS '변경 대상 table명';
COMMENT ON COLUMN change_history.before_value IS 'AdminService mutating transaction 성공 직전 값 기록';
COMMENT ON COLUMN change_history.after_value IS 'AdminService mutating transaction 성공 직후 값 기록';
CREATE INDEX IF NOT EXISTS idx_change_history_target ON change_history(target_table, target_id);

CREATE TABLE IF NOT EXISTS mapping_transaction_lock_constraints (
  mapping_transaction_lock_constraint_id uuid PRIMARY KEY,
  target_table varchar(80) NOT NULL,
  target_id uuid NOT NULL,
  operation_type varchar(20) NOT NULL CHECK (operation_type IN ('CREATE','UPDATE','DELETE')),
  mapper_technology varchar(40) NOT NULL DEFAULT 'MYBATIS_BLOCKING',
  transaction_status varchar(20) NOT NULL DEFAULT 'COMMITTED' CHECK (transaction_status IN ('COMMITTED','ROLLED_BACK')),
  korus_snapshot_read_only boolean NOT NULL DEFAULT true,
  locked_by uuid REFERENCES local_user_account(local_user_account_id),
  locked_at timestamptz NOT NULL DEFAULT now(),
  released_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE mapping_transaction_lock_constraints IS 'MyBatis blocking mapper 변경 API의 트랜잭션 경계와 KORUS snapshot 조회 전용 제약 준수 여부를 기록한다.';
COMMENT ON COLUMN mapping_transaction_lock_constraints.target_table IS '변경 대상 table명';
COMMENT ON COLUMN mapping_transaction_lock_constraints.target_id IS '각 target_table의 PK 참조 의도 (동적 테이블 참조로 FK 미선언)';
COMMENT ON COLUMN mapping_transaction_lock_constraints.operation_type IS 'CREATE:등록|UPDATE:수정|DELETE:삭제';
COMMENT ON COLUMN mapping_transaction_lock_constraints.mapper_technology IS 'AdminMapper 기반 blocking MyBatis persistence adapter 식별자';
COMMENT ON COLUMN mapping_transaction_lock_constraints.transaction_status IS 'COMMITTED:커밋됨|ROLLED_BACK:롤백됨';
COMMENT ON COLUMN mapping_transaction_lock_constraints.korus_snapshot_read_only IS 'KORUS snapshot table 변경 API 미제공 제약을 AdminService mutating transaction 완료 시 기록';
CREATE INDEX IF NOT EXISTS idx_mapping_tx_lock_target ON mapping_transaction_lock_constraints(target_table, target_id);
CREATE INDEX IF NOT EXISTS idx_mapping_tx_lock_status ON mapping_transaction_lock_constraints(transaction_status, locked_at);

INSERT INTO korus_organization_snapshot (organization_id, name, parent_organization_id) VALUES
('KORUS-ORG-ROOT', '한국교원대학교', NULL),
('KORUS-ORG-EDU', '교육학과', 'KORUS-ORG-ROOT')
ON CONFLICT (organization_id) DO NOTHING;
INSERT INTO korus_user_snapshot (korus_user_id, name, organization_id, position_name) VALUES
('KORUS-USER-ADMIN', '시드 관리자', 'KORUS-ORG-ROOT', '관리자')
ON CONFLICT (korus_user_id) DO NOTHING;
INSERT INTO local_user_account (local_user_account_id, korus_user_id, name, use_status) VALUES
('00000000-0000-0000-0000-000000000001', 'KORUS-USER-ADMIN', '시드 관리자', 'ENABLED')
ON CONFLICT (local_user_account_id) DO NOTHING;
INSERT INTO role (role_id, role_code, name, description, use_status) VALUES
('00000000-0000-0000-0000-000000000101', 'SYSTEM_ADMIN', '시스템 관리자', '1차 목표 메뉴 전체 관리 권한', 'ENABLED'),
('00000000-0000-0000-0000-000000000102', 'SYSTEM_VIEWER', '시스템 조회자', '조회 전용 권한', 'ENABLED')
ON CONFLICT (role_id) DO NOTHING;
INSERT INTO user_role_assignment (user_role_assignment_id, local_user_account_id, role_id, name, effective_status) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', '시드 관리자 - 시스템 관리자', 'ACTIVE')
ON CONFLICT (user_role_assignment_id) DO NOTHING;
INSERT INTO local_organization_setting (local_organization_setting_id, organization_id, name, use_status) VALUES
('00000000-0000-0000-0000-000000000301', 'KORUS-ORG-ROOT', '한국교원대학교', 'ENABLED')
ON CONFLICT (local_organization_setting_id) DO NOTHING;
INSERT INTO menu (menu_id, parent_menu_id, name, route_path, sort_order, display_status) VALUES
('00000000-0000-0000-0000-000000000401', NULL, '사용자 관리', '/system/users', 10, 'VISIBLE'),
('00000000-0000-0000-0000-000000000402', NULL, '조직 관리', '/system/organizations', 20, 'VISIBLE'),
('00000000-0000-0000-0000-000000000403', NULL, '역할 관리', '/system/roles', 30, 'VISIBLE'),
('00000000-0000-0000-0000-000000000404', NULL, '사용자 역할 관리', '/system/user-roles', 40, 'VISIBLE'),
('00000000-0000-0000-0000-000000000405', NULL, '메뉴 권한 관리', '/system/menu-permissions', 50, 'VISIBLE'),
('00000000-0000-0000-0000-000000000406', NULL, '메뉴 구조 관리', '/system/menu-structures', 60, 'VISIBLE'),
('00000000-0000-0000-0000-000000000407', NULL, '메뉴 정보 관리', '/system/menus', 70, 'VISIBLE'),
('00000000-0000-0000-0000-000000000408', NULL, '코드그룹 관리', '/system/code-groups', 80, 'VISIBLE'),
('00000000-0000-0000-0000-000000000409', NULL, '상세코드 관리', '/system/detail-codes', 90, 'VISIBLE')
ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO code_group (code_group_id, group_code, name, description, use_status) VALUES
('00000000-0000-0000-0000-000000000501', 'USE_STATUS', '사용 상태', '공통 사용 여부', 'ENABLED')
ON CONFLICT (code_group_id) DO NOTHING;
INSERT INTO detail_code (detail_code_id, code_group_id, code_value, name, sort_order, use_status) VALUES
('00000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000501', 'ENABLED', '사용', 1, 'ENABLED'),
('00000000-0000-0000-0000-000000000602', '00000000-0000-0000-0000-000000000501', 'DISABLED', '미사용', 2, 'ENABLED')
ON CONFLICT (detail_code_id) DO NOTHING;

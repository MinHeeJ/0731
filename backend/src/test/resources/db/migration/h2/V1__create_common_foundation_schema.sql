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

CREATE TABLE IF NOT EXISTS user_account (
  user_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
  login_id varchar(64) UNIQUE NOT NULL,
  password_hash varchar(255) NOT NULL,
  korus_staff_id varchar(64) UNIQUE REFERENCES korus_staff_snapshot(staff_id),
  system_use_enabled boolean NOT NULL DEFAULT true,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS organization (
  organization_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
  organization_code varchar(64) UNIQUE NOT NULL,
  organization_name varchar(200) NOT NULL,
  organization_type varchar(32) NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS organization_relation (
  relation_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  parent_organization_id uuid REFERENCES organization(organization_id),
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  CONSTRAINT organization_relation_date_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);

CREATE TABLE IF NOT EXISTS organization_user_assignment (
  assignment_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES user_account(user_id),
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  position_name varchar(100),
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true
);

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

CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS menu (
  menu_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(64) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description text,
  managing_department varchar(200),
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

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

CREATE TABLE IF NOT EXISTS auth_session (
  session_id uuid DEFAULT RANDOM_UUID() PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES user_account(user_id),
  session_token_hash varchar(255) UNIQUE NOT NULL,
  expires_at timestamptz NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_user_account_login_id ON user_account(login_id);
CREATE INDEX IF NOT EXISTS idx_korus_staff_search ON korus_staff_snapshot(staff_no, staff_name, organization_code);
CREATE INDEX IF NOT EXISTS idx_org_code ON organization(organization_code);
CREATE INDEX IF NOT EXISTS idx_org_relation_child ON organization_relation(organization_id, is_active);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role_assignment(user_id, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id, allowed);
CREATE INDEX IF NOT EXISTS idx_code_detail_group_sort ON code_detail(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_auth_session_hash ON auth_session(session_token_hash, status);



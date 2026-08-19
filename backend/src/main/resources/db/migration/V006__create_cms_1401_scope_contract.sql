CREATE TABLE IF NOT EXISTS c_m_s_1401 (
  scope_id varchar(50) PRIMARY KEY,
  feature_code varchar(20) NOT NULL,
  feature_name varchar(200) NOT NULL,
  screen_id varchar(100) NOT NULL,
  route_path varchar(200) NOT NULL,
  api_path varchar(200) NOT NULL,
  primary_entity varchar(100) NOT NULL,
  source_document varchar(200) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  CONSTRAINT ck_c_m_s_1401_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
  CONSTRAINT ck_c_m_s_1401_use_yn CHECK (use_yn IN ('Y', 'N')),
  CONSTRAINT uq_c_m_s_1401_route_api UNIQUE (route_path, api_path)
);
COMMENT ON TABLE c_m_s_1401 IS 'CMS-1401 공통 환경설정 scope 계약. data-model.md의 CMS-1401 heading에서 파생된 기능 범위와 화면/API/주요 엔티티 연결을 DB 기준으로 보존한다.';
COMMENT ON COLUMN c_m_s_1401.scope_id IS 'CMS-1401-SCR-COMMON-SETTING:공통환경설정화면';
COMMENT ON COLUMN c_m_s_1401.feature_code IS 'CMS-1401 기능 식별자';
COMMENT ON COLUMN c_m_s_1401.screen_id IS 'ui-design.md 화면ID 참조 의도 (FK 미선언)';
COMMENT ON COLUMN c_m_s_1401.route_path IS 'React router 경로 참조 의도 (FK 미선언)';
COMMENT ON COLUMN c_m_s_1401.api_path IS 'OpenAPI path 참조 의도 (FK 미선언)';
COMMENT ON COLUMN c_m_s_1401.primary_entity IS 'data-model.md Entity 참조 의도 (FK 미선언)';
COMMENT ON COLUMN c_m_s_1401.source_document IS '계약 산출물 문서명 참조 의도 (런타임 직접 읽기 금지)';
COMMENT ON COLUMN c_m_s_1401.status IS 'ACTIVE:활성|INACTIVE:비활성';
COMMENT ON COLUMN c_m_s_1401.use_yn IS 'Y:사용|N:미사용';
CREATE INDEX IF NOT EXISTS idx_c_m_s_1401_feature_code ON c_m_s_1401(feature_code);
CREATE INDEX IF NOT EXISTS idx_c_m_s_1401_screen_id ON c_m_s_1401(screen_id);
CREATE INDEX IF NOT EXISTS idx_c_m_s_1401_primary_entity ON c_m_s_1401(primary_entity);

INSERT INTO c_m_s_1401 (
  scope_id,
  feature_code,
  feature_name,
  screen_id,
  route_path,
  api_path,
  primary_entity,
  source_document,
  status,
  use_yn
)
VALUES (
  'CMS-1401-SCR-COMMON-SETTING',
  'CMS-1401',
  '공통 환경설정',
  'SCR-COMMON-SETTING',
  '/system/common-settings',
  '/api/system/common-settings',
  'system_common_setting',
  'data-model.md|openapi.yaml|ui-design.md',
  'ACTIVE',
  'Y'
)
ON CONFLICT (scope_id) DO UPDATE SET
  feature_name = EXCLUDED.feature_name,
  screen_id = EXCLUDED.screen_id,
  route_path = EXCLUDED.route_path,
  api_path = EXCLUDED.api_path,
  primary_entity = EXCLUDED.primary_entity,
  source_document = EXCLUDED.source_document,
  status = EXCLUDED.status,
  use_yn = EXCLUDED.use_yn,
  updated_at = now();

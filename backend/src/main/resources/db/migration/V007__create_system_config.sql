CREATE TABLE IF NOT EXISTS CMN_SYSTEM_CONFIG (
  config_id bigserial PRIMARY KEY,
  setting_key varchar(100) NOT NULL,
  setting_name varchar(200) NOT NULL,
  setting_value varchar(100) NOT NULL,
  value_type varchar(20) NOT NULL,
  unit varchar(30) NOT NULL,
  default_value varchar(100) NOT NULL,
  min_value varchar(100),
  max_value varchar(100),
  description varchar(1000) NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  updated_by varchar(50) NOT NULL DEFAULT 'system',
  UNIQUE (setting_key)
);
COMMENT ON TABLE CMN_SYSTEM_CONFIG IS '시스템 전역 공통 환경설정 항목과 현재값. 사용자별·업무별 개별 설정은 제공하지 않는다.';
COMMENT ON COLUMN CMN_SYSTEM_CONFIG.setting_key IS '환경설정 항목코드. 애플리케이션 전역 key-value 설정의 고유 식별자다.';
COMMENT ON COLUMN CMN_SYSTEM_CONFIG.setting_value IS 'SystemConfigService.updateSystemConfig 또는 bulkUpdateSystemConfigs 성공 시 애플리케이션에서 갱신하는 현재 설정값';
COMMENT ON COLUMN CMN_SYSTEM_CONFIG.value_type IS 'INTEGER:정수';
COMMENT ON COLUMN CMN_SYSTEM_CONFIG.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN CMN_SYSTEM_CONFIG.updated_by IS 'user_account.user_id 참조 의도 (FK 미선언: seed/system 변경 허용)';
CREATE INDEX IF NOT EXISTS idx_cmn_system_config_use_yn ON CMN_SYSTEM_CONFIG(use_yn);
CREATE INDEX IF NOT EXISTS idx_cmn_system_config_setting_key ON CMN_SYSTEM_CONFIG(setting_key);

CREATE TABLE IF NOT EXISTS system_common_setting (
  setting_key varchar(80) PRIMARY KEY,
  scope_type varchar(20) NOT NULL DEFAULT 'GLOBAL',
  setting_name varchar(200) NOT NULL,
  setting_value varchar(100) NOT NULL,
  value_type varchar(20) NOT NULL,
  unit_code varchar(20) NOT NULL,
  display_order integer NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  deleted_at timestamp NULL,
  change_reason varchar(500) NULL,
  CONSTRAINT ck_system_common_setting_scope_global CHECK (scope_type = 'GLOBAL'),
  CONSTRAINT ck_system_common_setting_value_type CHECK (value_type IN ('INTEGER')),
  CONSTRAINT ck_system_common_setting_unit_code CHECK (unit_code IN ('MINUTE', 'COUNT', 'DAY', 'SECOND')),
  CONSTRAINT ck_system_common_setting_use_yn CHECK (use_yn IN ('Y', 'N'))
);
COMMENT ON TABLE system_common_setting IS '전역 공통 환경설정 다섯 항목. 사용자별·업무별 개별 설정 scope는 제공하지 않는다.';
COMMENT ON COLUMN system_common_setting.scope_type IS 'GLOBAL:전역';
COMMENT ON COLUMN system_common_setting.value_type IS 'INTEGER:정수';
COMMENT ON COLUMN system_common_setting.unit_code IS 'MINUTE:분|COUNT:건|DAY:일|SECOND:초';
COMMENT ON COLUMN system_common_setting.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN system_common_setting.setting_value IS 'CommonSettingService.updateCommonSettings 저장 시 항목별 INTEGER 계약 검증 후 갱신';
CREATE INDEX IF NOT EXISTS idx_system_common_setting_display_order ON system_common_setting(display_order);
CREATE INDEX IF NOT EXISTS idx_system_common_setting_scope ON system_common_setting(scope_type);

INSERT INTO system_common_setting (setting_key, scope_type, setting_name, setting_value, value_type, unit_code, display_order, use_yn)
VALUES
  ('sessionIdleMinutes', 'GLOBAL', '세션 유휴시간', '30', 'INTEGER', 'MINUTE', 1, 'Y'),
  ('pageSize', 'GLOBAL', '페이지당 조회건수', '50', 'INTEGER', 'COUNT', 2, 'Y'),
  ('defaultSearchPeriodDays', 'GLOBAL', '기본 검색기간', '7', 'INTEGER', 'DAY', 3, 'Y'),
  ('bulkQueryThresholdCount', 'GLOBAL', '대량조회 기준건수', '1000', 'INTEGER', 'COUNT', 4, 'Y'),
  ('longRunningTaskNoticeSeconds', 'GLOBAL', '장시간작업 안내 기준', '60', 'INTEGER', 'SECOND', 5, 'Y')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO menu (menu_id,parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn)
VALUES
  ('MENU-SYSTEM-SETTING','MENU-SYSTEM','GROUP','시스템 환경설정',NULL,NULL,'sliders','system','시스템 환경설정 관리',5,'Y'),
  ('SCR-COMMON-SETTING','MENU-SYSTEM-SETTING','SCREEN','공통 환경설정','SCR-COMMON-SETTING','/system/common-settings','settings','system','세션·검색·조회 기준 공통 환경설정',1,'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (target_type,target_id,menu_id,decision,api_path_pattern,use_yn)
SELECT 'ROLE','R09',menu_id,'ALLOW','/api/system/common-settings','Y'
FROM menu
WHERE menu_id IN ('MENU-SYSTEM-SETTING', 'SCR-COMMON-SETTING')
ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

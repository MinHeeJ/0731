CREATE TABLE IF NOT EXISTS common_setting (
  setting_key varchar(80) PRIMARY KEY,
  setting_value varchar(200) NOT NULL,
  setting_unit varchar(80) NOT NULL,
  setting_meaning varchar(300) NOT NULL,
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  CONSTRAINT chk_common_setting_key CHECK (setting_key IN ('sessionIdleTime','pageSize','defaultSearchPeriod','bulkQueryThreshold','longRunningTaskNoticeThreshold'))
);

ALTER TABLE common_setting ADD COLUMN IF NOT EXISTS setting_value varchar(200) NOT NULL DEFAULT '';
ALTER TABLE common_setting ADD COLUMN IF NOT EXISTS setting_unit varchar(80) NOT NULL DEFAULT '';
ALTER TABLE common_setting ADD COLUMN IF NOT EXISTS setting_meaning varchar(300) NOT NULL DEFAULT '';
ALTER TABLE common_setting ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT now();
ALTER TABLE common_setting ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT now();

COMMENT ON TABLE common_setting IS '공통 환경설정 다섯 항목의 현재 값을 저장한다. 사용자별·업무별 개별 환경값, 세션 인증방식, 대량처리 구현방식은 저장하지 않는다.';
COMMENT ON COLUMN common_setting.setting_value IS 'CommonSettingsService.updateCommonSettings 저장 성공 시 최신 현재 값으로 갱신한다. 항목별 최소/최대 범위는 OQ-001 확인 전 DB 제약으로 확정하지 않는다.';
COMMENT ON COLUMN common_setting.setting_unit IS '화면 표시와 항목 의미 검증에 사용하는 단위 metadata. OQ-001 확인 전 enum 제약으로 고정하지 않는다.';
COMMENT ON COLUMN common_setting.setting_meaning IS '설정 항목의 업무 의미 설명. 화면 도움말과 검증 메시지의 근거로 사용한다.';

INSERT INTO common_setting (setting_key, setting_value, setting_unit, setting_meaning)
VALUES
  ('sessionIdleTime', '30', '분', '세션 유휴시간'),
  ('pageSize', '20', '건', '페이지당 조회건수'),
  ('defaultSearchPeriod', '30', '일', '기본 검색기간'),
  ('bulkQueryThreshold', '1000', '건', '대량조회 기준건수'),
  ('longRunningTaskNoticeThreshold', '10', '분', '장시간작업 안내 기준')
ON CONFLICT (setting_key) DO UPDATE SET
  setting_unit = EXCLUDED.setting_unit,
  setting_meaning = EXCLUDED.setting_meaning,
  updated_at = now();

INSERT INTO menu (menu_id, parent_menu_id, menu_type, menu_name, screen_id, url, icon, business_category, description, display_order, use_yn, created_at, updated_at)
VALUES
  ('SYS_ENV', NULL, 'GROUP', '시스템 환경설정', NULL, NULL, 'settings', 'SYSTEM', '시스템 환경설정 메뉴 그룹', 50, 'Y', now(), now()),
  ('COMMON_SETTINGS', 'SYS_ENV', 'SCREEN', '공통 환경설정', 'SCR-COMMON-SETTINGS', '/system/common-settings', 'sliders-horizontal', 'SYSTEM', '공통 환경설정 조회 및 변경 화면', 51, 'Y', now(), now())
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, parent_menu_id = EXCLUDED.parent_menu_id, screen_id = EXCLUDED.screen_id, url = EXCLUDED.url, updated_at = now();

INSERT INTO menu_permission (target_type, target_id, menu_id, decision, api_path_pattern, use_yn, created_at, updated_at)
VALUES ('ROLE', 'R09', 'COMMON_SETTINGS', 'ALLOW', '/api/system/common-settings/**', 'Y', now(), now())
ON CONFLICT (target_type, target_id, menu_id) DO UPDATE SET decision = EXCLUDED.decision, api_path_pattern = EXCLUDED.api_path_pattern, updated_at = now();

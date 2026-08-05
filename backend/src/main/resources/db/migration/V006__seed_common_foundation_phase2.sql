INSERT INTO menu (menu_id,parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn) VALUES
('MENU-SYSTEM-CONFIG','MENU-SYSTEM','GROUP','시스템 환경설정',NULL,NULL,'sliders','system','시스템 공통 환경설정',5,'Y'),
('MENU-NOTICE-HELP','MENU-SYSTEM','GROUP','공지·도움말 관리',NULL,NULL,'notice','system','공지와 도움말 관리',6,'Y'),
('SCR-POSITION','MENU-USER-ORG','SCREEN','보직 관리','SCR-POSITION','/system/positions','id-card','system','보직 관리',3,'Y'),
('SCR-COMMON-CONFIG','MENU-SYSTEM-CONFIG','SCREEN','공통 환경설정','SCR-COMMON-CONFIG','/system/common-configs','sliders','system','공통 환경설정',1,'Y'),
('SCR-NOTICE','MENU-NOTICE-HELP','SCREEN','공지사항 관리','SCR-NOTICE','/system/notices','megaphone','system','공지사항 관리',1,'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (target_type,target_id,menu_id,decision,api_path_pattern,use_yn)
VALUES
('ROLE','R09','MENU-SYSTEM-CONFIG','ALLOW','/api/common-configs*','Y'),
('ROLE','R09','MENU-NOTICE-HELP','ALLOW','/api/notices*','Y'),
('ROLE','R09','SCR-POSITION','ALLOW','/api/positions*','Y'),
('ROLE','R09','SCR-COMMON-CONFIG','ALLOW','/api/common-configs*','Y'),
('ROLE','R09','SCR-NOTICE','ALLOW','/api/notices*','Y')
ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

INSERT INTO common_config (config_key, config_value, unit, display_name, description, use_yn)
VALUES
('SESSION_IDLE_TIMEOUT_MINUTES','30','MINUTE','세션 유휴시간','관리자 세션 유휴 만료 기준(분)','Y'),
('PAGE_SIZE','20','COUNT','페이지당 조회건수','목록 화면 기본 페이지 크기(건)','Y'),
('DEFAULT_SEARCH_PERIOD_YEARS','1','YEAR','기본 검색기간','기간 검색 기본 범위(년)','Y'),
('BULK_QUERY_THRESHOLD_COUNT','1000','COUNT','대량조회 기준건수','대량조회 안내 기준(건)','Y'),
('LONG_TASK_NOTICE_THRESHOLD_SECONDS','10','SECOND','장시간작업 안내 기준','장시간 작업 안내 노출 기준(초)','Y')
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO common_position (position_code, user_id, organization_code, effective_start_date, effective_end_date, use_yn, change_reason)
VALUES ('DEPT_HEAD','USER-10002','CSE','2026-08-01','2026-12-31','Y','phase2 seed')
ON CONFLICT DO NOTHING;

INSERT INTO notice (title, content, posting_start_date, posting_end_date, target_role_code, target_organization_code, important_yn, created_by, use_yn, change_reason)
VALUES ('2차 공통기능 안내','보직 관리, 공통 환경설정, 공지사항 관리 기능이 추가되었습니다.','2026-08-01','2026-12-31','R09','CSE','Y','admin','Y','phase2 seed')
ON CONFLICT DO NOTHING;

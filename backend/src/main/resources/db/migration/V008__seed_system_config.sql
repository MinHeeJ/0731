INSERT INTO menu (menu_id,parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn) VALUES
('MENU-SYS-CONFIG','MENU-SYSTEM','GROUP','시스템 환경설정',NULL,NULL,'sliders','system','시스템 전역 환경설정 메뉴',5,'Y'),
('SCR-CMN-SYSTEM-CONFIG','MENU-SYS-CONFIG','SCREEN','공통 환경설정','SCR-CMN-SYSTEM-CONFIG','/system/config/common','settings','system','세션·조회·장시간작업 등 공통 환경설정 관리',1,'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (target_type,target_id,menu_id,decision,api_path_pattern,use_yn) VALUES
('ROLE','R09','MENU-SYS-CONFIG','ALLOW','/api/system-config*','Y'),
('ROLE','R09','SCR-CMN-SYSTEM-CONFIG','ALLOW','/api/system-config*','Y')
ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

INSERT INTO CMN_SYSTEM_CONFIG (setting_key, setting_name, setting_value, value_type, unit, default_value, min_value, max_value, description, use_yn, updated_by) VALUES
('SESSION_IDLE_TIMEOUT','세션 유휴시간','30','INTEGER','분','30','5','480','인증 세션의 유휴 허용 시간을 분 단위로 관리한다. 이번 범위에서는 저장·조회·화면 표시만 제공한다.','Y','system'),
('PAGE_SIZE','페이지당 조회건수','20','INTEGER','건','20','10','100','목록 화면에서 기본으로 표시할 페이지당 조회건수를 관리한다.','Y','system'),
('DEFAULT_SEARCH_PERIOD','기본 검색기간','30','INTEGER','일','30','7','365','검색 화면에서 기본으로 제안할 기간을 일 단위로 관리한다.','Y','system'),
('BULK_QUERY_THRESHOLD','대량조회 기준건수','1000','INTEGER','건','1000','100','10000','대량조회 안내가 필요한 기준 건수를 관리한다.','Y','system'),
('LONG_TASK_NOTICE_THRESHOLD','장시간작업 안내 기준','10','INTEGER','초','10','3','120','장시간 작업 안내를 표시할 기준 시간을 초 단위로 관리한다.','Y','system')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO org (org_code, org_name, org_type, use_yn, created_at, updated_at) VALUES
('UNIV001','한국교원대학교','UNIVERSITY','Y',now(),now()),
('COL001','사범대학','COLLEGE','Y',now(),now()),
('DEPT001','컴퓨터교육과','DEPARTMENT','Y',now(),now()),
('OFF001','교수지원과','OFFICE','Y',now(),now())
ON CONFLICT (org_code) DO NOTHING;

INSERT INTO korus_personnel_snapshot (person_id, name, org_code, position_name, rank_name, employment_status, retired_at, last_synced_at, created_at, updated_at) VALUES
('P0001','시스템 관리자','OFF001','팀장','행정사무관','ACTIVE',NULL,now(),now(),now()),
('P1001','김교원','DEPT001','학과장','교수','ACTIVE',NULL,now(),now(),now()),
('P1002','이교수','DEPT001',NULL,'부교수','ACTIVE',NULL,now(),now(),now())
ON CONFLICT (person_id) DO NOTHING;

INSERT INTO app_user (user_id, korus_person_id, password_hash, use_yn, status, created_at, updated_at) VALUES
('admin','P0001','{plain}admin','Y','ACTIVE',now(),now()),
('professor1','P1001','{plain}password','Y','ACTIVE',now(),now()),
('professor2','P1002','{plain}password','Y','ACTIVE',now(),now())
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO org_relation (org_code, parent_org_code, valid_from, valid_to, change_reason, created_at, updated_at) VALUES
('UNIV001',NULL,'2026-01-01',NULL,'초기 시드',now(),now()),
('COL001','UNIV001','2026-01-01',NULL,'초기 시드',now(),now()),
('DEPT001','COL001','2026-01-01',NULL,'초기 시드',now(),now()),
('OFF001','UNIV001','2026-01-01',NULL,'초기 시드',now(),now())
ON CONFLICT DO NOTHING;

INSERT INTO role (role_code, role_name, purpose, grant_criteria, default_data_scope, use_yn, created_at, updated_at) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','본인','Y',now(),now()),
('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직자','학과','Y',now(),now()),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','단과대학','Y',now(),now()),
('R04','교수지원과','기준정보와 평가 행정 관리','교수지원과 담당자','전체','Y',now(),now()),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','담당영역','Y',now(),now()),
('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','담당영역','Y',now(),now()),
('R07','실적부서','담당 실적 자료 관리','실적 부서 담당자','담당영역','Y',now(),now()),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사 담당자','전체조회','Y',now(),now()),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자','전체','Y',now(),now())
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_role (user_id, role_code, assignment_type, valid_from, valid_to, approver_id, status, created_at, updated_at) VALUES
('admin','R09','MANUAL','2026-01-01',NULL,'admin','ACTIVE',now(),now()),
('professor1','R01','POSITION_BASED','2026-01-01',NULL,'admin','ACTIVE',now(),now()),
('professor1','R02','POSITION_BASED','2026-01-01',NULL,'admin','ACTIVE',now(),now()),
('professor2','R01','POSITION_BASED','2026-01-01',NULL,'admin','ACTIVE',now(),now())
ON CONFLICT DO NOTHING;

INSERT INTO menu (menu_id, parent_menu_id, menu_type, menu_name, screen_id, url, icon, business_category, description, display_order, use_yn, created_at, updated_at) OVERRIDING SYSTEM VALUE VALUES
(1,NULL,'TOP','시스템 관리',NULL,NULL,'settings','COMMON','공통 시스템 관리 영역',10,'Y',now(),now()),
(10,1,'MIDDLE','사용자·조직 관리',NULL,NULL,'users','COMMON','사용자와 조직 기준정보 관리',10,'Y',now(),now()),
(11,10,'SCREEN','사용자 관리','SCR-USERS','/admin/users','user','COMMON','사용자 검색과 로컬 계정 관리',11,'Y',now(),now()),
(12,10,'SCREEN','조직 관리','SCR-ORGS','/admin/orgs','org','COMMON','조직 트리와 관계 기간 관리',12,'Y',now(),now()),
(20,1,'MIDDLE','역할·권한 관리',NULL,NULL,'shield','COMMON','역할과 메뉴 권한 관리',20,'Y',now(),now()),
(21,20,'SCREEN','역할 관리','SCR-ROLES','/admin/roles','role','COMMON','R01~R09 역할 기준 관리',21,'Y',now(),now()),
(22,20,'SCREEN','사용자 역할 관리','SCR-USER-ROLES','/admin/user-roles','user-role','COMMON','사용자별 역할 유효기간 관리',22,'Y',now(),now()),
(23,20,'SCREEN','메뉴 권한 관리','SCR-MENU-PERMISSIONS','/admin/menu-permissions','permission','COMMON','메뉴 접근 matrix 관리',23,'Y',now(),now()),
(30,1,'MIDDLE','메뉴 관리',NULL,NULL,'menu','COMMON','메뉴 계층과 실행정보 관리',30,'Y',now(),now()),
(31,30,'SCREEN','메뉴 구조 관리','SCR-MENU-STRUCTURE','/admin/menu-structure','tree','COMMON','메뉴 부모와 순서 관리',31,'Y',now(),now()),
(32,30,'SCREEN','메뉴 정보 관리','SCR-MENU-INFO','/admin/menu-info','info','COMMON','메뉴 실행정보 관리',32,'Y',now(),now()),
(40,1,'MIDDLE','공통코드 관리',NULL,NULL,'code','COMMON','코드그룹과 상세코드 관리',40,'Y',now(),now()),
(41,40,'SCREEN','코드그룹 관리','SCR-CODE-GROUPS','/admin/code-groups','code-group','COMMON','코드그룹 관리',41,'Y',now(),now()),
(42,40,'SCREEN','상세코드 관리','SCR-CODE-DETAILS','/admin/code-details','code-detail','COMMON','상세코드 관리',42,'Y',now(),now())
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (target_type, target_id, menu_id, access_allowed, created_at, updated_at)
SELECT 'ROLE','R09', menu_id, true, now(), now() FROM menu
ON CONFLICT (target_type, target_id, menu_id) DO NOTHING;

INSERT INTO menu_permission (target_type, target_id, menu_id, access_allowed, created_at, updated_at)
SELECT 'ROLE','R01', menu_id, false, now(), now() FROM menu WHERE menu_type = 'SCREEN'
ON CONFLICT (target_type, target_id, menu_id) DO NOTHING;

INSERT INTO code_group (group_id, group_name, description, managing_department, use_yn, created_at, updated_at) VALUES
('EVAL_AREA','평가영역','교수업적평가 영역 구분','교수지원과','Y',now(),now()),
('PROCESS_STATUS','처리상태','공통 처리 상태 코드','교수지원과','Y',now(),now()),
('AUTH_TYPE','인증구분','로컬·SSO 인증 구분','정보전산원','Y',now(),now())
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO code_detail (group_id, code_value, code_name, parent_code_value, sort_order, extra_attributes, valid_from, valid_to, use_yn, created_at, updated_at) VALUES
('EVAL_AREA','TEACHING','교육',NULL,10,'{}','2026-01-01',NULL,'Y',now(),now()),
('EVAL_AREA','RESEARCH','연구',NULL,20,'{}','2026-01-01',NULL,'Y',now(),now()),
('EVAL_AREA','SERVICE','봉사',NULL,30,'{}','2026-01-01',NULL,'Y',now(),now()),
('PROCESS_STATUS','ACTIVE','활성',NULL,10,'{}','2026-01-01',NULL,'Y',now(),now()),
('PROCESS_STATUS','INACTIVE','비활성',NULL,20,'{}','2026-01-01',NULL,'Y',now(),now()),
('AUTH_TYPE','LOCAL','로컬',NULL,10,'{}','2026-01-01',NULL,'Y',now(),now())
ON CONFLICT (group_id, code_value) DO NOTHING;

SELECT setval(pg_get_serial_sequence('menu','menu_id'), 100, true);

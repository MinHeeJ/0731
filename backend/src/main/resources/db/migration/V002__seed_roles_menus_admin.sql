INSERT INTO organization (organization_code, organization_name, organization_type, source_system, use_yn, status) VALUES ('KNUE','한국교원대학교','UNIVERSITY','KORUS_MOCK','Y','ACTIVE') ON CONFLICT (organization_code) DO NOTHING;
INSERT INTO korus_staff_snapshot (staff_no, staff_name, organization_code, position_name, job_title, employment_status, retirement_date, last_synced_at, use_yn) VALUES ('10001','관리자','KNUE','시스템관리자','교수','ACTIVE',NULL,now(),'Y') ON CONFLICT (staff_no) DO NOTHING;
INSERT INTO user_account (user_id, login_id, password_hash, staff_no, display_name, use_yn, status) VALUES ('admin','admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','10001','시스템관리자','Y','ACTIVE') ON CONFLICT (user_id) DO NOTHING;
INSERT INTO role (role_code, role_name, purpose, assignment_criteria, default_data_scope, use_yn) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','본인','Y'),('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직','학과','Y'),('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','대학','Y'),('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','전체','Y'),('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','전체','Y'),('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','전체','Y'),('R07','실적부서','담당 실적 자료 관리','실적부서 담당자','담당부서','Y'),('R08','점수산출 감사자','산출 과정과 근거 조회','감사자','전체조회','Y'),('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템관리자','전체','Y') ON CONFLICT (role_code) DO NOTHING;
INSERT INTO user_role (user_id, role_code, assignment_type, approved_by, effective_start_date, status, use_yn) VALUES ('admin','R09','MANUAL','admin',CURRENT_DATE,'ACTIVE','Y') ON CONFLICT DO NOTHING;
INSERT INTO menu (menu_id,parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn) VALUES
('MENU-SYSTEM',NULL,'ROOT','시스템 관리',NULL,NULL,'settings','system','시스템 관리 대메뉴',1,'Y'),
('MENU-USER-ORG','MENU-SYSTEM','GROUP','사용자·조직 관리',NULL,NULL,'users','system','사용자와 조직 관리',1,'Y'),
('MENU-AUTH','MENU-SYSTEM','GROUP','역할·권한 관리',NULL,NULL,'shield','system','역할과 권한 관리',2,'Y'),
('MENU-MENU','MENU-SYSTEM','GROUP','메뉴 관리',NULL,NULL,'menu','system','메뉴 구조와 실행정보 관리',3,'Y'),
('MENU-CODE','MENU-SYSTEM','GROUP','공통코드 관리',NULL,NULL,'code','system','공통코드 관리',4,'Y'),
('SCR-USER','MENU-USER-ORG','SCREEN','사용자 관리','SCR-USER','/system/users','user','system','사용자 관리',1,'Y'),
('SCR-ORG','MENU-USER-ORG','SCREEN','조직 관리','SCR-ORG','/system/organizations','building','system','조직 관리',2,'Y'),
('SCR-ROLE','MENU-AUTH','SCREEN','역할 관리','SCR-ROLE','/system/roles','key','system','역할 관리',1,'Y'),
('SCR-USER-ROLE','MENU-AUTH','SCREEN','사용자 역할 관리','SCR-USER-ROLE','/system/user-roles','user-check','system','사용자 역할 관리',2,'Y'),
('SCR-MENU-PERM','MENU-AUTH','SCREEN','메뉴 권한 관리','SCR-MENU-PERM','/system/menu-permissions','lock','system','메뉴 권한 관리',3,'Y'),
('SCR-MENU-TREE','MENU-MENU','SCREEN','메뉴 구조 관리','SCR-MENU-TREE','/system/menu-structure','tree','system','메뉴 구조 관리',1,'Y'),
('SCR-MENU-INFO','MENU-MENU','SCREEN','메뉴 정보 관리','SCR-MENU-INFO','/system/menus','list','system','메뉴 정보 관리',2,'Y'),
('SCR-CODE-GROUP','MENU-CODE','SCREEN','코드그룹 관리','SCR-CODE-GROUP','/system/code-groups','folder','system','코드그룹 관리',1,'Y'),
('SCR-DETAIL-CODE','MENU-CODE','SCREEN','상세코드 관리','SCR-DETAIL-CODE','/system/code-groups/EVAL_AREA/detail-codes','tags','system','상세코드 관리',2,'Y') ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO menu_permission (target_type,target_id,menu_id,decision,api_path_pattern,use_yn) SELECT 'ROLE','R09',menu_id,'ALLOW','/api/*','Y' FROM menu ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

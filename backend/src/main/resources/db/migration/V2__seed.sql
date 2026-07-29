INSERT INTO organization(organization_code,organization_name,organization_type,use_yn) VALUES
('KNUE','한국교원대학교','UNIVERSITY','Y'),('COLL_EDU','교육과학대학','COLLEGE','Y'),('DEPT_COMEDU','컴퓨터교육과','DEPARTMENT','Y'),('OFFICE_FAC','교수지원과','OFFICE','Y') ON CONFLICT DO NOTHING;
INSERT INTO korus_person_snapshot(person_id,person_name,organization_code,position_name,job_grade,employment_status,retired_at,last_synced_at) VALUES
('P_ADMIN','관리자','OFFICE_FAC','시스템관리자','행정','ACTIVE',NULL,now()),('P1001','김교원','DEPT_COMEDU','교수','정교수','ACTIVE',NULL,now()),('P1002','이학과장','DEPT_COMEDU','학과장','정교수','ACTIVE',NULL,now()) ON CONFLICT DO NOTHING;
INSERT INTO user_account(user_id,person_id,password_hash,system_use_yn,status) VALUES
('admin','P_ADMIN','admin','Y','ACTIVE'),('prof1001','P1001','admin','Y','ACTIVE'),('chair1002','P1002','admin','Y','ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO role(role_code,role_name,purpose,grant_criteria,default_data_scope,use_yn) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','SELF','Y'),('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직자','DEPARTMENT','Y'),('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','COLLEGE','Y'),('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','ALL','Y'),('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','ALL','Y'),('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','ALL','Y'),('R07','실적부서','담당 실적 자료 관리','실적 담당 부서','ALL','Y'),('R08','점수산출 감사자','산출 과정과 근거 조회','감사자 지정','ALL','Y'),('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자 지정','ALL','Y') ON CONFLICT DO NOTHING;
INSERT INTO user_role_assignment(user_id,role_code,grant_type,approver_user_id,valid_from,status) VALUES
('admin','R09','MANUAL','admin','2026-01-01','ACTIVE'),('prof1001','R01','POSITION_BASED','admin','2026-01-01','ACTIVE'),('chair1002','R02','POSITION_BASED','admin','2026-01-01','ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO organization_relation_history(organization_code,parent_organization_code,effective_start_date,change_reason) VALUES
('COLL_EDU','KNUE','2026-01-01','시드 조직 관계'),('DEPT_COMEDU','COLL_EDU','2026-01-01','시드 조직 관계'),('OFFICE_FAC','KNUE','2026-01-01','시드 조직 관계') ON CONFLICT DO NOTHING;
INSERT INTO menu(menu_id,parent_menu_id,menu_level,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn) VALUES
('SYS',NULL,1,'시스템 관리','SCR-ADMIN-SHELL','/admin','settings','시스템 관리','공통기능 대시보드',1,'Y'),
('SYS_USER_ORG','SYS',2,'사용자·조직 관리',NULL,NULL,'users','시스템 관리','사용자와 조직 관리',1,'Y'),
('MENU_USERS','SYS_USER_ORG',3,'사용자 관리','SCR-USERS','/admin/users','user','시스템 관리','사용자 검색 및 역할 관리',1,'Y'),
('MENU_ORGS','SYS_USER_ORG',3,'조직 관리','SCR-ORGS','/admin/organizations','building','시스템 관리','조직 관계 관리',2,'Y'),
('SYS_AUTH','SYS',2,'역할·권한 관리',NULL,NULL,'shield','시스템 관리','역할과 권한 관리',2,'Y'),
('MENU_ROLES','SYS_AUTH',3,'역할 관리','SCR-ROLES','/admin/roles','key','시스템 관리','역할 기준정보 관리',1,'Y'),
('MENU_USER_ROLES','SYS_AUTH',3,'사용자 역할 관리','SCR-USER-ROLES','/admin/user-roles','badge','시스템 관리','사용자 역할 유효기간 관리',2,'Y'),
('MENU_PERMS','SYS_AUTH',3,'메뉴 권한 관리','SCR-MENU-PERM','/admin/menu-permissions','lock','시스템 관리','메뉴 접근권한 관리',3,'Y'),
('SYS_MENU','SYS',2,'메뉴 관리',NULL,NULL,'menu','시스템 관리','메뉴 구조와 정보 관리',3,'Y'),
('MENU_TREE','SYS_MENU',3,'메뉴 구조 관리','SCR-MENU-TREE','/admin/menus/tree','tree','시스템 관리','메뉴 순서 관리',1,'Y'),
('MENU_INFO','SYS_MENU',3,'메뉴 정보 관리','SCR-MENU-INFO','/admin/menus','edit','시스템 관리','메뉴 실행정보 관리',2,'Y'),
('SYS_CODE','SYS',2,'공통코드 관리',NULL,NULL,'code','시스템 관리','공통코드 관리',4,'Y'),
('MENU_CODE_GROUPS','SYS_CODE',3,'코드그룹 관리','SCR-CODE-GROUPS','/admin/code-groups','folder','시스템 관리','코드그룹 관리',1,'Y'),
('MENU_CODES','SYS_CODE',3,'상세코드 관리','SCR-CODES','/admin/code-groups/USER_STATUS/codes','list','시스템 관리','상세코드 관리',2,'Y') ON CONFLICT DO NOTHING;
INSERT INTO menu_permission(target_type,target_id,menu_id,access_yn) SELECT 'ROLE','R09',menu_id,'Y' FROM menu ON CONFLICT DO NOTHING;
INSERT INTO code_group(group_id,group_name,description,managing_department,use_yn) VALUES
('USER_STATUS','사용자 상태','계정 상태 선택값','교수지원과','Y'),('ORG_TYPE','조직 유형','조직 유형 선택값','교수지원과','Y') ON CONFLICT DO NOTHING;
INSERT INTO common_code(group_id,code_value,code_name,sort_order,additional_attributes,valid_from,use_yn) VALUES
('USER_STATUS','ACTIVE','활성',1,'{}','2026-01-01','Y'),('USER_STATUS','DISABLED','비활성',2,'{}','2026-01-01','Y'),('USER_STATUS','LOCKED','잠김',3,'{}','2026-01-01','Y') ON CONFLICT DO NOTHING;

INSERT INTO org(org_code,org_name,org_type,use_yn,created_at,updated_at) VALUES
('UNIV001','한국교원대학교','UNIVERSITY','Y',now(),now()),
('COL001','사범대학','COLLEGE','Y',now(),now()),
('DEP001','컴퓨터교육과','DEPARTMENT','Y',now(),now()),
('OFF001','교수지원과','OFFICE','Y',now(),now()) ON CONFLICT (org_code) DO NOTHING;
INSERT INTO korus_personnel_snapshot(person_id,name,org_code,position_name,rank_name,employment_status,retired_at,last_synced_at,created_at,updated_at) VALUES
('PADMIN','관리자','OFF001','시스템관리자','행정직','ACTIVE',null,now(),now(),now()),
('P1001','김교원','DEP001','학과장','교수','ACTIVE',null,now(),now(),now()),
('P1002','이교원','DEP001','교원','부교수','ACTIVE',null,now(),now(),now()) ON CONFLICT (person_id) DO NOTHING;
INSERT INTO app_user(user_id,korus_person_id,password_hash,use_yn,status,created_at,updated_at) VALUES
('admin','PADMIN','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','Y','ACTIVE',now(),now()),
('teacher01','P1001','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','Y','ACTIVE',now(),now()),
('teacher02','P1002','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','N','INACTIVE',now(),now()) ON CONFLICT (user_id) DO NOTHING;
INSERT INTO org_relation(relation_id,org_code,parent_org_code,valid_from,valid_to,change_reason,created_at,updated_at) OVERRIDING SYSTEM VALUE VALUES
(1,'UNIV001',null,'2026-01-01',null,'seed',now(),now()),
(2,'COL001','UNIV001','2026-01-01',null,'seed',now(),now()),
(3,'DEP001','COL001','2026-01-01',null,'seed',now(),now()),
(4,'OFF001','UNIV001','2026-01-01',null,'seed',now(),now()) ON CONFLICT (relation_id) DO NOTHING;
INSERT INTO org_user_position(person_id,org_code,position_name,created_at,updated_at) VALUES
('P1001','DEP001','학과장',now(),now()),('P1002','DEP001','교원',now(),now()) ON CONFLICT DO NOTHING;
INSERT INTO role(role_code,role_name,purpose,grant_criteria,default_data_scope,use_yn,created_at,updated_at) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','본인','Y',now(),now()),
('R02','학과장','소속 학과 교원 업무 확인','학과장 보직','학과','Y',now(),now()),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','단과대학','Y',now(),now()),
('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','전체','Y',now(),now()),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','전체','Y',now(),now()),
('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','전체','Y',now(),now()),
('R07','실적부서','담당 실적 자료 관리','부서 담당자','부서','Y',now(),now()),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사자','전체조회','Y',now(),now()),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자','전체','Y',now(),now()) ON CONFLICT (role_code) DO NOTHING;
INSERT INTO user_role(assignment_id,user_id,role_code,assignment_type,valid_from,valid_to,approver_id,status,created_at,updated_at) OVERRIDING SYSTEM VALUE VALUES
(1,'admin','R09','MANUAL','2026-01-01',null,'admin','ACTIVE',now(),now()),
(2,'teacher01','R01','POSITION_BASED','2026-01-01',null,'admin','ACTIVE',now(),now()) ON CONFLICT (assignment_id) DO NOTHING;
INSERT INTO menu(menu_id,parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn,created_at,updated_at) OVERRIDING SYSTEM VALUE VALUES
(1,null,'TOP','시스템 관리',null,null,'settings','COMMON','시스템 관리 대메뉴',10,'Y',now(),now()),
(2,1,'MIDDLE','사용자·조직 관리',null,null,'users','COMMON','사용자와 조직 기준정보',20,'Y',now(),now()),
(3,2,'SCREEN','사용자 관리','SCR-USERS','/admin/users','user','COMMON','사용자 조회와 로컬 계정 관리',30,'Y',now(),now()),
(4,2,'SCREEN','조직 관리','SCR-ORGS','/admin/orgs','org','COMMON','조직 트리와 관계 관리',40,'Y',now(),now()),
(5,1,'MIDDLE','역할·권한 관리',null,null,'shield','COMMON','역할과 메뉴 권한 관리',50,'Y',now(),now()),
(6,5,'SCREEN','역할 관리','SCR-ROLES','/admin/roles','role','COMMON','R01~R09 역할 기준 관리',60,'Y',now(),now()),
(7,5,'SCREEN','사용자 역할 관리','SCR-USER-ROLES','/admin/user-roles','user-role','COMMON','사용자별 역할 유효기간 관리',70,'Y',now(),now()),
(8,5,'SCREEN','메뉴 권한 관리','SCR-MENU-PERMISSIONS','/admin/menu-permissions','lock','COMMON','메뉴 접근 matrix 관리',80,'Y',now(),now()),
(9,1,'MIDDLE','메뉴 관리',null,null,'menu','COMMON','메뉴 구조와 실행정보',90,'Y',now(),now()),
(10,9,'SCREEN','메뉴 구조 관리','SCR-MENU-STRUCTURE','/admin/menu-structure','tree','COMMON','메뉴 계층과 순서 관리',100,'Y',now(),now()),
(11,9,'SCREEN','메뉴 정보 관리','SCR-MENU-INFO','/admin/menu-info','info','COMMON','메뉴 실행정보 관리',110,'Y',now(),now()),
(12,1,'MIDDLE','공통코드 관리',null,null,'code','COMMON','공통 코드 관리',120,'Y',now(),now()),
(13,12,'SCREEN','코드그룹 관리','SCR-CODE-GROUPS','/admin/code-groups','folder','COMMON','코드그룹 관리',130,'Y',now(),now()),
(14,12,'SCREEN','상세코드 관리','SCR-CODE-DETAILS','/admin/code-details','list','COMMON','상세코드 관리',140,'Y',now(),now()) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO menu_permission(target_type,target_id,menu_id,access_allowed,created_at,updated_at) SELECT 'ROLE','R09',menu_id,true,now(),now() FROM menu ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;
INSERT INTO menu_permission(target_type,target_id,menu_id,access_allowed,created_at,updated_at) VALUES ('ROLE','R01',1,true,now(),now()) ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;
INSERT INTO code_group(group_id,group_name,description,managing_department,use_yn,created_at,updated_at) VALUES
('EVAL_AREA','평가영역','교수업적 평가영역 공통코드','교수지원과','Y',now(),now()),
('PROCESS_STATUS','처리상태','공통 처리상태 코드','정보전산원','Y',now(),now()),
('AUTH_TYPE','인증구분','인증 수단 구분','정보전산원','Y',now(),now()) ON CONFLICT (group_id) DO NOTHING;
INSERT INTO code_detail(group_id,code_value,code_name,parent_code_value,sort_order,extra_attributes,valid_from,valid_to,use_yn,created_at,updated_at) VALUES
('EVAL_AREA','TEACHING','교육',null,10,'{}','2026-01-01',null,'Y',now(),now()),
('EVAL_AREA','RESEARCH','연구',null,20,'{}','2026-01-01',null,'Y',now(),now()),
('EVAL_AREA','SERVICE','봉사',null,30,'{}','2026-01-01',null,'Y',now(),now()),
('PROCESS_STATUS','ACTIVE','활성',null,10,'{}','2026-01-01',null,'Y',now(),now()),
('PROCESS_STATUS','INACTIVE','비활성',null,20,'{}','2026-01-01',null,'Y',now(),now()) ON CONFLICT (group_id, code_value) DO NOTHING;

SELECT setval(pg_get_serial_sequence('org_relation','relation_id'), GREATEST((SELECT COALESCE(MAX(relation_id), 1) FROM org_relation), 1), true);
SELECT setval(pg_get_serial_sequence('org_user_position','mapping_id'), GREATEST((SELECT COALESCE(MAX(mapping_id), 1) FROM org_user_position), 1), true);
SELECT setval(pg_get_serial_sequence('user_role','assignment_id'), GREATEST((SELECT COALESCE(MAX(assignment_id), 1) FROM user_role), 1), true);
SELECT setval(pg_get_serial_sequence('menu','menu_id'), GREATEST((SELECT COALESCE(MAX(menu_id), 1) FROM menu), 1), true);
SELECT setval(pg_get_serial_sequence('menu_permission','permission_id'), GREATEST((SELECT COALESCE(MAX(permission_id), 1) FROM menu_permission), 1), true);
SELECT setval(pg_get_serial_sequence('change_history','history_id'), GREATEST((SELECT COALESCE(MAX(history_id), 1) FROM change_history), 1), true);

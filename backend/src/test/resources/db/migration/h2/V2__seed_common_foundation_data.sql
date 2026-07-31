INSERT INTO korus_staff_snapshot(staff_id, staff_no, staff_name, organization_code, position_name, job_grade, employment_status, retirement_date, last_synced_at)
VALUES ('KORUS-ADMIN', 'A0001', '시스템 관리자', 'KNUE-ROOT', '시스템관리자', '행정', 'ACTIVE', NULL, now());

INSERT INTO organization(organization_id, organization_code, organization_name, organization_type)
VALUES
('00000000-0000-0000-0000-000000000101', 'KNUE-ROOT', '한국교원대학교', 'UNIVERSITY'),
('00000000-0000-0000-0000-000000000102', 'COL-EDU', '교육과학대학', 'COLLEGE'),
('00000000-0000-0000-0000-000000000103', 'DEPT-COMMON', '공통행정부서', 'OFFICE');

INSERT INTO organization_relation(relation_id, organization_id, parent_organization_id, effective_start_date, effective_end_date)
VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', NULL, '2026-01-01', NULL),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000101', '2026-01-01', NULL),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000101', '2026-01-01', NULL);

INSERT INTO user_account(user_id, login_id, password_hash, korus_staff_id, system_use_enabled, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'KORUS-ADMIN', true, 'ACTIVE');

INSERT INTO organization_user_assignment(assignment_id, user_id, organization_id, position_name, effective_start_date)
VALUES ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000103', '시스템관리자', '2026-01-01');

INSERT INTO role(role_code, role_name, purpose, grant_criteria, default_data_scope) VALUES
('R01','교원','본인 관련 업무 수행','교원 인사 기준','본인'),
('R02','학과장','소속 학과 교원 확인','학과장 보직','소속학과'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 보직','소속대학'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','전체'),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','연구영역'),
('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','입학취업영역'),
('R07','실적부서','담당 실적 자료 관리','실적부서 담당자','담당부서'),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사자 지정','감사범위'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자','전체');

INSERT INTO user_role_assignment(assignment_id, user_id, role_code, assignment_type, valid_from, status, approved_by_user_id)
VALUES ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000001', 'R09', 'MANUAL', '2026-01-01', 'ACTIVE', '00000000-0000-0000-0000-000000000001');

INSERT INTO menu(menu_id, parent_menu_id, menu_name, menu_level, display_order, screen_id, url_path, icon_name, business_category, description) VALUES
('00000000-0000-0000-0000-000000001000', NULL, '시스템 관리', 'TOP', 1, 'SCR-SYSTEM', '/admin', 'settings', '공통', '시스템 관리 대시보드'),
('00000000-0000-0000-0000-000000001100', '00000000-0000-0000-0000-000000001000', '사용자·조직 관리', 'MIDDLE', 1, NULL, NULL, 'users', '공통', '사용자와 조직'),
('00000000-0000-0000-0000-000000001101', '00000000-0000-0000-0000-000000001100', '사용자 관리', 'LEAF', 1, 'SCR-USERS', '/admin/users', 'user', '공통', '사용자 검색 및 역할 관리'),
('00000000-0000-0000-0000-000000001102', '00000000-0000-0000-0000-000000001100', '조직 관리', 'LEAF', 2, 'SCR-ORGS', '/admin/organizations', 'building', '공통', '조직 계층 관리'),
('00000000-0000-0000-0000-000000001200', '00000000-0000-0000-0000-000000001000', '역할·권한 관리', 'MIDDLE', 2, NULL, NULL, 'shield', '공통', '역할과 권한'),
('00000000-0000-0000-0000-000000001201', '00000000-0000-0000-0000-000000001200', '역할 관리', 'LEAF', 1, 'SCR-ROLES', '/admin/roles', 'badge', '공통', '역할 정의'),
('00000000-0000-0000-0000-000000001202', '00000000-0000-0000-0000-000000001200', '사용자 역할 관리', 'LEAF', 2, 'SCR-USERROLES', '/admin/user-roles', 'key', '공통', '사용자 역할 부여'),
('00000000-0000-0000-0000-000000001203', '00000000-0000-0000-0000-000000001200', '메뉴 권한 관리', 'LEAF', 3, 'SCR-MENUPERM', '/admin/menu-permissions', 'lock', '공통', '메뉴 접근권한'),
('00000000-0000-0000-0000-000000001300', '00000000-0000-0000-0000-000000001000', '메뉴 관리', 'MIDDLE', 3, NULL, NULL, 'menu', '공통', '메뉴 구조와 실행정보'),
('00000000-0000-0000-0000-000000001301', '00000000-0000-0000-0000-000000001300', '메뉴 구조 관리', 'LEAF', 1, 'SCR-MENUSTRUCT', '/admin/menus/structure', 'tree', '공통', '메뉴 구조'),
('00000000-0000-0000-0000-000000001302', '00000000-0000-0000-0000-000000001300', '메뉴 정보 관리', 'LEAF', 2, 'SCR-MENUINFO', '/admin/menus/info', 'panel', '공통', '메뉴 실행정보'),
('00000000-0000-0000-0000-000000001400', '00000000-0000-0000-0000-000000001000', '공통코드 관리', 'MIDDLE', 4, NULL, NULL, 'code', '공통', '공통코드'),
('00000000-0000-0000-0000-000000001401', '00000000-0000-0000-0000-000000001400', '코드그룹 관리', 'LEAF', 1, 'SCR-CODEGROUP', '/admin/code-groups', 'folder', '공통', '코드그룹'),
('00000000-0000-0000-0000-000000001402', '00000000-0000-0000-0000-000000001400', '상세코드 관리', 'LEAF', 2, 'SCR-CODEDETAIL', '/admin/code-details', 'list', '공통', '상세코드');

INSERT INTO menu_permission(target_type, target_id, menu_id, allowed)
SELECT 'ROLE', 'R09', menu_id, true FROM menu WHERE menu_level = 'LEAF';

INSERT INTO code_group(group_id, group_name, description, managing_department) VALUES
('EMPLOYMENT_STATUS','재직상태','교직원 재직 상태','시스템관리'),
('ASSIGNMENT_TYPE','역할부여구분','보직 기반 또는 수동 역할','시스템관리');

INSERT INTO code_detail(group_id, code_value, code_name, sort_order, extra_attributes, is_active) VALUES
('EMPLOYMENT_STATUS','ACTIVE','재직',1,'{}',true),
('EMPLOYMENT_STATUS','LEAVE','휴직',2,'{}',true),
('EMPLOYMENT_STATUS','RETIRED','퇴직',3,'{}',true),
('ASSIGNMENT_TYPE','POSITION','보직기반',1,'{}',true),
('ASSIGNMENT_TYPE','MANUAL','수동',2,'{}',true);


CREATE TABLE IF NOT EXISTS common_position (
  position_id bigserial PRIMARY KEY,
  position_code varchar(50) NOT NULL,
  user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  organization_code varchar(50) NOT NULL REFERENCES organization(organization_code),
  effective_start_date date NOT NULL,
  effective_end_date date NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  deleted_at timestamp NULL,
  change_reason varchar(500) NULL
);
COMMENT ON TABLE common_position IS '공통기능 2차 보직 관리 정보. 사용자 인사정보와 조직구조를 변경하지 않고 기존 사용자·조직을 참조하여 보직 유효기간만 관리한다.';
COMMENT ON COLUMN common_position.position_code IS '보직 상세코드 또는 업무 보직 식별자 참조 의도 (FK 미선언)';
COMMENT ON COLUMN common_position.user_id IS 'user_account.user_id 참조';
COMMENT ON COLUMN common_position.organization_code IS 'organization.organization_code 참조';
COMMENT ON COLUMN common_position.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS common_config (
  config_key varchar(100) PRIMARY KEY,
  config_value varchar(100) NOT NULL,
  unit varchar(30) NOT NULL,
  display_name varchar(200) NOT NULL,
  description varchar(1000) NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  change_reason varchar(500) NULL
);
COMMENT ON TABLE common_config IS '세션 유휴시간, 페이지당 조회건수, 기본 검색기간 등 사용자·업무별이 아닌 전역 공통 환경설정 항목.';
COMMENT ON COLUMN common_config.config_key IS '설정 lifecycle identity. CommonConfigService 저장 시 애플리케이션에서 허용 키만 갱신';
COMMENT ON COLUMN common_config.unit IS 'MINUTE:분|COUNT:건|YEAR:년|SECOND:초';
COMMENT ON COLUMN common_config.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS notice (
  notice_id bigserial PRIMARY KEY,
  title varchar(300) NOT NULL,
  content text NOT NULL,
  posting_start_date date NOT NULL,
  posting_end_date date NOT NULL,
  target_role_code varchar(10) NULL REFERENCES role(role_code),
  target_organization_code varchar(50) NULL REFERENCES organization(organization_code),
  important_yn char(1) NOT NULL DEFAULT 'N',
  created_by varchar(50) NOT NULL REFERENCES user_account(user_id),
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now(),
  deleted_at timestamp NULL,
  change_reason varchar(500) NULL
);
COMMENT ON TABLE notice IS '공지사항 관리 정보. 게시기간과 대상 역할·조직에 따라 노출하며 평가기간·권한 설정·업무 승인 상태를 변경하지 않는다.';
COMMENT ON COLUMN notice.target_role_code IS 'role.role_code 참조';
COMMENT ON COLUMN notice.target_organization_code IS 'organization.organization_code 참조';
COMMENT ON COLUMN notice.important_yn IS 'Y:중요|N:일반';
COMMENT ON COLUMN notice.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS notice_attachment (
  attachment_id bigserial PRIMARY KEY,
  notice_id bigint NOT NULL REFERENCES notice(notice_id) ON DELETE CASCADE,
  original_file_name varchar(300) NOT NULL,
  stored_file_path varchar(1000) NOT NULL,
  file_size bigint NOT NULL DEFAULT 0,
  created_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE notice_attachment IS '공지사항 첨부파일 메타데이터. 독립 첨부파일 관리·파일정책 관리는 현재 범위 밖이며 공지 상세 표시용 정보만 저장한다.';
COMMENT ON COLUMN notice_attachment.notice_id IS 'notice.notice_id 참조';
COMMENT ON COLUMN notice_attachment.stored_file_path IS 'NoticeService 저장 시 전달받은 파일 메타데이터 경로. 실제 파일 저장소 정책은 현재 미사용 — 추후 연동 예정';

CREATE INDEX IF NOT EXISTS idx_common_position_code_org ON common_position(position_code, organization_code);
CREATE INDEX IF NOT EXISTS idx_common_position_effective ON common_position(effective_start_date, effective_end_date);
CREATE INDEX IF NOT EXISTS idx_notice_posting_period ON notice(posting_start_date, posting_end_date);
CREATE INDEX IF NOT EXISTS idx_notice_target_role ON notice(target_role_code);
CREATE INDEX IF NOT EXISTS idx_notice_target_org ON notice(target_organization_code);
CREATE INDEX IF NOT EXISTS idx_notice_attachment_notice ON notice_attachment(notice_id);

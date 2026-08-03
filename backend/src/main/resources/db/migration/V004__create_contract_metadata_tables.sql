CREATE TABLE IF NOT EXISTS technology_stack (
  area varchar(40) PRIMARY KEY,
  contract varchar(500) NOT NULL,
  canonical_id varchar(100) NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE technology_stack IS 'data-model.md 공통 계약 참조의 기술스택 항목. 런타임 검증 화면과 health API가 DB 기준 계약을 조회한다.';
COMMENT ON COLUMN technology_stack.area IS 'Backend:백엔드|Frontend:프론트엔드|Database:데이터베이스|Infrastructure:인프라';
COMMENT ON COLUMN technology_stack.canonical_id IS 'Requirement Registry canonical_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN technology_stack.use_yn IS 'Y:사용|N:미사용';
CREATE INDEX IF NOT EXISTS idx_technology_stack_area ON technology_stack(area);

CREATE TABLE IF NOT EXISTS version_b_o_m (
  bom_key varchar(50) PRIMARY KEY,
  version_value varchar(50) NOT NULL,
  canonical_id varchar(100) NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE version_b_o_m IS 'data-model.md Version BOM 계약. Spring Boot, Node, Vite, React, PostgreSQL major version을 DB 기준으로 보존한다.';
COMMENT ON COLUMN version_b_o_m.bom_key IS 'spring_boot:스프링부트|node:노드|vite:바이트|react:리액트|postgres:포스트그레스';
COMMENT ON COLUMN version_b_o_m.canonical_id IS 'Requirement Registry canonical_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN version_b_o_m.use_yn IS 'Y:사용|N:미사용';
CREATE INDEX IF NOT EXISTS idx_version_b_o_m_key ON version_b_o_m(bom_key);

CREATE TABLE IF NOT EXISTS required_outputs (
  output_key varchar(50) PRIMARY KEY,
  output_value varchar(300) NOT NULL,
  canonical_id varchar(100) NOT NULL,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE required_outputs IS 'data-model.md required_outputs 계약. backend/frontend/compose/health 필수 산출물 경로를 DB 기준으로 검증한다.';
COMMENT ON COLUMN required_outputs.output_key IS 'backend_dir:백엔드경로|frontend_dir:프론트엔드경로|compose_file:컴포즈파일|health_endpoint:상태확인엔드포인트';
COMMENT ON COLUMN required_outputs.canonical_id IS 'Requirement Registry canonical_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN required_outputs.use_yn IS 'Y:사용|N:미사용';
CREATE INDEX IF NOT EXISTS idx_required_outputs_key ON required_outputs(output_key);

INSERT INTO technology_stack (area, contract, canonical_id, use_yn) VALUES
('Backend', 'Java 17, Spring Boot 3.3.x, Maven, MyBatis, PostgreSQL 16, executable JAR', 'REQ-032|REQ-044', 'Y'),
('Frontend', 'React 18, TypeScript, Vite 5, nginx static serving and /api/* reverse proxy', 'REQ-033', 'Y'),
('Database', 'PostgreSQL 16, Flyway migration, MyBatis blocking access', 'REQ-034|REQ-042|REQ-049', 'Y'),
('Infrastructure', 'backend, frontend, database services in Docker Compose', 'REQ-016|REQ-034|REQ-041', 'Y')
ON CONFLICT (area) DO UPDATE SET contract = EXCLUDED.contract, canonical_id = EXCLUDED.canonical_id, use_yn = EXCLUDED.use_yn, updated_at = now();

INSERT INTO version_b_o_m (bom_key, version_value, canonical_id, use_yn) VALUES
('spring_boot', '3.3.x', 'REQ-045', 'Y'),
('node', '20.x', 'REQ-045', 'Y'),
('vite', '5.x', 'REQ-045', 'Y'),
('react', '18.x', 'REQ-045', 'Y'),
('postgres', '16.x', 'REQ-045', 'Y')
ON CONFLICT (bom_key) DO UPDATE SET version_value = EXCLUDED.version_value, canonical_id = EXCLUDED.canonical_id, use_yn = EXCLUDED.use_yn, updated_at = now();

INSERT INTO required_outputs (output_key, output_value, canonical_id, use_yn) VALUES
('backend_dir', 'backend', 'REQ-046', 'Y'),
('frontend_dir', 'frontend', 'REQ-046', 'Y'),
('compose_file', 'infra/docker-compose.yml', 'REQ-046', 'Y'),
('health_endpoint', '/api/health', 'REQ-046', 'Y')
ON CONFLICT (output_key) DO UPDATE SET output_value = EXCLUDED.output_value, canonical_id = EXCLUDED.canonical_id, use_yn = EXCLUDED.use_yn, updated_at = now();

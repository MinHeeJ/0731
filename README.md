# 0731

Initialized by AIOps Code Agent.


## 교원사이트 공통기능 1차 구현

- Backend: Java 17, Spring Boot 3.3.x, Maven, MyBatis, PostgreSQL 16
- Frontend: React 18, TypeScript, Vite 5, nginx `/api/*` reverse proxy
- Infra: `infra/docker-compose.yml` with `database`, `backend`, `frontend` services
- 기본 계정: `admin` / `admin` (R09 시스템관리자)

### 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

브라우저: http://localhost:3000/login

### 포함 화면

사용자 관리, 조직 관리, 역할 관리, 사용자 역할 관리, 메뉴 권한 관리, 메뉴 구조 관리, 메뉴 정보 관리, 코드그룹 관리, 상세코드 관리.

상세 검증 명령은 `docs/validation-commands.md`를 참고하세요.

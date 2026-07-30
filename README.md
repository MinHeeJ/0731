# KNUE 교수업적평가시스템 공통기능 1차

Spring Boot 3.3, React 18, PostgreSQL 16, Docker Compose 기반의 시스템 관리 9개 화면/API 구현입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: frontend proxy를 통해 http://localhost:3000/api/health
- 로그인: `admin` / `admin`

## 주요 검증

1. `/login` 접속 후 상태 점검 badge가 표시되는지 확인합니다.
2. `admin/admin`으로 로그인하면 `/admin/users`로 이동합니다.
3. Sidebar에서 다음 9개 관리 화면이 404 없이 열립니다.
   - 사용자 관리, 조직 관리
   - 역할 관리, 사용자 역할 관리, 메뉴 권한 관리
   - 메뉴 구조 관리, 메뉴 정보 관리
   - 코드그룹 관리, 상세코드 관리
4. API는 모두 `/api/...` 상대 경로로 호출됩니다.

## Handoff blocker

실행·영속성 계약의 `decision_status=clarification_required`는 build-ready handoff blocker로 유지됩니다. 상세 내용은 `docs/evidence/handoff-blockers.md`를 확인하세요.

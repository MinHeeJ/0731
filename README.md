# KNUE 교수업적평가시스템 공통기능 1차

Spring Boot 3.3.x + MyBatis + PostgreSQL 16, React 18 + TypeScript + Vite 5, Docker Compose 구성입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Swagger UI: http://localhost:3000/swagger-ui.html

## 시드 계정

- ID: admin
- Password: admin
- Role: R09 시스템관리자

## 주요 검증

1. `/login`에서 admin/admin 로그인 후 `/admin/users`로 이동합니다.
2. sidebar의 9개 관리 화면이 404 없이 렌더링되는지 확인합니다.
3. `/api/admin/users`, `/api/admin/orgs/tree`, `/api/admin/roles`, `/api/admin/menu-permissions`, `/api/admin/menus`, `/api/admin/code-groups`가 R09 세션으로 2xx를 반환하는지 확인합니다.
4. 세션 없이 보호 API를 호출하면 401, R09가 아닌 세션으로 관리 API를 호출하면 403을 반환합니다.

## Handoff blocker

실행·영속성 계약의 `decision_status=clarification_required`는 UI 배너와 README에 명시되어 있으며 명확화 전 build-ready handoff 차단 사유입니다.

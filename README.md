# CMS-1344 공통기능 1차 완료

한국교원대학교 교수업적평가시스템의 시스템 관리 공통기능 9개 화면과 Spring Boot/MyBatis/PostgreSQL API 구현입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Swagger UI: http://localhost:3000/swagger-ui.html

시드 관리자 계정은 로그인 ID `admin`, 비밀번호 `admin`입니다. 비밀번호는 DB에 SHA-256 해시로 저장됩니다.

## 구현 범위

- 사용자 관리 `/system/users`
- 조직 관리 `/system/organizations`
- 역할 관리 `/system/roles`
- 사용자 역할 관리 `/system/user-roles`
- 메뉴 권한 관리 `/system/menu-permissions`
- 메뉴 구조 관리 `/system/menu-structure`
- 메뉴 정보 관리 `/system/menu-info`
- 코드그룹 관리 `/system/code-groups`
- 상세코드 관리 `/system/code-details`

## 검증 명령

요청 계약에 따라 codegen 중에는 `mvn test`, `npm test`, package manager 실행, dev server 실행을 수행하지 않았습니다. 실행이 허용된 환경에서 다음을 사용하세요.

```bash
(cd backend && mvn test)
(cd frontend && npm test -- --run)
docker compose -f infra/docker-compose.yml config
```

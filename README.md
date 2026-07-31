# 공통기능 1차 완료 목표

한국교원대학교 교수업적평가시스템의 시스템 관리 9개 기능 MVP입니다.

## 기술 계약

- Backend: Java 17, Spring Boot 3.3.x, Maven, MyBatis, PostgreSQL 16, executable boot jar
- Frontend: React 18, TypeScript, Vite 5, nginx `/api/*` reverse proxy
- Infra: Docker Compose, Flyway migration, `/api/health`

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build -d
curl -i http://localhost:8080/api/health
```

Frontend: http://localhost:5173
Backend health: http://localhost:8080/api/health

## 로그인

- loginId: `admin`
- password: `admin`

## 검증

```bash
cd backend && ./mvnw test
cd frontend && npm test -- --run
./scripts/validate-common-foundation.sh
```

생성된 화면: 로그인, 사용자 관리, 조직 관리, 역할 관리, 사용자 역할 관리, 메뉴 권한 관리, 메뉴 구조 관리, 메뉴 정보 관리, 코드그룹 관리, 상세코드 관리.

브라우저 API 호출은 상대경로 `/api/...`만 사용하며 실제 KORUS/SSO/외부기관 API에는 접속하지 않습니다.

# 한국교원대학교 교수업적평가시스템 공통기능 1차

Spring Boot 3.3.x + MyBatis + PostgreSQL 16, React 18 + Vite 5, Docker Compose 구성입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Health: http://localhost:3000/api/health
- Swagger UI: http://localhost:3000/swagger-ui/index.html
- Seed admin: `admin` / `admin`

## 구현 범위

시스템 관리의 9개 1차 목표 화면만 구현했습니다: 사용자 관리, 조직 관리, 역할 관리, 사용자 역할 관리, 메뉴 권한 관리, 메뉴 구조 관리, 메뉴 정보 관리, 코드그룹 관리, 상세코드 관리.
실제 SSO/KORUS/파일/Excel/접속기록/배치 운영은 후속 확장 경계이며, 현재 KORUS 인사 정보는 조회 전용 snapshot으로만 사용합니다.

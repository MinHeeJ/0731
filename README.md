# 한국교원대학교 교수업적평가시스템 공통기능 1차

Java 17/Spring Boot 3.3/MyBatis/PostgreSQL 16 backend, React 18/TypeScript/Vite 5 frontend, Docker Compose runtime으로 구성된 시스템 관리 1차 공통기능입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Login seed: admin / admin

## 구현 범위

사용자 관리, 조직 관리, 역할 관리, 사용자 역할 관리, 메뉴 권한 관리, 메뉴 구조 관리, 메뉴 정보 관리, 코드그룹 관리, 상세코드 관리만 포함합니다. KORUS 정보는 로컬 Mock snapshot으로 조회 전용 제공되며, 교수업적평가·학술지원금 업무 데이터는 구현하지 않았습니다.

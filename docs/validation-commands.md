# 검증 명령

패키지 설치와 장시간 실행은 코드 생성 단계에서 수행하지 않았습니다. 아래 명령은 개발자/품질 검증 단계에서 실행합니다.

## Backend

```bash
cd backend
mvn test
```

기대 결과: JUnit 5 테스트가 통과하고 Spring Boot executable boot jar 구성이 유지됩니다.

## Frontend

```bash
cd frontend
npm install
npm test -- --run
npm run build
```

기대 결과: Vitest 테스트가 통과하고 Vite 5 React 18 빌드가 생성됩니다. API 호출은 `/api/...` 상대경로만 사용합니다.

## Docker Compose smoke

```bash
docker compose -f infra/docker-compose.yml up --build
```

별도 터미널에서:

```bash
curl -fsS http://localhost:3000/api/health
curl -i -c /tmp/cms-cookie.txt -H 'Content-Type: application/json' \
  -d '{"loginId":"admin","password":"admin"}' \
  http://localhost:3000/api/auth/login
curl -b /tmp/cms-cookie.txt http://localhost:3000/api/menus/my
curl -b /tmp/cms-cookie.txt http://localhost:3000/api/users
```

기대 결과: health는 `success=true`, admin 로그인 후 R09 세션, 9개 관리 화면 메뉴, 사용자 조회 결과가 반환됩니다.

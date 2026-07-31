#!/usr/bin/env bash
set -euo pipefail
COOKIE_JAR=${COOKIE_JAR:-/tmp/cms-common-foundation-cookie.txt}
docker compose -f infra/docker-compose.yml config >/dev/null
cd backend && ./mvnw test && cd - >/dev/null
cd frontend && npm test -- --run && npm run build && cd - >/dev/null
docker compose -f infra/docker-compose.yml up --build -d database backend frontend
curl -fsS http://localhost:8080/api/health | grep '"success":true'
curl -fsS -c "$COOKIE_JAR" -H 'Content-Type: application/json' -d '{"loginId":"admin","password":"admin"}' http://localhost:8080/api/auth/login | grep '"success":true'
for path in users organizations roles user-roles menus menu-permissions code-groups code-details; do
  curl -fsS -b "$COOKIE_JAR" "http://localhost:8080/api/$path" | grep '"success":true'
done
curl -fsS http://localhost:5173/ >/dev/null

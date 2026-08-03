#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"
docker compose -f infra/docker-compose.yml up --build -d
trap 'docker compose -f infra/docker-compose.yml down' EXIT
for _ in $(seq 1 60); do
  if curl -fsS http://localhost:3000/api/health >/tmp/cms-health.json; then break; fi
  sleep 2
done
cat /tmp/cms-health.json | grep '"success"[[:space:]]*:[[:space:]]*true'
curl -fsS -c /tmp/cms-cookie.txt -H 'Content-Type: application/json' -d '{"loginId":"admin","password":"admin"}' http://localhost:3000/api/auth/login | grep '"success"[[:space:]]*:[[:space:]]*true'
curl -fsS -b /tmp/cms-cookie.txt http://localhost:3000/api/menus/my | grep '사용자 관리'
curl -fsS -b /tmp/cms-cookie.txt http://localhost:3000/api/users | grep 'staffNo'

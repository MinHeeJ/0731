#!/usr/bin/env bash
set -euo pipefail
docker compose -f infra/docker-compose.yml config >/dev/null
docker compose -f infra/docker-compose.yml up --build -d database backend frontend
curl -fsS http://localhost:8080/api/health | grep '"success":true'
curl -fsS http://localhost:5173/ >/dev/null

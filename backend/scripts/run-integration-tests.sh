#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.test.yml"

export TEST_DB_HOST="localhost"
export TEST_DB_PORT="55432"
export TEST_DB_NAME="todoapp_int"
export TEST_DB_USER="todo"
export TEST_DB_PASSWORD="todo"

cleanup() {
  docker compose -f "$COMPOSE_FILE" down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "Starting test Postgres..."
docker compose -f "$COMPOSE_FILE" up -d

echo "Waiting for Postgres to be healthy..."
ATTEMPTS=0
MAX_ATTEMPTS=30
while true; do
  STATUS=$(docker compose -f "$COMPOSE_FILE" ps -q test-db | xargs docker inspect -f '{{ .State.Health.Status }}')
  if [[ "$STATUS" == "healthy" ]]; then
    break
  fi
  ATTEMPTS=$((ATTEMPTS + 1))
  if [[ $ATTEMPTS -ge $MAX_ATTEMPTS ]]; then
    echo "Postgres did not become healthy in time"
    exit 1
  fi
  sleep 2
done

echo "Running integration tests..."
cd "$ROOT_DIR"
mvn -B -P integration-tests test

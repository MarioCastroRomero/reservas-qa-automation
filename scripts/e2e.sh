#!/usr/bin/env bash

cd "$(dirname "$0")/.."

API_PORT=8081
WEB_PORT=5174
TEST_DB=${TEST_DB:-reserva_test_db}

set -m
pid=
cleanup() {
  [ -n "$pid" ] && kill -TERM -- "-$pid" 2>/dev/null
  wait 2>/dev/null
}
trap cleanup EXIT INT TERM

SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/$TEST_DB" \
APP_CORS_ALLOWED_ORIGINS="http://localhost:$WEB_PORT" \
API_PORT=$API_PORT WEB_PORT=$WEB_PORT \
  ./scripts/dev.sh >target/e2e-servers.log 2>&1 &
pid=$!

echo "Esperando a la API (:$API_PORT) y al frontend (:$WEB_PORT)... (logs en target/e2e-servers.log)"
for _ in $(seq 1 180); do
  if curl -s -o /dev/null "http://localhost:$API_PORT/business" && curl -s -o /dev/null "http://localhost:$WEB_PORT"; then
    ready=1
    break
  fi
  if ! kill -0 "$pid" 2>/dev/null; then
    echo "Los servidores se han parado; revisa target/e2e-servers.log" >&2
    exit 1
  fi
  sleep 1
done
[ -n "$ready" ] || { echo "Los servidores no respondieron en 3 minutos; revisa target/e2e-servers.log" >&2; exit 1; }

./mvnw test -Dtest=E2ERunner -Dsurefire.failIfNoSpecifiedTests=false -Dheadless="${HEADLESS:-true}" "$@"

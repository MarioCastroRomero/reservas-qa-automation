#!/usr/bin/env bash

cd "$(dirname "$0")/.."

API_PORT=${API_PORT:-8080}
WEB_PORT=${WEB_PORT:-5173}

set -m

port_in_use() {
  lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

for port in $API_PORT $WEB_PORT; do
  if port_in_use "$port"; then
    echo "El puerto $port ya está en uso. Cierra lo que lo esté usando (o ejecuta 'lsof -iTCP:$port -sTCP:LISTEN')." >&2
    exit 1
  fi
done

pids=()

start() {
  local prefix=$1
  shift
  ( "$@" 2>&1 | awk -v p="$prefix" '{ print p $0; fflush() }' ) &
  pids+=($!)
}

stopping=0
cleanup() {
  [ "$stopping" = 1 ] && return
  stopping=1
  echo
  echo "Parando API y frontend..."
  for pid in "${pids[@]}"; do
    kill -TERM -- "-$pid" 2>/dev/null
  done
  wait 2>/dev/null
  echo "Listo. La base de datos sigue arrancada (para pararla: make stop)."
}
trap cleanup INT TERM EXIT

start $'\033[36m[api]\033[0m ' env SERVER_PORT="$API_PORT" ./mvnw -q spring-boot:run
start $'\033[35m[web]\033[0m ' env API_TARGET="http://localhost:$API_PORT" npm --prefix frontend run dev -- --port "$WEB_PORT" --strictPort

(
  for _ in $(seq 1 180); do
    if curl -s -o /dev/null "http://localhost:$API_PORT/business"; then
      printf '\nListo: http://localhost:%s (Ctrl+C para parar)\n\n' "$WEB_PORT"
      exit 0
    fi
    sleep 1
  done
  printf '\n\033[33m⚠ La API no ha respondido en 3 minutos; revisa los logs [api].\033[0m\n'
) &
pids+=($!)

while kill -0 "${pids[0]}" 2>/dev/null && kill -0 "${pids[1]}" 2>/dev/null; do
  sleep 1
done

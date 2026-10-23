#!/usr/bin/env bash
# =============================================================================
# to-env-inline.sh
#
# Environment variables of the project (.env + backend/src/main/resources/
# application.yml). All values are FIXED in this file; when a value changes,
# edit it below.
#
# Database: PostgreSQL (port 5432), database car_reselling.
#
# -----------------------------------------------------------------------------
# 1) Print mode (default) – SINGLE LINE, ";" as the only separator:
#
#      ./to-env-inline.sh loc
#      ./to-env-inline.sh prd
#
#      key1=value1;key2=value2;key3=value3
#
# 2) Export mode (--exp) – exports every variable to the CURRENT shell session
#    with `export key=value`, so you can then run e.g. `./gradlew bootRun`:
#
#      source ./to-env-inline.sh --exp --loc
#      source ./to-env-inline.sh --exp --prd
#      . ./to-env-inline.sh --exp --loc          # same thing
#
#    IMPORTANT: it MUST be sourced (`source` or `.`). A script that is executed
#    (./to-env-inline.sh) runs in a child process and can never change the
#    variables of your terminal. If you execute it with --exp anyway, nothing is
#    exported; it prints ready-to-use `export` lines instead, which you can load with:
#
#      eval "$(./to-env-inline.sh --exp --loc)"
#
# Environment and flags can be given with or without "--" and in any order
# (loc | --loc | prd | --prd | exp | --exp).
# =============================================================================

_tei_usage() {
  echo "Usage:" >&2
  echo "  ./to-env-inline.sh <loc|prd>                 print KEY=VALUE;KEY=VALUE;... (single line)" >&2
  echo "  source ./to-env-inline.sh --exp <--loc|--prd>   export the variables to the current session" >&2
}

# Prints the variables of the chosen environment, one KEY=VALUE per line.
_tei_vars_loc() {
  printf '%s\n' \
    'DOCKERHUB_USERNAME=change-me' \
    'API_VERSION=1' \
    'UI_VERSION=1' \
    'AUTH_VERSION=1' \
    'MYSQL_HOST=localhost' \
    'MYSQL_PORT=5432' \
    'MYSQL_DATABASE=car_reselling' \
    'MYSQL_USER=car' \
    'MYSQL_PASSWORD=car' \
    'MYSQL_ROOT_PASSWORD=root' \
    'SERVER_PORT=8082' \
    'CAR_RESELLING_SERVER_PORT=8082' \
    'SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/car_reselling' \
    'SPRING_DATASOURCE_USERNAME=car' \
    'SPRING_DATASOURCE_PASSWORD=car' \
    'CAR_RESELLING_DB_URL=jdbc:postgresql://localhost:5432/car_reselling' \
    'CAR_RESELLING_DB_USERNAME=car' \
    'CAR_RESELLING_DB_PASSWORD=car' \
    'STORAGE_BASE_PATH=./storage' \
    'AUTH_BASE_URL=http://localhost:8081' \
    'ORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:*' \
    'CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:*' \
    'JWT_SECRET=change-me-to-a-long-random-secret-string-at-least-32-chars' \
    'SWAGGER_ENABLED=true' \
    'MAIL_USERNAME=' \
    'MAIL_PASSWORD=' \
    'GOOGLE_CLIENT_ID=' \
    'GOOGLE_CLIENT_SECRET=' \
    'FACEBOOK_CLIENT_ID=' \
    'FACEBOOK_CLIENT_SECRET=' \
    'FRONTEND_URL=http://localhost:5173' \
    'OBSERVABILITY_TRACING_ENABLED=true' \
    'OBSERVABILITY_SERVICE_NAME=car-reselling-api' \
    'OBSERVABILITY_OTLP_ENDPOINT=http://localhost:4317' \
    'OBSERVABILITY_OTLP_HEADERS=' \
    'OBSERVABILITY_METRICS_INTERVAL=30s' \
    'OTLP_METRICS_ENABLED=true' \
    'OTLP_METRICS_URL=http://localhost:4318/v1/metrics' \
    'OTLP_METRICS_STEP=30s' \
    'LOKI_URL=http://localhost:3100'
}

_tei_vars_prd() {
  printf '%s\n' \
    'DOCKERHUB_USERNAME=change-me' \
    'API_VERSION=1' \
    'UI_VERSION=1' \
    'AUTH_VERSION=1' \
    'MYSQL_HOST=postgres' \
    'MYSQL_PORT=5432' \
    'MYSQL_DATABASE=car_reselling' \
    'MYSQL_USER=change-me' \
    'MYSQL_PASSWORD=change-me' \
    'MYSQL_ROOT_PASSWORD=change-me-root' \
    'SERVER_PORT=8080' \
    'CAR_RESELLING_SERVER_PORT=8080' \
    'SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/car_reselling' \
    'SPRING_DATASOURCE_USERNAME=change-me' \
    'SPRING_DATASOURCE_PASSWORD=change-me' \
    'CAR_RESELLING_DB_URL=jdbc:postgresql://postgres:5432/car_reselling' \
    'CAR_RESELLING_DB_USERNAME=change-me' \
    'CAR_RESELLING_DB_PASSWORD=change-me' \
    'STORAGE_BASE_PATH=/storage' \
    'AUTH_BASE_URL=http://authentication-api:8081' \
    'ORS_ALLOWED_ORIGIN_PATTERNS=https://carreselling.vps7659.panel.icontainer.run' \
    'CORS_ALLOWED_ORIGIN_PATTERNS=https://carreselling.vps7659.panel.icontainer.run' \
    'JWT_SECRET=change-me-to-a-long-random-secret-string-at-least-32-chars' \
    'SWAGGER_ENABLED=false' \
    'MAIL_USERNAME=' \
    'MAIL_PASSWORD=' \
    'GOOGLE_CLIENT_ID=' \
    'GOOGLE_CLIENT_SECRET=' \
    'FACEBOOK_CLIENT_ID=' \
    'FACEBOOK_CLIENT_SECRET=' \
    'FRONTEND_URL=http://localhost' \
    'OBSERVABILITY_TRACING_ENABLED=true' \
    'OBSERVABILITY_SERVICE_NAME=car-reselling-api' \
    'OBSERVABILITY_OTLP_ENDPOINT=http://otel-collector:4317' \
    'OBSERVABILITY_OTLP_HEADERS=' \
    'OBSERVABILITY_METRICS_INTERVAL=30s' \
    'OTLP_METRICS_ENABLED=true' \
    'OTLP_METRICS_URL=http://otel-collector:4318/v1/metrics' \
    'OTLP_METRICS_STEP=30s' \
    'LOKI_URL=http://loki:3100'
}

# Main logic. Returns a status instead of calling `exit`, so that it is safe to
# run inside the user's terminal when the script is sourced.
# $1 = 1 when the script is being sourced, 0 when executed; the rest are the arguments.
_tei_main() {
  local sourced="$1"
  shift

  local env_name="" do_export=0 arg name
  for arg in "$@"; do
    name="${arg#--}"
    case "$name" in
      loc|prd)
        if [[ -n "$env_name" && "$env_name" != "$name" ]]; then
          echo "Error: choose only one environment (loc or prd)" >&2
          _tei_usage
          return 1
        fi
        env_name="$name"
        ;;
      exp) do_export=1 ;;
      *)
        echo "Error: invalid option '$arg'" >&2
        _tei_usage
        return 1
        ;;
    esac
  done

  if [[ -z "$env_name" ]]; then
    echo "Error: missing environment (loc or prd)" >&2
    _tei_usage
    return 1
  fi

  # --- Print mode: KEY=VALUE;KEY=VALUE;... on a single line -----------------
  if [[ $do_export -eq 0 ]]; then
    local line result=""
    while IFS= read -r line; do
      result="${result:+$result;}$line"
    done < <("_tei_vars_$env_name")
    echo "$result"
    return 0
  fi

  # --- Export mode, but executed instead of sourced -------------------------
  if [[ $sourced -eq 0 ]]; then
    echo "Warning: this script was executed, so it cannot export variables to your terminal." >&2
    echo "         Use:  source ./to-env-inline.sh --exp --$env_name" >&2
    echo "         or:   eval \"\$(./to-env-inline.sh --exp --$env_name)\"" >&2
    local line
    while IFS= read -r line; do
      printf 'export %s=%q\n' "${line%%=*}" "${line#*=}"
    done < <("_tei_vars_$env_name")
    return 0
  fi

  # --- Export mode, sourced: export key=value in the current session --------
  local line count=0
  while IFS= read -r line; do
    export "$line"
    count=$((count + 1))
  done < <("_tei_vars_$env_name")
  echo "to-env-inline: $count variable(s) exported to the current session ($env_name)"
  return 0
}

# Was this file sourced (source / .) or executed?
_tei_sourced=0
if [[ "${BASH_SOURCE[0]}" != "$0" ]]; then
  _tei_sourced=1
fi

_tei_main "$_tei_sourced" "$@"
_tei_status=$?

# Do not leave helper functions in the user's session.
unset -f _tei_usage _tei_vars_loc _tei_vars_prd _tei_main

if [[ $_tei_sourced -eq 1 ]]; then
  # `return` (not `exit`) so the user's terminal is not closed.
  unset _tei_sourced
  return "$_tei_status"
else
  exit "$_tei_status"
fi

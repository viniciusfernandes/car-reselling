#!/usr/bin/env bash
# Reads .env and prints all key=value pairs as a single inline string separated by semicolons.
# Usage: ./print-env-inline.sh

ENV_FILE="$(dirname "$0")/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: .env file not found at $ENV_FILE" >&2
  exit 1
fi

result=""
while IFS= read -r line; do
  # Skip blank lines and comments
  [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue

  if [[ -n "$result" ]]; then
    result="$result;$line"
  else
    result="$line"
  fi
done < "$ENV_FILE"

echo "$result"

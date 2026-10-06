#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p pwd
PROJECT_DIR="$(pwd)"
PORT="${PORT:-3000}"
DIST_DIR="$PROJECT_DIR/dist"
WEB_DIR="${OPENCODE_WEB_DIR:-/home/runner/work/_temp/omgithub-web}"
DEPLOYMENT_OUTPUT="$WEB_DIR/deployment-output.json"
/usr/bin/time -p mkdir -p "$DIST_DIR" "$WEB_DIR"
if /usr/bin/time -p test -f "$PROJECT_DIR/package.json"; then
  if /usr/bin/time -p test -f "$PROJECT_DIR/package-lock.json"; then
    /usr/bin/time -p npm ci --no-audit --no-fund
  else
    /usr/bin/time -p npm install --no-audit --no-fund
  fi
  if /usr/bin/time -p npm run --silent build --if-present; then
    true
  else
    echo "Build script failed." >&2
    exit 1
  fi
fi
if ! /usr/bin/time -p test -f "$DIST_DIR/index.html"; then
  echo "Static deployment output must contain index.html: $DIST_DIR/index.html" >&2
  exit 1
fi
/usr/bin/time -p python3 -c 'import json,sys; json.dump({"project": sys.argv[1], "directory": sys.argv[2]}, open(sys.argv[3], "w"))' "$PROJECT_DIR" "$DIST_DIR" "$DEPLOYMENT_OUTPUT"
/usr/bin/time -p cat "$DEPLOYMENT_OUTPUT"
echo "Serving $DIST_DIR on port $PORT (project $PROJECT_DIR)"
exec /usr/bin/time -p python3 -m http.server "$PORT" --directory "$DIST_DIR" --bind 0.0.0.0

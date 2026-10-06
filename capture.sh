#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p pwd
RUNTIME_DIR="${RUNTIME_DIR:-/home/runner/work/_temp/omgithub-runtime}"
if /usr/bin/time -p test -z "${CAPTURE_URL:-}"; then
  echo "Set CAPTURE_URL." >&2
  exit 1
fi
if /usr/bin/time -p test -z "${CAPTURE_DIR:-}"; then
  echo "Set CAPTURE_DIR." >&2
  exit 1
fi
/usr/bin/time -p mkdir -p "$CAPTURE_DIR"
/usr/bin/time -p test -f "$RUNTIME_DIR/scripts/default-capture.mjs"
set +e
/usr/bin/time -p node "$RUNTIME_DIR/scripts/default-capture.mjs"
status=$?
set -e
if /usr/bin/time -p test "$status" -ne 0; then
  exit "$status"
fi
if ! /usr/bin/time -p test -f "$CAPTURE_DIR/final-desktop.png"; then
  echo "Missing $CAPTURE_DIR/final-desktop.png" >&2
  exit 1
fi
if ! /usr/bin/time -p test -f "$CAPTURE_DIR/final-mobile.png"; then
  echo "Missing $CAPTURE_DIR/final-mobile.png" >&2
  exit 1
fi
/usr/bin/time -p ls -l "$CAPTURE_DIR/final-desktop.png" "$CAPTURE_DIR/final-mobile.png"

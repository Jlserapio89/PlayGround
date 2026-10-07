#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p bash -c 'echo "NexusAI capture start"'
/usr/bin/time -p bash -c 'test -n "${CAPTURE_URL:-}" || { echo "Set CAPTURE_URL." >&2; exit 1; }'
/usr/bin/time -p bash -c 'test -n "${CAPTURE_DIR:-}" || { echo "Set CAPTURE_DIR." >&2; exit 1; }'
/usr/bin/time -p mkdir -p "${CAPTURE_DIR:?}"
/usr/bin/time -p bash -c 'case "$CAPTURE_DIR" in /home/runner/work/PlayGround/PlayGround/*) echo "CAPTURE_DIR must be outside source" >&2; exit 1;; esac'
RUNTIME="${RUNTIME_DIR:-/home/runner/work/_temp/omgithub-runtime}"
/usr/bin/time -p test -f "$RUNTIME/scripts/default-capture.mjs"
status=0
set +e
/usr/bin/time -p node "$RUNTIME/scripts/default-capture.mjs"
status=$?
set -e
/usr/bin/time -p bash -c 'echo "capture backend exit: '"$status"'"'
/usr/bin/time -p test -f "$CAPTURE_DIR/final-desktop.png"
/usr/bin/time -p test -f "$CAPTURE_DIR/final-mobile.png"
/usr/bin/time -p ls -l "$CAPTURE_DIR"
exit $status

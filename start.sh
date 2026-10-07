#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p bash -c 'echo "NexusAI static preview start (project: $(pwd))"'
PORT="${PORT:-3000}"
export PORT
PROJECT_ROOT="$(pwd)"
DIST_DIR="$PROJECT_ROOT/dist"
WEB_DIR="${OPENCODE_WEB_DIR:-/home/runner/work/_temp/omgithub-web}"
export WEB_DIR
/usr/bin/time -p mkdir -p "$DIST_DIR" "$WEB_DIR"
/usr/bin/time -p test -f "$DIST_DIR/index.html"
/usr/bin/time -p bash -c 'echo "No npm dependencies for static preview (dist/index.html only)"'
/usr/bin/time -p bash -c 'echo "Build step: static dist already committed, nothing to compile"'
/usr/bin/time -p python3 -c "import json,os; web=os.environ.get('OPENCODE_WEB_DIR','/home/runner/work/_temp/omgithub-web'); proj=os.getcwd(); dist=os.path.join(proj,'dist'); os.makedirs(web,exist_ok=True); open(os.path.join(web,'deployment-output.json'),'w').write(json.dumps({'project':proj,'directory':dist})); print('wrote '+os.path.join(web,'deployment-output.json'))"
/usr/bin/time -p cat "$WEB_DIR/deployment-output.json"
/usr/bin/time -p bash -c 'if [ "$WEB_DIR" != "/home/runner/work/_temp/omgithub-web" ]; then mkdir -p /home/runner/work/_temp/omgithub-web; cp "$WEB_DIR/deployment-output.json" /home/runner/work/_temp/omgithub-web/deployment-output.json; fi; true'
/usr/bin/time -p echo "Serving $DIST_DIR on 0.0.0.0:$PORT (foreground)"
exec /usr/bin/time -p python3 -m http.server "$PORT" --directory "$DIST_DIR" --bind 0.0.0.0

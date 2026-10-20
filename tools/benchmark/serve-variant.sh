#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${1:-8081}"
DIST="$ROOT/benchmark/server/$PORT"
GRADLE="./gradlew -q --warning-mode=none --console=plain"

cd "$ROOT"
echo "--- building web bundle"
$GRADLE :web:jsBrowserDevelopmentWebpack

echo "--- building server"
$GRADLE :server:installDist

echo "--- snapshotting distribution for $PORT"
rm -rf "$DIST"
mkdir -p "$(dirname "$DIST")"
cp -r server/build/install/server "$DIST"

echo "--- launching on $PORT"
cd "$ROOT/server"
exec "$DIST/bin/server" \
    -port="$PORT" \
    -P:streetlight.benchmark=true
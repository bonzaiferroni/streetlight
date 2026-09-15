#!/usr/bin/env bash
set -euo pipefail

dir="$HOME/streetlight/server"
backups="$HOME/backups"

mkdir -p "$dir" "$backups"
pg_dump -h localhost -U streetlight -Fc streetlightdb > "$backups/pre-deploy-$(date +%Y%m%d-%H%M%S).dump"
ls -1t "$backups"/pre-deploy-*.dump | tail -n +6 | xargs -r rm --

tmp="$(mktemp "$dir/streetlight-server.jar.XXXXXX")"
trap 'rm -f "$tmp"' EXIT

cat > "$tmp"
chmod 644 "$tmp"
mv -f "$tmp" "$dir/streetlight-server.jar"
sudo systemctl restart streetlight
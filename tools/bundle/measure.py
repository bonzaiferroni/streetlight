#!/usr/bin/env python3
"""Measures the production web bundles and prints a markdown entry for logs/bundle.

Run after ./gradlew :web:jsBrowserProductionWebpack, from the repository root:

    python3 tools/bundle/measure.py > logs/bundle/$(date +%F).md
"""
import gzip
import json
import re
import sys
from collections import defaultdict
from datetime import date
from pathlib import Path

OUTPUT = Path("web/build/kotlin-webpack/js/productionExecutable")
TOP_GROUPS = 12

# the main entry, and each lazy chunk by the portal file it holds
MAIN = "web"
ROLES = [
    ("viewDashPortal.kt", "Dash"),
    ("viewStudioPortal.kt", "Studio"),
]

# library sources, matched against the path in the source map
LIBRARIES = [
    (r"/koin/", "lib koin"),
    (r"ktor-", "lib ktor"),
    (r"kotlinx-coroutines", "lib coroutines"),
    (r"kotlinx-serialization|Json|/json/", "lib serialization"),
    (r"/generated/gen-|kotlinx/html", "lib kotlinx.html"),
    (r"8ebe0a8279b7bad8|kotlinx-datetime", "lib datetime"),
    (r"kotlin-web/|kotlin-browser/|kotlin-js/|kotlin-wrappers", "lib wrappers"),
    (r"atomicfu|stately", "lib atomicfu"),
    (r"node_modules", "node_modules"),
    (r"^webpack:///(NATIVE_IMPLEMENTATIONS|Z/)", "unattributed"),
    (r"^webpack:///(src/kotlin|js/|common/src/generated|src/generated)", "lib stdlib"),
]


def group_of(source):
    """The module and package of one of our sources, or the library a source belongs to."""
    match = re.search(r"\.\./(web|model|koala|kampfire)/src/\w+/kotlin/(.+)/[^/]+\.kt$", source)
    if match:
        package = match.group(2).replace("/", ".")
        package = re.sub(r"^streetlight\.(web|model)\.?", "", package)
        package = re.sub(r"^(koala|kampfire)\.?", "", package)
        return f"{match.group(1)} {package}".strip()
    for pattern, name in LIBRARIES:
        if re.search(pattern, source):
            return name
    return "other"


VLQ = {c: i for i, c in enumerate("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")}


def decode_segment(text):
    values, shift, value = [], 0, 0
    for char in text:
        digit = VLQ[char]
        value += (digit & 31) << shift
        if digit & 32:
            shift += 5
        else:
            values.append(-(value >> 1) if value & 1 else value >> 1)
            shift, value = 0, 0
    return values


def bytes_by_source(js_lines, mappings):
    """The characters of generated code each source index accounts for."""
    totals = defaultdict(int)
    source = 0
    for line_text, line in zip(js_lines, mappings.split(";")):
        column = 0
        spans = []
        for segment in filter(None, line.split(",")):
            values = decode_segment(segment)
            column += values[0]
            if len(values) > 1:
                source += values[1]
                spans.append((column, source))
            else:
                spans.append((column, None))
        for index, (start, owner) in enumerate(spans):
            end = spans[index + 1][0] if index + 1 < len(spans) else len(line_text)
            if owner is not None:
                totals[owner] += end - start
    return totals


def measure(js_path):
    data = js_path.read_bytes()
    map_path = js_path.with_name(js_path.name + ".map")
    source_map = json.loads(map_path.read_text())
    sources = source_map["sources"]
    names = {s.rsplit("/", 1)[-1] for s in sources}
    if js_path.stem == MAIN:
        role = "Main"
    elif not js_path.stem.isdigit():
        role = "Standalone"
    else:
        role = next((r for file, r in ROLES if file in names), "Shared")
    groups = defaultdict(int)
    for index, size in bytes_by_source(data.decode("utf-8", "replace").split("\n"), source_map["mappings"]).items():
        groups[group_of(sources[index])] += size
    kt_count = sum(1 for s in sources if s.endswith(".kt"))
    return {
        "file": js_path.name,
        "role": role,
        "bytes": len(data),
        "gzip": len(gzip.compress(data, 9)),
        "kt": kt_count,
        "groups": sorted(groups.items(), key=lambda item: -item[1]),
    }


def kb(n):
    return f"{n / 1024:,.0f} KB"


def main():
    if not OUTPUT.exists():
        sys.exit(f"No bundles at {OUTPUT}; run ./gradlew :web:jsBrowserProductionWebpack first")
    chunks = [measure(p) for p in sorted(OUTPUT.glob("*.js"))]
    order = {"Main": 0, "Dash": 1, "Studio": 2, "Shared": 3, "Standalone": 4}
    chunks.sort(key=lambda c: (order[c["role"]], c["file"]))

    print(f"# Bundles {date.today().isoformat()}\n")
    print("## Chunks\n")
    for chunk in chunks:
        print(f"- **{chunk['role']}** `{chunk['file']}`: {kb(chunk['bytes'])}, {kb(chunk['gzip'])} gzip, {chunk['kt']} Kotlin files")
    for chunk in chunks:
        if chunk["role"] in ("Shared",):
            continue
        print(f"\n## {chunk['role']} `{chunk['file']}`\n")
        for name, size in chunk["groups"][:TOP_GROUPS]:
            print(f"- {name}: {kb(size)}")
    print("\n## Notes\n")


if __name__ == "__main__":
    main()

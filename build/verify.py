#!/usr/bin/env python3
"""Static sanity checks for modrinth.index.json (no network access)."""

import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
INDEX = ROOT / "modrinth.index.json"
PACK = ROOT / "THE-LAST-LIGHT-1.0.0.mrpack"

REQUIRED_TOP = ["formatVersion", "game", "versionId", "name", "summary", "files", "dependencies"]
ALLOWED_ENV = {"required", "optional", "unsupported"}


def main() -> int:
    errors = []
    index = json.loads(INDEX.read_text(encoding="utf-8"))

    for key in REQUIRED_TOP:
        if key not in index:
            errors.append(f"missing top-level key: {key}")

    if index.get("game") != "minecraft":
        errors.append("game must be 'minecraft'")
    if index.get("formatVersion") != 1:
        errors.append("formatVersion must be 1")

    deps = index.get("dependencies", {})
    if deps.get("minecraft") != "1.21.11":
        errors.append("minecraft dependency must be 1.21.11")
    if not deps.get("fabric-loader"):
        errors.append("fabric-loader dependency missing")

    paths = set()
    for entry in index.get("files", []):
        p = entry.get("path", "")
        if not p or p.startswith("/") or ".." in p:
            errors.append(f"bad path: {p!r}")
        if p in paths:
            errors.append(f"duplicate path: {p}")
        paths.add(p)

        hashes = entry.get("hashes", {})
        if not re.fullmatch(r"[0-9a-f]{40}", hashes.get("sha1", "")):
            errors.append(f"{p}: sha1 must be 40 hex chars")
        if not re.fullmatch(r"[0-9a-f]{128}", hashes.get("sha512", "")):
            errors.append(f"{p}: sha512 must be 128 hex chars")

        downloads = entry.get("downloads", [])
        if not downloads:
            errors.append(f"{p}: downloads array is empty")
        for url in downloads:
            if not url.startswith("https://cdn.modrinth.com/data/"):
                errors.append(f"{p}: url must be a cdn.modrinth.com url")
        if not entry.get("fileSize", 0) > 0:
            errors.append(f"{p}: fileSize missing")

        env = entry.get("env", {})
        for side in ("client", "server"):
            if env.get(side) not in ALLOWED_ENV:
                errors.append(f"{p}: env.{side} invalid: {env.get(side)}")

    # Every mod jar must live under mods/, every shader under shaderpacks/.
    for p in paths:
        if not p.startswith(("mods/", "shaderpacks/")):
            errors.append(f"unexpected file location: {p}")

    with zipfile.ZipFile(PACK) as zf:
        if zf.testzip() is not None:
            errors.append("mrpack is corrupt")
        names = zf.namelist()
        if "modrinth.index.json" not in names:
            errors.append("mrpack is missing modrinth.index.json at root")
        inner = json.loads(zf.read("modrinth.index.json"))
        if inner != index:
            errors.append("index inside mrpack differs from modrinth.index.json")

    print(f"index entries : {len(index['files'])}")
    print(f"total size    : {sum(f['fileSize'] for f in index['files']) / 1024 / 1024:.1f} MB")
    print(f"mrpack size   : {PACK.stat().st_size / 1024:.1f} KB")
    print(f"mrpack members: {len(names)}")

    if errors:
        print("\nFAILED:")
        for e in errors:
            print("  -", e)
        return 1

    print("\nOK: index is internally consistent")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

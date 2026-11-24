#!/usr/bin/env python3
"""Build THE LAST LIGHT .mrpack from the verified Modrinth file table."""

import json
import os
import shutil
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from pack_data import FILES  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
BUILD = ROOT / "build"
OVERRIDES = BUILD / "overrides"
NAME = "THE LAST LIGHT"
VERSION = "1.0.0"
MC = "1.21.11"
LOADER = "0.20.1"


def write_overrides() -> None:
    (OVERRIDES / "config").mkdir(parents=True, exist_ok=True)

    # Minimal options.txt: only the keys we actually want to pin.
    # Minecraft keeps defaults for every key that is absent.
    (OVERRIDES / "options.txt").write_text(
        "\n".join(
            [
                "renderDistance:8",
                "simulationDistance:5",
                "maxFps:120",
                "enableVsync:false",
                "fov:0.0",
                "gamma:0.0",
                "guiScale:0",
                "lang:ru_ru",
            ]
        )
        + "\n",
        encoding="utf-8",
    )

    # Iris: ship one active shader so the pack boots with the intended look.
    # Filename must match the file stored under shaderpacks/.
    (OVERRIDES / "config" / "iris.properties").write_text(
        "\n".join(
            [
                "enableShaderPack=true",
                "shaderPack=Kefir's-flashlight.zip",
            ]
        )
        + "\n",
        encoding="utf-8",
    )


def build_index() -> dict:
    files = []
    for path, url, sha1, sha512, size, env in FILES:
        files.append(
            {
                "path": path,
                "hashes": {"sha1": sha1, "sha512": sha512},
                "env": env,
                "downloads": [url],
                "fileSize": size,
            }
        )

    return {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": VERSION,
        "name": NAME,
        "summary": (
            "Fabric 1.21.11 post-apocalyptic survival: abandoned cities, "
            "graveyards, cold fog and rare horror. Tuned for GTX 1050 3 GB."
        ),
        "files": files,
        "dependencies": {"minecraft": MC, "fabric-loader": LOADER},
        "environment": "client",
        "recommendedRam": 6144,
    }


def zip_pack(index: dict) -> Path:
    out = ROOT / f"THE-LAST-LIGHT-{VERSION}.mrpack"
    if out.exists():
        out.unlink()

    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("modrinth.index.json", json.dumps(index, indent=2, ensure_ascii=False) + "\n")
        for path in sorted(OVERRIDES.rglob("*")):
            if path.is_file():
                zf.write(path, "overrides/" + path.relative_to(OVERRIDES).as_posix())
    return out


def main() -> None:
    if OVERRIDES.exists():
        shutil.rmtree(OVERRIDES)
    write_overrides()

    index = build_index()
    (ROOT / "modrinth.index.json").write_text(
        json.dumps(index, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )

    out = zip_pack(index)

    total = sum(f[4] for f in FILES)
    print(f"mods/shaders : {len(FILES)} files, {total / 1024 / 1024:.1f} MB to download")
    print(f"mrpack       : {out} ({out.stat().st_size / 1024:.1f} KB)")

    with zipfile.ZipFile(out) as zf:
        bad = zf.testzip()
        if bad:
            raise SystemExit(f"corrupt member: {bad}")
        print(f"members      : {len(zf.namelist())}")
        print("  " + "\n  ".join(zf.namelist()))


if __name__ == "__main__":
    main()

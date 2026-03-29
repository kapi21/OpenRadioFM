#!/usr/bin/env python3
"""
Auditoría de claves en app/src/main/res/values*/strings.xml frente a values/strings.xml.

Uso:
  python tools/diff_strings.py           # informe; exit 0
  python tools/diff_strings.py --strict  # exit 1 si falta alguna clave o hay extras

Desde Gradle: ./gradlew auditStrings
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
BASE_FILE = RES / "values" / "strings.xml"

STRING_NAME_RE = re.compile(r'<string\s+name="([^"]+)"')


def keys_in_file(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8")
    return set(STRING_NAME_RE.findall(text))


def nontranslatable_keys_in_base(path: Path) -> set[str]:
    """Nombres declarados con translatable=\"false\" en el fichero base."""
    out: set[str] = set()
    for line in path.read_text(encoding="utf-8").splitlines():
        if 'translatable="false"' in line and 'name="' in line:
            m = STRING_NAME_RE.search(line)
            if m:
                out.add(m.group(1))
    return out


def locale_string_files() -> list[Path]:
    if not RES.is_dir():
        return []
    out: list[Path] = []
    for p in sorted(RES.iterdir()):
        if not p.is_dir():
            continue
        name = p.name
        if not name.startswith("values"):
            continue
        if name == "values":
            continue
        f = p / "strings.xml"
        if f.is_file():
            out.append(f)
    return out


def main() -> int:
    ap = argparse.ArgumentParser(description="Compare Android strings.xml keys to default values/.")
    ap.add_argument(
        "--strict",
        action="store_true",
        help="Exit with code 1 if any locale is missing keys or has extra keys vs base.",
    )
    ap.add_argument(
        "--ignore-nontranslatable",
        action="store_true",
        help="Do not require keys that are translatable=false in default (Android falls back).",
    )
    args = ap.parse_args()

    if not BASE_FILE.is_file():
        print(f"ERROR: missing base file {BASE_FILE}", file=sys.stderr)
        return 2

    base_all = keys_in_file(BASE_FILE)
    skip = nontranslatable_keys_in_base(BASE_FILE) if args.ignore_nontranslatable else set()
    base_keys = base_all - skip

    locales = locale_string_files()
    if not locales:
        print("No values-*/strings.xml found.")
        return 0

    failed = False
    print(f"Base ({BASE_FILE.relative_to(ROOT)}): {len(base_all)} keys", end="")
    if skip:
        print(f" ({len(skip)} non-translatable skipped for check)")
    else:
        print()

    for path in locales:
        keys = keys_in_file(path)
        miss = sorted(base_keys - keys)
        extra = sorted(keys - base_keys)
        rel = path.relative_to(ROOT)
        if miss:
            failed = True
            print(f"\n{rel} MISSING ({len(miss)}):")
            for k in miss:
                print(f"  {k}")
        if extra:
            failed = True
            print(f"\n{rel} EXTRA vs base ({len(extra)}):")
            for k in extra:
                print(f"  {k}")
        if not miss and not extra:
            print(f"OK  {rel} ({len(keys)} keys)")

    if failed:
        print("\n--- audit: FAILED ---", file=sys.stderr)
        return 1 if args.strict else 0

    print("\n--- audit: all locales match required keys ---")
    return 0


if __name__ == "__main__":
    sys.exit(main())

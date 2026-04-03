#!/usr/bin/env python3
"""
Reformatea values-*/strings.xml: una etiqueta <string> por línea con indentación 4 espacios.
No modifica nombres ni textos; solo separa bloques minificados (</string><string...).
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"

# Separa cadenas pegadas: </string><string ...> o </string>    <string
SPLIT_RE = re.compile(r"</string>\s*<string")


def format_content(text: str) -> str:
    # Repetir hasta estabilizar (por si quedan varios niveles pegados en una línea)
    prev = None
    while prev != text:
        prev = text
        text = SPLIT_RE.sub("</string>\n    <string", text)

    lines: list[str] = []
    for line in text.splitlines():
        stripped = line.lstrip()
        if stripped.startswith("<string") and not line.startswith("    "):
            lines.append("    " + stripped)
        else:
            lines.append(line)

    out = "\n".join(lines)
    if text.endswith("\n"):
        out += "\n"
    return out


def main() -> int:
    paths = sorted(RES.glob("values-*/strings.xml"))
    if not paths:
        print("No locale strings.xml found", file=sys.stderr)
        return 1
    for p in paths:
        raw = p.read_text(encoding="utf-8")
        new = format_content(raw)
        if new != raw:
            p.write_text(new, encoding="utf-8", newline="\n")
            print(f"formatted: {p.relative_to(ROOT)}")
        else:
            print(f"unchanged: {p.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

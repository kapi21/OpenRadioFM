"""Extrae el PNG embebido en v5.svg y genera mipmaps para ic_launcher."""
import base64
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SVG = ROOT / "v5.svg"
OUT_PNG = ROOT / "build" / "tooling" / "v5_extracted.png"


def main():
    text = SVG.read_text(encoding="utf-8")
    m = re.search(r'data:image/png;base64,([^"]+)', text)
    if not m:
        raise SystemExit("No se encontró PNG base64 en v5.svg")
    b64 = m.group(1).replace("&#10;", "").replace("\n", "")
    raw = base64.b64decode(b64)
    OUT_PNG.parent.mkdir(parents=True, exist_ok=True)
    OUT_PNG.write_bytes(raw)
    print(f"OK: {OUT_PNG} ({len(raw)} bytes)")


if __name__ == "__main__":
    main()

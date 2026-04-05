"""Genera ic_launcher / ic_launcher_round en todas las densidades desde v5_extracted.png."""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
# Salida intermedia de extract_v5_png.py (carpeta ignorada por git).
SRC = ROOT / "build" / "tooling" / "v5_extracted.png"
RES = ROOT / "app" / "src" / "main" / "res"

# Misma resolución en px que xxxhdpi (192×192) en todas las carpetas mipmap:
# el sistema escala al slot del launcher; así mdpi/hdpi/etc. no usan bitmaps más pequeños.
XXXHDPI_PX = 192

MIPMAP_FOLDERS = (
    "mipmap-mdpi",
    "mipmap-hdpi",
    "mipmap-xhdpi",
    "mipmap-xxhdpi",
    "mipmap-xxxhdpi",
)


def main():
    im = Image.open(SRC).convert("RGBA")
    thumb = im.resize((XXXHDPI_PX, XXXHDPI_PX), Image.Resampling.LANCZOS)
    for folder in MIPMAP_FOLDERS:
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        for name in (
            "ic_launcher.png",
            "ic_launcher_round.png",
            "ic_launcher_foreground.png",
            "ic_launcher_round_foreground.png",
        ):
            path = out_dir / name
            thumb.save(path, "PNG", optimize=True)
            print(path.relative_to(ROOT))


if __name__ == "__main__":
    main()

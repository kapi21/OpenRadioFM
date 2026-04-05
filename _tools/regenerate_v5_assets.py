"""Regenera mipmaps y nodpi desde v5.svg (PNG embebido)."""
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    subprocess.check_call([sys.executable, str(ROOT / "_tools" / "extract_v5_png.py")])
    subprocess.check_call([sys.executable, str(ROOT / "_tools" / "build_launcher_from_v5.py")])
    import shutil

    shutil.copy2(
        ROOT / "build" / "tooling" / "v5_extracted.png",
        ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi" / "ic_app_logo.png",
    )
    print("OK: drawable-nodpi/ic_app_logo.png actualizado")


if __name__ == "__main__":
    main()

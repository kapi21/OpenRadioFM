import os
import stat
import sys
import zipfile


def _is_executable_script(rel_path: str) -> bool:
    p = rel_path.replace("\\", "/")
    return p == "META-INF/com/google/android/update-binary" or p.endswith(".sh")


def _write_dir_entry(zf: zipfile.ZipFile, rel_dir: str) -> None:
    rel_dir = rel_dir.replace("\\", "/").rstrip("/") + "/"
    zi = zipfile.ZipInfo(rel_dir)
    zi.create_system = 3  # Unix
    zi.external_attr = (0o755 | stat.S_IFDIR) << 16
    zf.writestr(zi, b"")


def _write_file_entry(zf: zipfile.ZipFile, abs_path: str, rel_path: str) -> None:
    rel_path = rel_path.replace("\\", "/")
    mode = 0o755 if _is_executable_script(rel_path) else 0o644
    zi = zipfile.ZipInfo(rel_path)
    zi.create_system = 3  # Unix
    zi.external_attr = (mode | stat.S_IFREG) << 16
    with open(abs_path, "rb") as f:
        zf.writestr(zi, f.read())


def zip_magisk_module(src_dir: str, dst_zip: str) -> None:
    src_dir = os.path.abspath(src_dir)
    if not os.path.isdir(src_dir):
        raise SystemExit(f"[ERROR] src_dir no existe o no es carpeta: {src_dir}")

    dst_zip = os.path.abspath(dst_zip)
    os.makedirs(os.path.dirname(dst_zip), exist_ok=True)
    if os.path.exists(dst_zip):
        os.remove(dst_zip)

    with zipfile.ZipFile(dst_zip, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
        for root, dirs, files in os.walk(src_dir):
            # directorios
            for d in sorted(dirs):
                abs_d = os.path.join(root, d)
                rel_d = os.path.relpath(abs_d, src_dir)
                _write_dir_entry(zf, rel_d)

            # ficheros
            for fn in sorted(files):
                abs_f = os.path.join(root, fn)
                rel_f = os.path.relpath(abs_f, src_dir)
                _write_file_entry(zf, abs_f, rel_f)


def main(argv: list[str]) -> int:
    if len(argv) != 3:
        print("Uso: zip_magisk.py <src_dir> <dst_zip>")
        return 2
    zip_magisk_module(argv[1], argv[2])
    print(f"[OK] ZIP Magisk generado: {os.path.abspath(argv[2])}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))


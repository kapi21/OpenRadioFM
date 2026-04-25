import argparse
import os
from pathlib import Path

import brotli
from ext4 import Volume
from ext4.inode import Directory


def brotli_decompress_file(src: Path, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    data = src.read_bytes()
    dst.write_bytes(brotli.decompress(data))


def sdat2img(transfer_list: Path, new_dat: Path, out_img: Path) -> None:
    """
    Minimal sdat2img implementation for OTA *.transfer.list + *.new.dat.
    Supports the formats seen in update_auto zips (Android sparse block images).
    """
    lines = [ln.strip() for ln in transfer_list.read_text(encoding="utf-8", errors="ignore").splitlines() if ln.strip()]
    version = int(lines[0])
    if version not in (2, 3, 4):
        raise RuntimeError(f"transfer.list version no soportada: {version}")

    total_blocks = int(lines[1])
    # V2/V3 typically: version, total_blocks, stashes, max_stash, commands...
    # V4 can add more header fields; commands always start after 4th line.
    idx = 2
    if version >= 2:
        # stashes and max stash entries exist in many builds
        if idx < len(lines) and lines[idx].isdigit():
            idx += 1
        if idx < len(lines) and lines[idx].isdigit():
            idx += 1

    block_size = 4096
    out_img.parent.mkdir(parents=True, exist_ok=True)
    with out_img.open("wb") as out:
        out.truncate(total_blocks * block_size)

        with new_dat.open("rb") as dat:
            for ln in lines[idx:]:
                parts = ln.split()
                if not parts:
                    continue
                cmd = parts[0]
                if cmd not in ("new", "erase", "zero"):
                    # ignore stash/free commands, etc.
                    continue

                if len(parts) < 2:
                    raise RuntimeError(f"Línea inválida en transfer.list: {ln}")
                rng = [int(x) for x in parts[1].split(",") if x]
                if not rng:
                    continue
                count = rng[0]
                # En transfer.list, el primer entero es el número de enteros que siguen (no el nº de pares).
                # Ej: "erase 2,647581,648533" => 2 enteros => 1 par (647581,648533)
                if count + 1 != len(rng):
                    raise RuntimeError(f"Rango inválido en transfer.list: {ln}")
                if count % 2 != 0:
                    raise RuntimeError(f"Rango con número impar de elementos: {ln}")

                pairs = count // 2
                for i in range(pairs):
                    start = rng[1 + i * 2]
                    end = rng[1 + i * 2 + 1]
                    blocks = end - start
                    if blocks < 0:
                        raise RuntimeError(f"Bloques negativos en range: {ln}")

                    if cmd == "new":
                        chunk = dat.read(blocks * block_size)
                        if len(chunk) != blocks * block_size:
                            raise RuntimeError("new.dat terminó antes de lo esperado")
                        out.seek(start * block_size)
                        out.write(chunk)
                    else:
                        # erase/zero: already zero-filled by truncate
                        continue


def extract_from_ext4(img_path: Path, out_dir: Path) -> list[Path]:
    out_dir.mkdir(parents=True, exist_ok=True)
    extracted: list[Path] = []

    with img_path.open("rb") as f:
        vol = Volume(f)

        def _try_extract(path: str) -> bool:
            try:
                inode = vol.inode_at(path)
            except Exception:
                return False
            dst = out_dir / Path(path).name
            with inode.open("rb") as rf, dst.open("wb") as wf:
                wf.write(rf.read())
            extracted.append(dst)
            return True

        # Try common candidate paths for QF K706 (system partition usually has /app,/priv-app)
        candidates = [
            # Algunos firmwares exponen /system/ dentro de la partición system.img
            "/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk",
            "/system/app/QF_FMRadioExt/QF_FMRadioExt.apk",
            "/system/priv-app/FMRadio/FMRadio.apk",
            "/system/app/FMRadio/FMRadio.apk",
            "/system/priv-app/Radio/Radio.apk",
            "/system/app/Radio/Radio.apk",
            # Otros system.img van "planos" sin prefijo /system
            "/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk",
            "/app/QF_FMRadioExt/QF_FMRadioExt.apk",
            "/priv-app/FMRadio/FMRadio.apk",
            "/app/FMRadio/FMRadio.apk",
            "/priv-app/Radio/Radio.apk",
            "/app/Radio/Radio.apk",
        ]

        for p in candidates:
            _try_extract(p)

        if extracted:
            return extracted

        # Fallback: walk filesystem and find APKs that look like FM/Radio packages
        def walk(dir_inode: Directory, prefix: str, depth: int) -> None:
            if depth <= 0:
                return
            for dirent, ft in dir_inode.opendir():
                name_b = getattr(dirent, "name", b"")
                if not name_b or name_b in (b".", b".."):
                    continue
                name = name_b.decode("utf-8", errors="ignore")
                path = f"{prefix}/{name}" if prefix else f"/{name}"
                try:
                    child = vol.inode_at(path)
                except Exception:
                    continue
                if isinstance(child, Directory):
                    walk(child, path, depth - 1)
                else:
                    low = name.lower()
                    if low.endswith(".apk") and any(k in low for k in ("fm", "radio", "fmradio", "qf_fmradioext")):
                        _try_extract(path)

        walk(vol.root, "", depth=6)

    return extracted


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--update-auto-dir",
        required=True,
        help="Ruta a carpeta update_auto (descomprimida). Ej: .../ROOT JAMES/update_auto",
    )
    ap.add_argument(
        "--partition",
        choices=["system", "product", "vendor"],
        default="system",
        help="Partición a extraer (system/product/vendor).",
    )
    ap.add_argument(
        "--out-dir",
        required=True,
        help="Carpeta de salida para la APK extraída.",
    )
    args = ap.parse_args()

    update_dir = Path(args.update_auto_dir)
    part = args.partition
    out_dir = Path(args.out_dir)

    tl = update_dir / f"{part}.transfer.list"
    dat_br = update_dir / f"{part}.new.dat.br"
    if not tl.is_file() or not dat_br.is_file():
        raise SystemExit(f"No se encontraron {tl.name} y/o {dat_br.name} en {update_dir}")

    work = out_dir / "_work"
    dat = work / f"{part}.new.dat"
    img = work / f"{part}.img"

    print(f"[INFO] Decompress {dat_br.name} -> {dat.name}")
    brotli_decompress_file(dat_br, dat)

    print(f"[INFO] Convert {tl.name} + {dat.name} -> {img.name}")
    sdat2img(tl, dat, img)

    print(f"[INFO] Extract from {img.name}")
    extracted = extract_from_ext4(img, out_dir)
    if not extracted:
        print("[WARN] No se encontraron APKs candidatos en la imagen.")
        return 1

    for p in extracted:
        print(f"[OK] Extracted: {p}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())


#!/usr/bin/env python3
"""Insert selective_scan_title, dialog_btn_save, dialog_btn_restore_original if missing."""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"

# (title, save, restore) per folder name; English fallback for unknown
LOCALE: dict[str, tuple[str, str, str]] = {
    "values": ("Escaneando emisoras…", "Guardar", "Restaurar original"),
    "values-en": ("Scanning stations…", "Save", "Restore original"),
    "values-de": ("Sender werden gescannt…", "Speichern", "Original wiederherstellen"),
    "values-fr": ("Balayage des stations…", "Enregistrer", "Restaurer l’original"),
    "values-it": ("Scansione stazioni…", "Salva", "Ripristina originale"),
    "values-pt": ("A procurar estações…", "Guardar", "Restaurar original"),
    "values-ro": ("Se scanează stațiile…", "Salvează", "Restaurează originalul"),
    "values-hu": ("Állomások keresése…", "Mentés", "Eredeti visszaállítása"),
    "values-ja": ("局をスキャン中…", "保存", "元に戻す"),
    "values-ru": ("Сканирование станций…", "Сохранить", "Восстановить оригинал"),
    "values-uk": ("Сканування станцій…", "Зберегти", "Відновити оригінал"),
    "values-sr": ("Skeniranje stanica…", "Sačuvaj", "Vrati original"),
    "values-zh": ("正在扫描电台…", "保存", "恢复原始名称"),
}


def block_for(folder: str) -> str:
    t, s, r = LOCALE.get(folder, LOCALE["values-en"])
    return (
        f'    <string name="selective_scan_title">{t}</string>\n'
        f'    <string name="dialog_btn_save">{s}</string>\n'
        f'    <string name="dialog_btn_restore_original">{r}</string>\n'
    )


def main() -> int:
    needle = 'name="selective_scan_freq_placeholder"'
    for p in sorted(RES.glob("values*/strings.xml")):
        folder = p.parent.name
        text = p.read_text(encoding="utf-8")
        if 'name="selective_scan_title"' in text:
            print(f"skip: {p.relative_to(ROOT)}")
            continue
        if needle not in text:
            print(f"WARN: no selective_scan_freq_placeholder in {p}", file=sys.stderr)
            continue
        insert = block_for(folder)
        text = text.replace(f"    <string {needle}", insert + f"    <string {needle}", 1)
        p.write_text(text, encoding="utf-8", newline="\n")
        print(f"updated: {p.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

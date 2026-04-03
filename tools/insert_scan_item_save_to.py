#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
TRANS = {
    "values-de": "Speichern unter:",
    "values-fr": "Enregistrer dans :",
    "values-it": "Salva in:",
    "values-pt": "Guardar em:",
    "values-ro": "Salvează la:",
    "values-hu": "Mentés ide:",
    "values-ja": "保存先:",
    "values-ru": "Сохранить в:",
    "values-uk": "Зберегти в:",
    "values-sr": "Sačuvaj u:",
    "values-zh": "保存到：",
}

for p in sorted(ROOT.glob("values*/strings.xml")):
    folder = p.parent.name
    text = p.read_text(encoding="utf-8")
    if 'name="scan_item_save_to"' in text:
        print(f"skip: {p}")
        continue
    if 'name="scan_rds_searching"' not in text:
        print(f"WARN: {p}")
        continue
    t = TRANS.get(folder, "Save to:")
    ins = f'    <string name="scan_item_save_to">{t}</string>'
    new, n = re.subn(
        r'(\s*)(<string name="scan_rds_searching">[^<]+</string>)',
        r"\1\2\n\1" + ins,
        text,
        count=1,
    )
    if n != 1:
        print(f"FAIL: {p}")
        continue
    p.write_text(new, encoding="utf-8", newline="\n")
    print(f"ok: {p}")

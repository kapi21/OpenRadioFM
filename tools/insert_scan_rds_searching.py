#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
TRANS = {
    "values-de": "RDS wird gesucht…",
    "values-fr": "Recherche RDS…",
    "values-it": "Ricerca RDS…",
    "values-pt": "A procurar RDS…",
    "values-ro": "Se caută RDS…",
    "values-hu": "RDS keresése…",
    "values-ja": "RDSを検索中…",
    "values-ru": "Поиск RDS…",
    "values-uk": "Пошук RDS…",
    "values-sr": "Traženje RDS…",
    "values-zh": "正在搜索RDS…",
}
NEEDLE = 'name="selective_scan_freq_placeholder"'

for p in sorted(ROOT.glob("values*/strings.xml")):
    folder = p.parent.name
    text = p.read_text(encoding="utf-8")
    if 'name="scan_rds_searching"' in text:
        print(f"skip: {p}")
        continue
    if NEEDLE not in text:
        print(f"WARN: {p}")
        continue
    t = TRANS.get(folder, "Searching for RDS…")
    ins = f'    <string name="scan_rds_searching">{t}</string>\n'
    text = text.replace(f"    <string {NEEDLE}", ins + f"    <string {NEEDLE}", 1)
    p.write_text(text, encoding="utf-8", newline="\n")
    print(f"ok: {p}")

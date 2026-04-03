from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
for p in ROOT.glob("values-*/strings.xml"):
    t = p.read_text(encoding="utf-8")
    old = "\n<string name=\"save_load_section_actions\""
    new = "\n    <string name=\"save_load_section_actions\""
    if old in t:
        p.write_text(t.replace(old, new), encoding="utf-8")
        print("fixed", p.name)

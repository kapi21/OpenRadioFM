import argparse
import json
import os
import sys
from typing import Any, Dict, Iterable, List, Optional, Set

import requests


KEEP_COUNTRIES_DEFAULT = "ES,RU,IT,GR,RO,HU,PT,DE,PL"


def _env(name: str) -> str:
    v = os.environ.get(name, "").strip()
    if not v:
        raise SystemExit(f"Missing env var: {name}")
    return v


def iter_rows(
    json_path: str,
    keep_countries: Set[str],
) -> Iterable[Dict[str, Any]]:
    data = json.load(open(json_path, "r", encoding="utf-8"))
    if not isinstance(data, list):
        raise SystemExit("Expected top-level JSON array")

    seen_streams: Set[str] = set()
    for r in data:
        if not isinstance(r, dict):
            continue
        cc = (r.get("iso_3166_1") or "").strip().upper()
        if cc not in keep_countries:
            continue

        name = (r.get("name") or "").strip()
        stream_url = (r.get("url_stream") or "").strip()
        if not name or not stream_url:
            continue

        # Avoid duplicates inside the same import run (and within a batch),
        # which can break Postgres ON CONFLICT DO UPDATE semantics.
        if stream_url in seen_streams:
            continue
        seen_streams.add(stream_url)

        favicon = (r.get("url_favicon") or "").strip() or None
        lat = r.get("geo_lat")
        lon = r.get("geo_long")

        # Map to the SAME columns as public.stations
        yield {
            "pi_code": None,
            "ps_name": name,
            # Radio-Browser dataset has no FM frequency -> sentinel 0
            "frequency": 0,
            "logo_url": favicon,
            "stream_url": stream_url,
            "latitude": lat,
            "longitude": lon,
            "hw_model": None,
            "device_id": None,
            "country_code": cc,
        }


def chunked(it: Iterable[Dict[str, Any]], size: int) -> Iterable[List[Dict[str, Any]]]:
    buf: List[Dict[str, Any]] = []
    for x in it:
        buf.append(x)
        if len(buf) >= size:
            yield buf
            buf = []
    if buf:
        yield buf


def post_batch(
    session: requests.Session,
    rest_url: str,
    api_key: str,
    table: str,
    on_conflict: Optional[str],
    batch: List[Dict[str, Any]],
) -> None:
    params = {}
    if on_conflict:
        params["on_conflict"] = on_conflict

    headers = {
        "apikey": api_key,
        "Authorization": f"Bearer {api_key}",
        # Upsert semantics (requires UNIQUE index on on_conflict columns)
        "Prefer": "resolution=merge-duplicates,return=minimal",
        "Content-Type": "application/json",
    }

    resp = session.post(
        f"{rest_url}/{table}",
        params=params,
        headers=headers,
        data=json.dumps(batch, ensure_ascii=False).encode("utf-8"),
        timeout=120,
    )
    # PostgREST may return 200/201/204 depending on Prefer/representation.
    if resp.status_code not in (200, 201, 204):
        raise SystemExit(f"Insert failed: HTTP {resp.status_code}: {resp.text[:4000]}")


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Import Radio-Browser snapshot JSON into Supabase (admin/service role recommended)."
    )
    ap.add_argument(
        "--json",
        default="radiobrowser_stations_20260116_234403.json",
        help="Path to the Radio-Browser stations JSON snapshot.",
    )
    ap.add_argument(
        "--table",
        default="stations_radiobrowser",
        help="Target table (default: stations_radiobrowser).",
    )
    ap.add_argument(
        "--countries",
        default=KEEP_COUNTRIES_DEFAULT,
        help=f"Comma-separated ISO3166-1 country codes (default: {KEEP_COUNTRIES_DEFAULT}).",
    )
    ap.add_argument(
        "--batch-size",
        type=int,
        default=500,
        help="Rows per HTTP request (default: 500).",
    )
    ap.add_argument(
        "--on-conflict",
        default="stream_url",
        help="Upsert conflict target (requires UNIQUE index). Default: stream_url",
    )

    args = ap.parse_args()

    supabase_url = _env("SUPABASE_URL").rstrip("/")
    # Use SERVICE_ROLE for admin import (bypasses RLS); anon key may fail if RLS blocks inserts.
    service_key = _env("SUPABASE_SERVICE_ROLE_KEY")

    keep = {c.strip().upper() for c in args.countries.split(",") if c.strip()}
    if not keep:
        raise SystemExit("No countries selected")

    rest_url = f"{supabase_url}/rest/v1"

    s = requests.Session()
    total = 0
    for batch in chunked(iter_rows(args.json, keep), args.batch_size):
        post_batch(
            session=s,
            rest_url=rest_url,
            api_key=service_key,
            table=args.table,
            on_conflict=args.on_conflict,
            batch=batch,
        )
        total += len(batch)
        print(f"Imported {total} rows...", flush=True)

    print(f"Done. Imported {total} rows into {args.table}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())


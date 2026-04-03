#!/usr/bin/env python3
"""
Inserta el bloque de strings de toasts/UI (mismo texto que values-en) en cada
values-*/strings.xml que aún no tenga la clave save_load_section_actions.
Ejecutar tras añadir claves en values/ y values-en.
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"

# Mismo contenido que en values-en (inglés); locales sin traducción propia
# reciben inglés en lugar de español mezclado en la UI.
BLOCK = r"""
    <string name="save_load_section_actions">Available actions</string>
    <string name="save_load_save_desc_detail">Create .fav backup in /RadioLogos</string>
    <string name="save_load_load_desc_detail">Restore backup from .fav</string>
    <string name="save_load_section_danger">Danger zone</string>
    <string name="save_load_delete_all_title">Delete all favorites</string>
    <string name="save_load_delete_all_sub">Remove all saved stations</string>
    <string name="save_load_clear_history_title">Clear history</string>
    <string name="save_load_clear_history_sub">Clear recent listening history</string>
    <string name="credits_developed_by">Developed by Jimmy80</string>
    <string name="btn_ok">OK</string>
    <string name="selective_scan_freq_placeholder">---.- MHz</string>
    <string name="selective_scan_waiting_rds">Waiting for RDS…</string>
    <string name="selective_scan_btn_finish">Finish</string>
    <string name="selective_scan_btn_next">Next</string>

    <string name="toast_station_name_saved">Name saved: %1$s</string>
    <string name="toast_station_name_restored">Name restored</string>
    <string name="toast_logos_online_on_1">Online logos: on (Internet required)</string>
    <string name="toast_logos_online_on_2">The central database will be queried for HD logos</string>
    <string name="toast_logos_online_off">Online logos: off</string>
    <string name="toast_relief_hd_on">HD relief: on</string>
    <string name="toast_relief_hd_off">HD relief: off</string>
    <string name="toast_night_mode_auto_on">Automatic night mode: on</string>
    <string name="toast_night_logos_tint_on">Tint logos in night mode</string>
    <string name="toast_night_logos_tint_off">Logos without tint in night mode</string>
    <string name="toast_contrib_on">Community contribution enabled</string>
    <string name="toast_contrib_off">Community contribution disabled</string>
    <string name="toast_logo_mode">Logo mode: %1$s</string>
    <string name="toast_logo_provider">Logo provider: %1$s</string>
    <string name="toast_steering_mode">Steering NEXT/PREV: %1$s</string>
    <string name="toast_skin_colon">Skin: %1$s</string>
    <string name="toast_skin_night_mode">Skin: night mode</string>
    <string name="toast_history_deleted_full">History has been cleared</string>
    <string name="toast_easter_egg">Easter egg activated!</string>

    <string name="toast_no_other_favorites">No other saved favorites</string>
    <string name="toast_returning_fm">Returning to FM radio…</string>
    <string name="toast_stream_searching">Looking for streaming link…</string>
    <string name="toast_stream_starting">Starting online radio…</string>
    <string name="toast_stream_unavailable">Streaming not available for this station</string>
    <string name="toast_station_load_error">Error loading station data</string>
    <string name="toast_station_cache_sync">Station cache cleared. Syncing…</string>
    <string name="toast_hardware_colon">Hardware: %1$s</string>
    <string name="toast_app_not_installed">App not installed: %1$s</string>
    <string name="toast_permissions_granted">Permissions granted</string>
    <string name="toast_storage_permission_needed">Storage permission is required to save favorites.</string>
    <string name="toast_phone_mute_on_call">Calls: radio will mute automatically</string>
    <string name="toast_phone_no_permission_fm">Without phone permission, FM may keep playing during calls</string>
    <string name="toast_layout_v3">Layout: V3 (horizontal)</string>
    <string name="toast_layout_simple">Layout: simple (minimal)</string>
    <string name="toast_layout_v2">Layout: V2 (vertical)</string>

    <string name="toast_eq_unavailable">Equalizer unavailable (radio engine not started)</string>
    <string name="toast_radio_engine_not_started">Radio engine not started</string>
    <string name="toast_no_gps_app">No maps/GPS app found</string>
    <string name="toast_basic_no_engineering">Basic mode: engineering menu unavailable</string>

    <string name="toast_autoscan_stopped">AutoScan stopped</string>
    <string name="toast_autoscan_slow">Slow AutoScan: searching stations… (tap again to stop)</string>
    <string name="toast_autoscan_start_failed">Could not start AutoScan</string>
    <string name="toast_autoscan_finished">AutoScan finished (%1$d presets)</string>
    <string name="toast_autoscan_band_presets">AutoScan → %1$s · presets 1–18</string>
    <string name="toast_presets_overwritten">Presets overwritten (%1$d stations).</string>

    <string name="toast_back_standard_layout">Returning to standard mode…</string>
    <string name="toast_recovering_fm_audio">Recovering FM audio…</string>
    <string name="toast_hw_eq_unsupported">Hardware EQ not supported on this device</string>
    <string name="toast_saved_to_slot">Saved to preset %1$d</string>
"""


def main() -> int:
    for p in sorted(RES.glob("values-*/strings.xml")):
        if p.parent.name == "values-en":
            continue
        text = p.read_text(encoding="utf-8")
        if 'name="save_load_section_actions"' in text:
            print(f"skip (already has block): {p.relative_to(ROOT)}")
            continue
        if "</resources>" not in text:
            print(f"ERROR: no </resources> in {p}", file=sys.stderr)
            return 1
        text = text.replace("</resources>", BLOCK.strip() + "\n</resources>", 1)
        p.write_text(text, encoding="utf-8", newline="\n")
        print(f"updated: {p.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Fetch real CC0 ambient recordings from Freesound and install them as app assets.

Downloads the HQ ogg preview of a hand-vetted, CC0-licensed clip for each of the 15
non-noise tracks, normalizes it with ffmpeg to a consistent 44.1 kHz stereo .ogg with a
short loop crossfade, and writes it to app/src/main/assets/sounds/<cat>/<name>.ogg.

White/pink/brown noise are NOT fetched — they stay synthesized (exact and cleaner).

Usage: FREESOUND_TOKEN=xxxx python3 scripts/fetch_sounds.py
"""
import json
import os
import subprocess
import sys
import tempfile
import urllib.parse
import urllib.request

TOKEN = os.environ.get("FREESOUND_TOKEN")
if not TOKEN:
    sys.exit("Set FREESOUND_TOKEN env var")

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets", "sounds")
MAX_SECONDS = 60        # cap clip length to control APK size
XFADE = 2.0             # loop crossfade seconds (blends end into start for seamless loop)

# (category, name, freesound_id) — each hand-vetted, CC0, matches the track's display name.
TRACKS = [
    ("nature",  "rain_light",     818347),  # RAIN Late Spring, Residential Area
    ("nature",  "rain_heavy",     435221),  # Heavy rain, clear recording
    ("nature",  "thunder_rumble", 158691),  # Distant Thunder and Rain
    ("nature",  "ocean_waves",    550915),  # Water Beach Ocean Waves Constant Splashing
    ("nature",  "forest_birds",   462136),  # forest-bird
    ("nature",  "river_stream",   433589),  # Stream River Water Up Close
    ("ambient", "campfire",       588401),  # Campfire Close Crackling Sticks
    ("ambient", "soft_wind",      523374),  # Open field winds summer ambience
    ("ambient", "coffee_shop",    540299),  # Coffee shop Ambience
    ("ambient", "train_tracks",   455045),  # Train Rumble and Rattle
    ("ambient", "fan_electric",   530200),  # SERVICE FAN 1
    ("sleep",   "singing_bowl",   398285),  # Tibetan Singing Bowl
    ("sleep",   "deep_drone",     810373),  # Bass Drone Pad
    ("sleep",   "night_crickets", 522298),  # Crickets At Night - Clean sound
    ("sleep",   "heartbeat_slow", 332816),  # heartbeat-40bpm
]


def fetch_meta(sound_id):
    params = urllib.parse.urlencode({
        "fields": "id,name,username,url,license,duration,previews",
        "token": TOKEN,
    })
    url = f"https://freesound.org/apiv2/sounds/{sound_id}/?{params}"
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.load(r)


def download(url, dest):
    req = urllib.request.Request(url, headers={"User-Agent": "AuraSound/1.0"})
    with urllib.request.urlopen(req, timeout=120) as r, open(dest, "wb") as f:
        f.write(r.read())


def normalize(src, dst, duration):
    # Clip to MAX_SECONDS, then make it loop seamlessly: split off the last XFADE
    # seconds and crossfade it over the start, so the loop point is inaudible.
    clip = min(duration, MAX_SECONDS)
    xf = XFADE if clip > 2 * XFADE else 0
    # libopus in an .ogg container (Homebrew ffmpeg has no libvorbis); media3/ExoPlayer
    # decodes Opus-in-Ogg natively. 48 kHz is Opus's native rate.
    common = ["-ar", "48000", "-ac", "2", "-c:a", "libopus", "-b:a", "112k"]
    if xf:
        body = clip - xf
        filt = (
            f"[0:a]atrim=0:{body},asetpts=N/SR/TB[a];"
            f"[0:a]atrim={body}:{clip},asetpts=N/SR/TB[b];"
            f"[b][a]acrossfade=d={xf}:c1=tri:c2=tri,"
            f"loudnorm=I=-18:TP=-2:LRA=11"
        )
        cmd = ["ffmpeg", "-y", "-loglevel", "error", "-t", str(clip), "-i", src,
               "-filter_complex", filt, *common, dst]
    else:
        cmd = ["ffmpeg", "-y", "-loglevel", "error", "-t", str(clip), "-i", src,
               "-af", "loudnorm=I=-18:TP=-2:LRA=11", *common, dst]
    subprocess.run(cmd, check=True)


def main():
    credits, failures = [], []
    with tempfile.TemporaryDirectory() as tmp:
        for cat, name, sid in TRACKS:
            try:
                meta = fetch_meta(sid)
                ogg = meta["previews"]["preview-hq-ogg"]
                raw = os.path.join(tmp, f"{name}.src.ogg")
                download(ogg, raw)
                out_dir = os.path.join(ASSETS, cat)
                os.makedirs(out_dir, exist_ok=True)
                normalize(raw, os.path.join(out_dir, f"{name}.ogg"), meta["duration"])
                credits.append(
                    f'{cat}/{name}.ogg  <-  "{meta["name"]}" by {meta["username"]} '
                    f'(CC0)  fs#{sid}  {meta["url"]}'
                )
                print(f"OK   {cat}/{name}.ogg  <- fs#{sid} ({meta['duration']:.0f}s)")
            except Exception as e:
                failures.append((cat, name, str(e)))
                print(f"FAIL {cat}/{name}: {e}", file=sys.stderr)

    with open(os.path.join(ASSETS, "CREDITS.txt"), "w") as f:
        f.write("AuraSound — audio sources\n")
        f.write("Nature/ambient/sleep tracks: Creative Commons 0 (public domain) via freesound.org.\n")
        f.write("Focus tracks (white/pink/brown noise): procedurally synthesized at build time.\n")
        f.write("=" * 72 + "\n\n")
        f.write("\n".join(credits) + "\n")

    print(f"\nDone: {len(credits)} ok, {len(failures)} failed")
    if failures:
        for cat, name, e in failures:
            print(f"  - {cat}/{name}: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Generate legacy launcher PNGs (API 24-25 fallback) for AuraSound.

Adaptive icons (API 26+) are vector drawables in res/drawable + mipmap-anydpi-v26.
This script renders matching raster icons for the density buckets so older devices
show the same AuraSound "sound ripple" mark instead of the default placeholder.

Pure-PIL implementation (no SVG toolchain required). The design is drawn in a
108x108 logical space (matching the vector viewport), supersampled, then resized
down per density bucket for clean anti-aliasing.
"""
import os
from PIL import Image, ImageDraw

RES = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")

# 48dp baseline launcher icon scaled per density bucket.
DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

VP = 108           # logical viewport
SS = 8             # supersample factor -> render at 864px then downscale
R = VP * SS
C = 54 * SS        # center


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def grad_stops(stops, t):
    """stops: list of (offset, (r,g,b[,a])). Returns interpolated color."""
    if t <= stops[0][0]:
        return stops[0][1]
    if t >= stops[-1][0]:
        return stops[-1][1]
    for i in range(1, len(stops)):
        o0, c0 = stops[i - 1]
        o1, c1 = stops[i]
        if t <= o1:
            return lerp(c0, c1, (t - o0) / (o1 - o0))
    return stops[-1][1]


def render():
    img = Image.new("RGBA", (R, R), (0, 0, 0, 0))
    px = img.load()

    bg = [(0.0, (0x1A, 0x1A, 0x2E)), (0.55, (0x2A, 0x1B, 0x4E)), (1.0, (0x3D, 0x2A, 0x6B))]
    aura = (0x55, 0x9C, 0xF0)  # blended aura tint

    for y in range(R):
        for x in range(R):
            # diagonal background gradient
            t = (x + y) / (2 * R)
            r, g, b = grad_stops(bg, t)
            # radial aura glow added on top
            dx, dy = x - C, y - C
            dist = (dx * dx + dy * dy) ** 0.5 / SS  # back to logical units
            if dist < 46:
                a = max(0.0, 1 - dist / 46) * 0.30
                r, g, b = lerp((r, g, b), aura, a)
            px[x, y] = (r, g, b, 255)

    draw = ImageDraw.Draw(img)

    def ring(radius, color, alpha, width):
        rr = radius * SS
        w = max(1, round(width * SS))
        layer = Image.new("RGBA", (R, R), (0, 0, 0, 0))
        ld = ImageDraw.Draw(layer)
        ld.ellipse([C - rr, C - rr, C + rr, C + rr],
                   outline=color + (round(alpha * 255),), width=w)
        img.alpha_composite(layer)

    ring(31, (0xFF, 0xFF, 0xFF), 0.25, 3)
    ring(22, (0xB8, 0xC8, 0xFF), 0.50, 3.5)
    ring(13, (0xDC, 0xE4, 0xFF), 1.0, 4)

    # glowing core: radial white -> #7FB0FF
    core = [(0.0, (0xFF, 0xFF, 0xFF)), (1.0, (0x7F, 0xB0, 0xFF))]
    cr = 7 * SS
    for y in range(C - cr, C + cr + 1):
        for x in range(C - cr, C + cr + 1):
            dx, dy = x - C, y - C
            d = (dx * dx + dy * dy) ** 0.5
            if d <= cr:
                img.putpixel((x, y), grad_stops(core, d / cr) + (255,))
    return img


def round_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def main():
    base = render()
    for bucket, size in DENSITIES.items():
        d = os.path.join(RES, f"mipmap-{bucket}")
        sq = base.resize((size, size), Image.LANCZOS)
        sq.save(os.path.join(d, "ic_launcher.png"))

        rnd = sq.copy()
        rnd.putalpha(round_mask(size))
        rnd.save(os.path.join(d, "ic_launcher_round.png"))

        for name in ("ic_launcher", "ic_launcher_round"):
            webp = os.path.join(d, f"{name}.webp")
            if os.path.exists(webp):
                os.remove(webp)
        print(f"wrote mipmap-{bucket} ({size}x{size})")

    base.resize((320, 320), Image.LANCZOS).save("/tmp/aura_preview_sq.png")
    prev = base.resize((320, 320), Image.LANCZOS)
    prev.putalpha(round_mask(320))
    prev.save("/tmp/aura_preview_round.png")


if __name__ == "__main__":
    main()

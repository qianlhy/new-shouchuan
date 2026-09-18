# -*- coding: utf-8 -*-
"""Generate native WeChat tabBar icons at official 81x81 (图二造型)."""
from PIL import Image, ImageDraw
import math
import os

OUT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "static", "tabbar"))
SS = 4
BOX = 81  # WeChat official
SIZE = BOX * SS
GRAY = (107, 107, 107, 255)
PURPLE = (139, 92, 246, 255)


def canvas():
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def save(im, name):
    out = im.resize((BOX, BOX), Image.Resampling.LANCZOS)
    path = os.path.join(OUT, name)
    out.save(path, "PNG", optimize=True)
    print(f"{name}: {out.size} {os.path.getsize(path)}B")


def W():
    return max(SS * 2, int(2.2 * SS))


def u(x):
    return x * SS


def draw_home(c):
    im = canvas()
    d = ImageDraw.Draw(im)
    w = W()
    d.line([(u(40.5), u(17)), (u(17), u(38)), (u(64), u(38)), (u(40.5), u(17))], fill=c, width=w, joint="curve")
    d.rounded_rectangle([u(20), u(38), u(61), u(66)], radius=u(3), outline=c, width=w)
    d.rounded_rectangle([u(34), u(48), u(47), u(66)], radius=u(2), outline=c, width=w)
    return im


def draw_plane(c):
    im = canvas()
    d = ImageDraw.Draw(im)
    w = W()
    nose, left, fold = (u(65), u(17)), (u(13), u(39)), (u(33), u(43))
    right, tail = (u(52), u(54)), (u(20), u(63))
    for a, b in [(left, nose), (nose, right), (right, fold), (fold, left), (fold, tail), (nose, fold)]:
        d.line([a, b], fill=c, width=w)
    return im


def draw_seal(c):
    im = canvas()
    d = ImageDraw.Draw(im)
    w = W()
    cx, cy = u(40.5), u(40.5)
    base = u(21)
    petals = 16
    pts = []
    for i in range(petals * 8):
        ang = 2 * math.pi * i / (petals * 8) - math.pi / 2
        rr = base + u(5.2) * (0.5 + 0.5 * math.cos(petals * ang))
        pts.append((cx + rr * math.cos(ang), cy + rr * math.sin(ang)))
    d.line(pts + [pts[0]], fill=c, width=w)
    ir = u(13.5)
    d.ellipse([cx - ir, cy - ir, cx + ir, cy + ir], outline=c, width=w)
    d.ellipse([cx + u(-9), cy + u(-10), cx + u(-3.5), cy + u(-4.5)], outline=c, width=w)
    d.ellipse([cx + u(3.5), cy + u(4.5), cx + u(9), cy + u(10)], outline=c, width=w)
    d.line([(cx + u(8), cy + u(-8.5)), (cx + u(-8), cy + u(8.5))], fill=c, width=w)
    return im


def draw_cart(c):
    im = canvas()
    d = ImageDraw.Draw(im)
    w = W()
    d.line(
        [(u(17), u(27)), (u(25), u(27)), (u(29), u(52)), (u(55), u(52)), (u(59), u(27)), (u(68), u(27))],
        fill=c, width=w, joint="curve",
    )
    d.line([(u(29), u(52)), (u(29), u(58))], fill=c, width=w)
    d.line([(u(55), u(52)), (u(55), u(58))], fill=c, width=w)
    d.ellipse([u(25), u(58), u(34), u(67)], outline=c, width=w)
    d.ellipse([u(51), u(58), u(60), u(67)], outline=c, width=w)
    d.arc([u(11), u(15), u(28), u(36)], start=200, end=330, fill=c, width=w)
    return im


def draw_mine(c):
    im = canvas()
    d = ImageDraw.Draw(im)
    w = W()
    d.ellipse([u(29), u(14), u(52), u(37)], outline=c, width=w)
    d.arc([u(15), u(40), u(66), u(78)], start=20, end=160, fill=c, width=w)
    return im


def main():
    os.makedirs(OUT, exist_ok=True)
    makers = {
        "home": draw_home,
        "square": draw_plane,
        "design": draw_seal,
        "cart": draw_cart,
        "mine": draw_mine,
    }
    for k, fn in makers.items():
        g, p = fn(GRAY), fn(PURPLE)
        save(g, f"{k}.png")
        save(p, f"{k}_selected.png")
        save(g, f"{k}@3x.png")
        save(p, f"{k}_selected@3x.png")


if __name__ == "__main__":
    main()

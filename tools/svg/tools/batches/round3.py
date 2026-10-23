"""New icons, round 3: values whose icon stood in for something else (notes.md, New Icons).

Usage: python3 round3.py [labels...]   (default: all of these). Tabler style on the 24 grid, built with round2's packer.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, os.path.dirname(__file__))
from anim import fmt, group, osc
from forge import keys, p
from rig import morph
import round2
from round2 import pack, build

round2.PACKS.clear()


def circle(cx, cy, r):
    return f'M{fmt(cx - r)} {fmt(cy)}a{fmt(r)} {fmt(r)} 0 1 0 {fmt(2 * r)} 0a{fmt(r)} {fmt(r)} 0 1 0 {fmt(-2 * r)} 0'


def dot(x, y):
    return f'M{fmt(x)} {fmt(y)}v.01'


def animated(d, *anims, attrs=''):
    inner = '\n'.join('    ' + a for a in anims)
    return f'<path{(" " + attrs) if attrs else ""} d="{d}">\n{inner}\n</path>'


def bump(base, peak, at, rise=0.06, fall=0.14):
    """Key pairs that rest at [base], reach [peak] at time [at], and settle back."""
    return [(at, base), (at + rise, peak), (at + rise + fall, base)]


def track(base, events):
    """Key pairs from (time, value) [events], holding [base] at the ends."""
    pairs = [(0, base)] + events + [(1, base)]
    return [pairs[0]] + [e for e in pairs[1:] if e[0] > 0]


# ---------- clown: a round face, red nose, wide smile, tufts of hair at the sides; the nose honks, the tufts puff ----------
CLOWN_FACE = circle(12, 13, 6.5)
CLOWN_NOSE = circle(12, 13.2, 1.6)
CLOWN_EYES = dot(9.3, 10.6) + dot(14.7, 10.6)
CLOWN_SMILE = 'M8.3 15.8Q12 20.2 15.7 15.8'
TUFT_L = 'M6 9.6Q4.4 7.6 2.9 9.4Q1.8 10.9 3 12.4Q1.6 14.2 3.2 15.6Q4.6 16.8 6.3 16'
TUFT_R = 'M18 9.6Q19.6 7.6 21.1 9.4Q22.2 10.9 21 12.4Q22.4 14.2 20.8 15.6Q19.4 16.8 17.7 16'


def fluff(d, x):
    """A tuft that puffs outward from its roots at [x] on the face, so it never crosses the face."""
    puff = keys('transform', track('1 1', bump('1 1', '1.18 1.06', 0.12) + bump('1 1', '1.18 1.06', 0.37)), 2.4, kind='scale')
    inner = group([p(d)], attrs=f'transform="translate({fmt(-x)} -12.8)"')
    return group([group([inner], puff)], attrs=f'transform="translate({fmt(x)} 12.8)"')


@pack('clown')
def clown():
    static = [p(CLOWN_FACE), p(TUFT_L), p(TUFT_R), p(CLOWN_EYES), p(CLOWN_NOSE), p(CLOWN_SMILE)]
    honk = keys('transform', track('1', bump('1', '1.35', 0.1) + bump('1', '1.35', 0.35)), 2.4, kind='scale')
    nose = group([group([group([p(circle(0, 0, 1.6))], honk)], attrs='transform="translate(12 13.2)"')])
    smile = keys('d', track(CLOWN_SMILE, bump(CLOWN_SMILE, 'M8.3 15.6Q12 21 15.7 15.6', 0.1, fall=0.4)), 2.4)
    return static, [p(CLOWN_FACE), fluff(TUFT_L, 6.1), fluff(TUFT_R, 17.9), p(CLOWN_EYES),
                    animated(CLOWN_SMILE, smile), nose]


# ---------- top-hat: a magician's hat and wand; the wand's tip taps the crown, sparkles twinkle, a rabbit peeks out ----------
HAT_CROWN = 'M4.5 17.5V7a1 1 0 0 1 1 -1h8a1 1 0 0 1 1 1v10.5'
HAT_BRIM = 'M2 17.5h15'
HAT_BAND = 'M4.5 14.5h10'
WAND = 'M21.5 13.5l-5 -5'
SPARKLE_BIG = 'M18.5 2.5a2 2 0 0 0 2 2a2 2 0 0 0 -2 2a2 2 0 0 0 -2 -2a2 2 0 0 0 2 -2'
SPARKLE_SMALL = 'M21.2 6.6a1.2 1.2 0 0 0 1.2 1.2a1.2 1.2 0 0 0 -1.2 1.2a1.2 1.2 0 0 0 -1.2 -1.2a1.2 1.2 0 0 0 1.2 -1.2'
EARS = ['M7.6 6c-.6 -1.8 -.4 -4.2 .6 -4.4c1 -.2 1.3 2.4 1 4.4', 'M10 6c0 -2 .6 -4.4 1.6 -4.2c1 .2 .6 2.6 -.4 4.2']


def twinkle(d, cx, cy, at, dur):
    pulse = keys('transform', track('1', bump('1', '1.4', at, 0.1, 0.25)), dur, kind='scale')
    inner = group([p(d)], attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    return group([group([inner], pulse)], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


@pack('top-hat')
def top_hat():
    static = [p(HAT_CROWN), p(HAT_BRIM), p(HAT_BAND), p(WAND), p(SPARKLE_BIG), p(SPARKLE_SMALL)]
    dur = 4
    tap = keys('transform', track('0 21.5 13.5', bump('0 21.5 13.5', '-16 21.5 13.5', 0.08, 0.08, 0.12) + bump('0 21.5 13.5', '-16 21.5 13.5', 0.3, 0.08, 0.12)), dur, kind='rotate')
    peek = keys('transform', [(0, '0 6'), (0.45, '0 6'), (0.55, '0 0'), (0.8, '0 0'), (0.9, '0 6'), (1, '0 6')], dur, kind='translate')
    clip = '<clipPath id="top-hat-crown">\n    <path d="M0 0h24v6h-24z"/>\n</clipPath>'
    rabbit = f'{clip}\n<g clip-path="url(#top-hat-crown)">\n' + '\n'.join('    ' + l for l in group([p(e) for e in EARS], peek).split('\n')) + '\n</g>'
    return static, [rabbit, p(HAT_CROWN), p(HAT_BRIM), p(HAT_BAND), group([p(WAND)], tap),
                    twinkle(SPARKLE_BIG, 18.5, 4.5, 0.16, dur), twinkle(SPARKLE_SMALL, 21.2, 7.8, 0.38, dur)]


# ---------- pen-nib: a calligraphy nib at the end of its flourish; it lifts back and writes it again ----------
NIB = 'M0 0L-2.8 -5.2Q-3 -7.2 -2 -8.2H2Q3 -7.2 2.8 -5.2Z'
NIB_SLIT = 'M0 0V-3.6'
NIB_HOLE = circle(0, -4.6, 1)
NIB_COLLAR = 'M-2 -8.2V-10.2H2V-8.2'
FLOURISH = 'M2.5 17.5C4 13 7 12.5 7.5 15.5C8 19 9.5 18.5 11 15.5'
NIB_AT = (11, 15.5)


def nib(x, y):
    parts = [p(NIB), p(NIB_SLIT), p(NIB_HOLE), p(NIB_COLLAR)]
    return group(parts, attrs=f'transform="translate({fmt(x)} {fmt(y)}) rotate(40)"')


@pack('pen-nib')
def pen_nib():
    static = [p(FLOURISH), nib(*NIB_AT)]
    dur = 4
    t = [0, 0.2, 0.3, 0.45, 0.9, 1]
    ink = (f'<path d="{FLOURISH}" pathLength="1" stroke-dasharray="1 1.1">\n'
           f'    <animate attributeName="stroke-dashoffset" values="0;0;0;1;0;0" keyTimes="{";".join(fmt(x) for x in t)}" dur="{dur}s" repeatCount="indefinite"/>\n'
           f'    <animate attributeName="opacity" values="1;1;0;1;1;1" keyTimes="{";".join(fmt(x) for x in t)}" dur="{dur}s" repeatCount="indefinite"/>\n</path>')
    motion = (f'<animateMotion path="{FLOURISH}" keyPoints="1;1;1;0;1;1" keyTimes="{";".join(fmt(x) for x in t)}" '
              f'calcMode="spline" keySplines="{";".join(["0.42 0 0.58 1"] * 5)}" dur="{dur}s" repeatCount="indefinite"/>')
    pen = group([nib(0, 0)], motion)
    return static, [ink, pen]


# ---------- french-horn: a coiled tube with a wide bell; the bell swells on each note ----------
HORN_COIL = circle(9, 9.5, 5.5)
HORN_INNER = circle(9, 9.5, 2.7)
HORN_BELL = 'M9 15C13 15.2 16 16.5 17.2 20.8M11.4 14.5C14.5 13.8 17.5 13.4 20.8 14.6M17.2 20.8A3.58 1.2 -60 1 0 20.8 14.6A3.58 1.2 -60 1 0 17.2 20.8'
HORN_LEAD = 'M5.1 5.6L3 3.5m-.6 1.2l1.8 -1.8'
HORN_VALVES = 'M9 10.5h2'


@pack('french-horn')
def french_horn():
    static = [p(HORN_COIL), p(HORN_INNER), p(HORN_BELL), p(HORN_LEAD)]
    dur = 2.4
    swell = keys('transform', track('1', bump('1', '1.1', 0.05, 0.08, 0.3) + bump('1', '1.1', 0.5, 0.08, 0.3)), dur, kind='scale')
    bell = group([group([group([p(HORN_BELL)], attrs='transform="translate(-10.5 -14.8)"')], swell)], attrs='transform="translate(10.5 14.8)"')
    rock = keys('transform', track('0 9 9.5', bump('0 9 9.5', '-3 9 9.5', 0.05, 0.08, 0.3) + bump('0 9 9.5', '-3 9 9.5', 0.5, 0.08, 0.3)), dur, kind='rotate')
    return static, [group([p(HORN_COIL), p(HORN_INNER), bell, p(HORN_LEAD)], rock)]


# ---------- clarinet: a straight tube on a slant with a flared bell and a beak mouthpiece; fingers close the holes ----------
CLAR_MOUTH = 'M11.3 5.5l.3 -3h.8l.3 3'
CLAR_TUBE = 'M10.5 5.5h3v12h-3z'
CLAR_RINGS = 'M10.5 7.5h3'
CLAR_BELL = 'M10.5 17.5Q10.5 20 8.8 21.5h6.4Q13.5 20 13.5 17.5'
CLAR_HOLES = [10, 12.5, 15]


@pack('clarinet')
def clarinet():
    base = [p(CLAR_MOUTH), p(CLAR_TUBE), p(CLAR_RINGS), p(CLAR_BELL)]
    holes = [dot(12, y) for y in CLAR_HOLES]
    tilt = 'transform="rotate(30 12 12)"'
    static = [group(base + [p(h) for h in holes], attrs=tilt)]
    out = list(base)
    for i, h in enumerate(holes):
        out.append(animated(h, keys('stroke-width', track('1', [(0.05 + i * 0.12, '1'), (0.12 + i * 0.12, '2.4'), (0.55, '2.4'), (0.65, '1')]), 2.8)))
    bob = keys('transform', track('30 12 12', [(0.1, '30 12 12'), (0.4, '27 12 12'), (0.7, '30 12 12')]), 2.8, kind='rotate')
    return static, [group(out, bob)]


# ---------- organ: a rank of pipes over a console; each pipe rises as its note sounds ----------
PIPE_X = [4.5, 7.5, 10.5, 13.5, 16.5]
PIPE_TOPS = [9.5, 6.5, 3.5, 6.5, 9.5]
CONSOLE = 'M3 15.5h18v5h-18z'
KEYBOARD = 'M6 18h12'


def pipe(x, top):
    return f'M{fmt(x)} 15.5V{fmt(top)}h3V15.5'


def mouth(x):
    return f'M{fmt(x + 0.75)} 13.5l.75 -1l.75 1'


@pack('organ')
def organ():
    pipes = [pipe(x, t) for x, t in zip(PIPE_X, PIPE_TOPS)]
    static = [p(d) for d in pipes] + [p(mouth(x)) for x in PIPE_X] + [p(CONSOLE), p(KEYBOARD)]
    dur = 3.2
    order = [2, 0, 3, 1, 4]
    out = []
    for i, (x, t) in enumerate(zip(PIPE_X, PIPE_TOPS)):
        at = 0.05 + order.index(i) * 0.13
        out.append(animated(pipe(x, t), keys('d', track(pipe(x, t), bump(pipe(x, t), pipe(x, t - 1.2), at, 0.08, 0.25)), dur)))
    return static, out + [p(mouth(x)) for x in PIPE_X] + [p(CONSOLE), p(KEYBOARD)]


# ---------- water-slide: a ladder tower and a winding slide into the pool; a rider slides down and splashes ----------
LADDER = 'M3.5 21V4.5m3 16.5V4.5M3.5 8h3m-3 3.5h3m-3 3.5h3m-3 3.5h3'
SLIDE_TOP = 'M6.5 4.5C12 4.5 9 10.5 14 11.5C17 12 19 13.5 20 16.5'
SLIDE_LOW = 'M6.5 7C10 7 7.5 13 13 14C15.5 14.5 16.5 15.5 17.5 17.5'
POOL = 'M9 20.5q1.25 -1 2.5 0t2.5 0t2.5 0t2.5 0t2.5 0'
RIDE = 'M7.5 4C11.5 4.5 9 10.5 13.5 11.5C16.5 12.2 18 13.5 19 16.5L19.5 18.5'


@pack('water-slide')
def water_slide():
    static = [p(LADDER), p(SLIDE_TOP), p(SLIDE_LOW), p(POOL)]
    dur = 3.2
    t = '0;0.2;0.55;0.65;1'
    rider = (f'<path d="{circle(0, -1.6, 1.1)}" opacity="0">\n'
             f'    <animateMotion path="{RIDE}" keyPoints="0;0;1;1;1" keyTimes="{t}" calcMode="spline" keySplines="0 0 1 1;0.5 0 0.9 0.6;0 0 1 1;0 0 1 1" dur="{dur}s" repeatCount="indefinite"/>\n'
             f'    <animate attributeName="opacity" values="0;1;1;0;0" keyTimes="{t}" dur="{dur}s" repeatCount="indefinite"/>\n</path>')
    splash = []
    for side in (-1, 1):
        d0 = f'M{fmt(19 + side * 0.8)} 19.5l{fmt(side * 0.01)} -.01'
        d1 = f'M{fmt(19 + side * 0.8)} 19.5l{fmt(side * 1.4)} -2'
        splash.append(f'<path d="{d0}" opacity="0">\n    {keys("d", [(0, d0), (0.55, d0), (0.65, d1), (0.8, d1), (1, d0)], dur)}\n'
                      f'    <animate attributeName="opacity" values="0;0;1;0;0" keyTimes="0;0.55;0.6;0.8;1" dur="{dur}s" repeatCount="indefinite"/>\n</path>')
    drift = keys('transform', [(0, '0 0'), (1, '-5 0')], dur)
    drift = f'<animateTransform attributeName="transform" type="translate" values="0 0;-5 0" dur="{dur}s" repeatCount="indefinite"/>'
    clip = '<clipPath id="water-slide-pool">\n    <path d="M9 17h12.5v5h-12.5z"/>\n</clipPath>'
    long_pool = 'M9 20.5q1.25 -1 2.5 0t2.5 0t2.5 0t2.5 0t2.5 0t2.5 0t2.5 0'
    pool = f'{clip}\n<g clip-path="url(#water-slide-pool)">\n' + '\n'.join('    ' + l for l in group([p(long_pool)], drift).split('\n')) + '\n</g>'
    return static, [p(LADDER), p(SLIDE_TOP), p(SLIDE_LOW), pool, rider] + splash


# ---------- speakeasy: a door with a sliding peephole; eyes glance about, the slot shuts and opens ----------
DOOR = 'M6 21v-16a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v16'
FLOOR = 'M3 21h18'
KNOB = dot(15.5, 13)
SLOT_TOP, SLOT_BOTTOM = 6, 9.5


def slot(bottom):
    return f'M8.5 {fmt(SLOT_TOP)}h7v{fmt(bottom - SLOT_TOP)}h-7z'


def eyes(dx):
    return dot(10.75 + dx, 7.75) + dot(13.25 + dx, 7.75)


@pack('speakeasy')
def speakeasy():
    static = [p(DOOR), p(FLOOR), p(KNOB), p(slot(SLOT_BOTTOM)), p(eyes(0))]
    dur = 5
    o, c = slot(SLOT_BOTTOM), slot(SLOT_TOP + 0.01)
    shut = keys('d', [(0, o), (0.5, o), (0.6, c), (0.8, c), (0.9, o), (1, o)], dur)
    glance = keys('d', [(0, eyes(0)), (0.08, eyes(0)), (0.16, eyes(-1)), (0.26, eyes(-1)), (0.34, eyes(1)), (0.44, eyes(1)), (0.5, eyes(0)), (1, eyes(0))], dur)
    blink = '<animate attributeName="opacity" values="1;1;0;0;1;1" keyTimes="0;0.52;0.56;0.86;0.9;1" dur="5s" repeatCount="indefinite"/>'
    return static, [p(DOOR), p(FLOOR), p(KNOB), animated(o, shut), animated(eyes(0), glance, blink)]


# ---------- beatbox: a bust in profile with beats leaving the mouth; boom and tss in turn ----------
HEAD = ('M7.5 17V15.8C5.4 14.8 4 12.8 4 10.4C4 6.6 6.9 3.5 10.6 3.5C13.6 3.5 15.5 5.5 15.5 8.2L16.8 10.9'
        'C17 11.4 16.7 11.7 16.2 11.7H15.6V13.6C15.6 15 14.6 15.8 13.3 15.8H12V17')
SHOULDERS = 'M3 21C3 18.5 5 17 7.5 17M12 17C14.5 17 16.5 18.5 16.5 21'
BEAT_IN = 'M18.2 10.8a3.5 3.5 0 0 1 0 4.4'
BEAT_OUT = 'M20.4 8.6a6.5 6.5 0 0 1 0 8.8'


@pack('beatbox')
def beatbox():
    static = [p(HEAD), p(SHOULDERS), p(BEAT_IN), p(BEAT_OUT)]
    dur = 1.6
    nod = keys('transform', track('0 9.75 16.5', bump('0 9.75 16.5', '-2.5 9.75 16.5', 0, 0.06, 0.18) + bump('0 9.75 16.5', '-2.5 9.75 16.5', 0.5, 0.06, 0.18)), dur, kind='rotate')
    boom = keys('opacity', [(0, '1'), (0.2, '0.15'), (0.5, '1'), (0.7, '0.15'), (1, '1')], dur)
    # frame 0 equals the static icon: both beats full; the inner one dips out of phase after it
    tss = keys('opacity', [(0, '1'), (0.12, '0.15'), (0.25, '1'), (0.45, '0.15'), (0.75, '1'), (0.95, '0.6'), (1, '1')], dur)
    return static, [group([p(HEAD)], nod), p(SHOULDERS), animated(BEAT_IN, tss), animated(BEAT_OUT, boom)]


# ---------- race-flag: a checkered flag on its pole, waving ----------
FLAG_X, FLAG_Y, FLAG_W, FLAG_H = 5, 4, 15, 9
COLS, ROWS = 4, 3


def flag_point(u, v, t):
    wave = 0.9 * math.sin(2 * math.pi * (u * 0.9 - t)) * u
    return FLAG_X + FLAG_W * u - 0.6 * u * u * (1 + math.sin(2 * math.pi * (u * 0.9 - t + 0.25))) * 0.5, FLAG_Y + FLAG_H * v + wave


def edge(u0, u1, v, t, steps=3):
    pts = [flag_point(u0 + (u1 - u0) * k / steps, v, t) for k in range(steps + 1)]
    return pts


def poly(pts):
    return 'M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in pts) + 'Z'


def cell(i, j, t):
    u0, u1, v0, v1 = i / COLS, (i + 1) / COLS, j / ROWS, (j + 1) / ROWS
    return poly(edge(u0, u1, v0, t) + edge(u1, u0, v1, t))


def outline(t):
    top = edge(0, 1, 0, t, 12)
    bottom = edge(1, 0, 1, t, 12)
    return poly(top + bottom)


@pack('race-flag')
def race_flag():
    pole = 'M5 21V3'
    filled = [(i, j) for i in range(COLS) for j in range(ROWS) if (i + j) % 2 == 0]
    fill = 'stroke="none" fill="currentColor"'
    static = [p(pole), p(outline(0))] + [f'<path {fill} d="{cell(i, j, 0)}"/>' for i, j in filled]
    n, dur = 12, 1.6
    ts = [k / n for k in range(n)]
    out = [p(pole), animated(outline(0), morph([outline(t) for t in ts], dur, linear=True))]
    out += [animated(cell(i, j, 0), morph([cell(i, j, t) for t in ts], dur, linear=True), attrs=fill) for i, j in filled]
    return static, out


if __name__ == '__main__':
    for label in sys.argv[1:] or round2.PACKS:
        build(label)
        print('built', label)

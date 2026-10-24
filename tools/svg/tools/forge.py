"""Forged icons in the Tabler style: static small/large/very-large plus an animated variant.

Usage: python3 forge.py [labels...]   (default: all)
Each design is a list of element strings on the 24 grid; `wrap` may rotate the whole drawing.
"""
import math
import os
import re
import sys
sys.path.insert(0, os.path.dirname(__file__))
from anim import SVG_DIR, EASE, fmt, group, osc, spin

FRAME = '<path stroke="none" d="M0 0h24v24H0z" fill="none"/>'


def header(label, width, kind='outline'):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"\n'
            f'     stroke-width="{width}" stroke-linecap="round" stroke-linejoin="round"\n'
            f'     class="icon icon-streetlight icons-streetlight-{kind} icon-streetlight-{label}">')


def write(path, head, body):
    lines = []
    for b in body:
        lines += ['    ' + ln for ln in b.split('\n')]
    open(path, 'w').write(head + '\n' + '\n'.join(lines) + '\n</svg>\n')


def keys(attr, pairs, dur, kind=None, begin=None):
    """An animation through (time fraction, value) pairs, eased between each, looping."""
    ts = ';'.join(fmt(t) for t, _ in pairs)
    vs = ';'.join(v for _, v in pairs)
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"' + (f' type="{kind}"' if kind else '')
    a += f' values="{vs}" keyTimes="{ts}" dur="{dur}s" repeatCount="indefinite" calcMode="spline" keySplines="{";".join([EASE] * (len(pairs) - 1))}"'
    return a + (f' begin="{begin}"' if begin else '') + '/>'


def p(d):
    return f'<path d="{d}"/>'


def wrapped(els, rot):
    return [group(els, attrs=f'transform="rotate({rot} 12 12)"')] if rot else els


DESIGNS = {}


def design(label, rot=0):
    def deco(fn):
        DESIGNS[label] = (fn, rot)
        return fn
    return deco


# ---------- drum: a snare with two sticks; the sticks take turns striking ----------
DRUM_HEAD = 'M4 10a8 3 0 1 0 16 0a8 3 0 1 0 -16 0'
DRUM_SHELL = 'M4 10v7a8 3 0 0 0 16 0v-7'
DRUM_LACE = 'M7 12.6l2.5 6m2.5 -5.4l-2.5 5.4m2.5 -5.4l2.5 5.4m2.5 -6l-2.5 6'
STICK_L, STICK_R = 'M5 3l6 6', 'M19 3l-6 6'


@design('drum')
def drum(animated):
    base = [p(DRUM_HEAD), p(DRUM_SHELL), p(DRUM_LACE)]
    if not animated:
        return base + [p(STICK_L), p(STICK_R)]
    lift = lambda deg, a, b: keys('transform', [(0, '0 5 3' if deg < 0 else '0 19 3'), (a, f'{deg} {5 if deg < 0 else 19} 3'),
                                                 (b, '0 5 3' if deg < 0 else '0 19 3'), (1, '0 5 3' if deg < 0 else '0 19 3')], 1.6, kind='rotate')
    return base + [group([p(STICK_L)], lift(-14, 0.2, 0.4)), group([p(STICK_R)], keys('transform', [(0, '0 19 3'), (0.5, '0 19 3'), (0.7, '14 19 3'), (0.9, '0 19 3'), (1, '0 19 3')], 1.6, kind='rotate'))]


# ---------- violin: upright violin, the bow draws across the strings ----------
VIOLIN_BODY = ('M12 8c-2.6 0 -3.6 1.3 -3.6 2.6c0 1 .9 1.4 .9 2.2c0 .9 -1.9 1.5 -1.9 3.7c0 2.3 2 4 4.6 4s4.6 -1.7 4.6 -4'
               'c0 -2.2 -1.9 -2.8 -1.9 -3.7c0 -.8 .9 -1.2 .9 -2.2c0 -1.3 -1 -2.6 -3.6 -2.6')
VIOLIN_NECK = 'M12 4v12'
VIOLIN_SCROLL = 'M12 4a1 1 0 1 1 1 -1'
VIOLIN_BRIDGE = 'M10.6 17h2.8'
VIOLIN_FHOLES = 'M9.9 13.2v2.2m4.2 -2.2v2.2'
BOW = 'M4 18l15 -6'


@design('violin')
def violin(animated):
    base = [p(VIOLIN_BODY), p(VIOLIN_NECK), p(VIOLIN_SCROLL), p(VIOLIN_BRIDGE), p(VIOLIN_FHOLES)]
    if not animated:
        return base + [p(BOW)]
    # the bow slides along its own direction
    dx, dy = 15 / math.hypot(15, 6) * 1.8, -6 / math.hypot(15, 6) * 1.8
    return base + [group([p(BOW)], osc('transform', ['0 0', f'{fmt(dx)} {fmt(dy)}', '0 0', f'{fmt(-dx)} {fmt(-dy)}'], 2.4, kind='translate'))]


# ---------- trumpet: pipe, valves and bell; the valves press in turn ----------
TRUMPET_PIPE = 'M3 10h11'
TRUMPET_LOOP = 'M6 10v3a1 1 0 0 0 1 1h6a1 1 0 0 0 1 -1v-3'
TRUMPET_BELL = 'M14 10l6 -4v8l-6 -4'
TRUMPET_MOUTH = 'M3 9v2'
VALVES = [8, 10, 12]


@design('trumpet')
def trumpet(animated):
    base = [p(TRUMPET_PIPE), p(TRUMPET_LOOP), p(TRUMPET_BELL), p(TRUMPET_MOUTH)]
    valves = [p(f'M{x} 10v-3m-.8 0h1.6') for x in VALVES]
    if not animated:
        return base + valves
    out = list(base)
    for i, v in enumerate(valves):
        a = 0.08 + i * 0.25
        out.append(group([v], keys('transform', [(0, '0 0'), (a, '0 0'), (a + 0.1, '0 1.3'), (a + 0.22, '0 0'), (1, '0 0')], 2.4, kind='translate')))
    return out


# ---------- saxophone: J-shaped body with keys; it sways from the mouthpiece ----------
SAX_BODY = 'M6 3l2 2c1.7 0 3 1.3 3 3v8a3.5 3.5 0 0 0 7 0v-2.5'
SAX_RIM = 'M16 13.5h4'
SAX_KEYS = 'M11 9.5h1.5m-1.5 2.5h1.5m-1.5 2.5h1.5'


@design('saxophone')
def saxophone(animated):
    base = [p(SAX_BODY), p(SAX_RIM), p(SAX_KEYS)]
    if not animated:
        return base
    return [group(base, osc('transform', ['0 7 4', '5 7 4', '0 7 4', '-5 7 4'], 3, kind='rotate'))]


# ---------- acoustic guitar: drawn upright, shown on the diagonal; the strings ring ----------
GUITAR_BODY = ('M12 9c-2 0 -3 1 -3 2.3c0 .9 .6 1.4 .6 2c0 .7 -2.1 1.2 -2.1 3.7c0 2.3 2 4 4.5 4s4.5 -1.7 4.5 -4'
               'c0 -2.5 -2.1 -3 -2.1 -3.7c0 -.6 .6 -1.1 .6 -2c0 -1.3 -1 -2.3 -3 -2.3')
GUITAR_HOLE = 'M10.8 15a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0'
GUITAR_HEAD = 'M11 2h2l-.2 2h-1.6l-.2 -2'
GUITAR_BRIDGE = 'M10.5 18h3'
GUITAR_NECK = 'M12 4v9.8'


@design('acoustic-guitar', rot=40)
def acoustic_guitar(animated):
    base = [p(GUITAR_BODY), p(GUITAR_HOLE), p(GUITAR_HEAD), p(GUITAR_BRIDGE)]
    if not animated:
        return base + [p(GUITAR_NECK)]
    # a strum every 2s: the string bows out and settles
    shape = lambda a: f'M12 4q{fmt(a)} 4.9 0 9.8'
    pairs = [(0, shape(0)), (0.05, shape(1.4)), (0.12, shape(-1.1)), (0.2, shape(0.8)), (0.28, shape(-0.5)), (0.36, shape(0.25)), (0.45, shape(0)), (1, shape(0))]
    return base + [f'<path d="{shape(0)}">\n    {keys("d", pairs, 2)}\n</path>']


# ---------- boxing glove: the glove jabs forward and draws back ----------
GLOVE_BODY = 'M8 17v-7a6 6 0 0 1 6 -6h1a4 4 0 0 1 4 4v4a5 5 0 0 1 -3 5'
GLOVE_THUMB = 'M8 11h2a2 2 0 1 1 0 4h-2'
GLOVE_CUFF = 'M7 17h10v4h-10l0 -4'


@design('boxing-glove')
def boxing_glove(animated):
    base = [p(GLOVE_BODY), p(GLOVE_THUMB), p(GLOVE_CUFF)]
    if not animated:
        return base
    return [group(base, keys('transform', [(0, '0 0'), (0.15, '0.6 0.6'), (0.3, '-1 -1'), (0.55, '0 0'), (1, '0 0')], 2, kind='translate'))]


# ---------- disco ball: the facets turn and sparkles twinkle ----------
BALL = 'M5 13a7 7 0 1 0 14 0a7 7 0 1 0 -14 0'
BALL_HANG = 'M12 3v3'
BALL_LAT = 'M5.6 10.2h12.8m-12.8 5.6h12.8'
PHASES = [1 / 8, 3 / 8, 5 / 8, 7 / 8]


def meridian(k):
    # the front half of a meridian, bulging k units left (negative) or right (positive)
    c = 0.5523
    return (f'M12 6C{fmt(12 + c * k)} 6 {fmt(12 + k)} {fmt(13 - c * 7)} {fmt(12 + k)} 13'
            f'S{fmt(12 + c * k)} 20 12 20')


def bulge(phase):
    return 7 * math.sin(math.pi * (phase - 0.5))


@design('disco-ball')
def disco_ball(animated):
    base = [p(BALL), p(BALL_HANG), p(BALL_LAT)]
    if not animated:
        return base + [p(meridian(bulge(ph))) for ph in PHASES]
    out = list(base)
    n = 24
    for ph in PHASES:
        # sweep left to right; the wrap happens on the outline, where it can't be seen
        pts = []
        jump = 1 - ph
        for i in range(n + 1):
            t = i / n * jump
            pts.append((t, meridian(bulge(ph + t))))
        pts.append((jump + 0.0001, meridian(bulge(0))))
        for i in range(1, n + 1):
            t = jump + i / n * ph
            pts.append((min(t, 1), meridian(bulge(t - jump))))
        out.append(f'<path d="{meridian(bulge(ph))}">\n    <animate attributeName="d" values="{";".join(v for _, v in pts)}" '
                   f'keyTimes="{";".join(fmt(t) for t, _ in pts)}" dur="4s" repeatCount="indefinite"/>\n</path>')
    sparkles = []
    for (x, y), a in (((20, 5), 0.1), ((4, 7), 0.55)):
        star = f'<path d="M{x} {y - 1.5}v3m-1.5 -1.5h3"/>'
        sparkles.append(group([star], keys('transform', [(0, f'0 {x} {y}'), (a, f'0 {x} {y}'), (a + 0.15, f'45 {x} {y}'), (a + 0.3, f'90 {x} {y}'), (1, f'90 {x} {y}')], 4, kind='rotate'),
                              keys('opacity', [(0, '0'), (a, '0'), (a + 0.15, '1'), (a + 0.3, '0'), (1, '0')], 4)))
    return out + sparkles


# ---------- juggling: three balls in a cascade over two hands ----------
JUGGLE_PATH = 'M6 14Q12 -2 18 14Q12 18 6 14'
HANDS = 'M3.5 16.5q2.5 2 5 0m7 0q2.5 2 5 0'
HAND_L, HAND_R = 'M3.5 16.5q2.5 2 5 0', 'M15.5 16.5q2.5 2 5 0'


def path_point(frac):
    # point at arc-length fraction along JUGGLE_PATH (two quadratic beziers)
    segs = [((6, 14), (12, -2), (18, 14)), ((18, 14), (12, 18), (6, 14))]
    pts = []
    for a, c, b in segs:
        for i in range(400):
            t = i / 400
            pts.append(((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0], (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1]))
    pts.append((6, 14))
    lens = [0]
    for i in range(1, len(pts)):
        lens.append(lens[-1] + math.dist(pts[i - 1], pts[i]))
    target = frac * lens[-1]
    i = next(i for i, l in enumerate(lens) if l >= target)
    return pts[i]


@design('juggling')
def juggling(animated):
    fracs = [0.1, 0.1 + 1 / 3, 0.1 + 2 / 3]
    if not animated:
        balls = []
        # positions measured with getPointAtLength, matching animateMotion's paced timing
        for x, y in [(7.31, 10.889), (15.252, 8.35), (13.454, 15.883)]:
            balls.append(f'<circle cx="{fmt(x)}" cy="{fmt(y)}" r="1.5"/>')
        return [p(HANDS)] + balls
    # each hand dips with the arriving ball and tosses it on; a ball reaches each hand every second
    def hand(d, e, inward):
        pairs = [(0, '0 0'), (e - 0.2, '0 0'), (e - 0.05, f'{fmt(-inward * 0.2)} 0.6'), (e + 0.05, f'{fmt(inward * 0.5)} -0.7'), (e + 0.25, '0 0'), (1, '0 0')]
        return group([p(d)], keys('transform', pairs, 1, kind='translate'))
    hands = [hand(HAND_L, 0.7, 1), hand(HAND_R, 0.56, -1)]
    balls = []
    for f in fracs:
        balls.append(f'<circle r="1.5">\n    <animateMotion path="{JUGGLE_PATH}" dur="3s" begin="{fmt(-3 * f)}s" repeatCount="indefinite"/>\n</circle>')
    return hands + balls


# ---------- hot spring: a pool with rising steam ----------
SPRING_WAVE = 'M3 17c1.5 -1 3 -1 4.5 0s3 1 4.5 0s3 -1 4.5 0s3 1 4.5 0'
SPRING_BASE = 'M5 20.5h14'
STEAMS = [8, 12, 16]
def steam(x):
    return f'M{x} 13c-1 -1.2 1 -2.3 0 -3.5s1 -2.3 0 -3.5'


@design('hot-spring')
def hot_spring(animated):
    base = [p(SPRING_WAVE), p(SPRING_BASE)]
    if not animated:
        return base + [p(steam(x)) for x in STEAMS]
    out = list(base)
    # like the coffee steam: each wisp drifts up and fades, then rises back in from below
    for x, dur in zip(STEAMS, (2, 2.5, 3)):
        out.append(group([p(steam(x))],
                         f'<animateTransform attributeName="transform" type="translate" values="0 0;0 -1.5;0 0.99;0 0" keyTimes="0;0.5;0.5;1" dur="{dur}s" repeatCount="indefinite"/>',
                         f'<animate attributeName="opacity" values="1;0;0;1" keyTimes="0;0.5;0.5;1" dur="{dur}s" repeatCount="indefinite"/>'))
    return out


# ---------- flute: a long tube shown on a slant; fingers close the holes in turn ----------
FLUTE_TUBE = 'M3 10.5h18a1.5 1.5 0 0 1 0 3h-18a1.5 1.5 0 0 1 0 -3'
FLUTE_LIP = 'M5.5 12v.01'
FLUTE_HOLES = [10, 13, 16, 19]


@design('flute', rot=-30)
def flute(animated):
    base = [p(FLUTE_TUBE), p(FLUTE_LIP)]
    holes = [f'M{x} 12v.01' for x in FLUTE_HOLES]
    if not animated:
        return base + [p(h) for h in holes]
    out = list(base)
    for i, h in enumerate(holes):
        a = 0.05 + i * 0.2
        out.append(f'<path d="{h}">\n    ' + keys('stroke-width', [(0, '1'), (a, '1'), (a + 0.08, '2.4'), (a + 0.2, '2.4'), (a + 0.3, '1'), (1, '1')], 3.2) + '\n</path>')
    return out


# ---------- dance: a dancer sways with arms waving and the right leg lifting with them; run smooth.py after ----------
DANCER_HEAD = 'M12.5 4a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0'
DANCER_TORSO = 'M13.5 7l-1.5 6'
DANCER_ARM_UP = 'M13.3 8l3.7 -2.5'
DANCER_ARM_OUT = 'M13.3 8l-4.3 1.5'
DANCER_LEG_LEFT = 'M12 13Q8.57 16.28 8 21'  # each leg one gentle arc
DANCER_LEG_RIGHT = 'M12 13Q14.49 16.16 18.5 16.5'


@design('dance')
def dance(animated):
    parts = [p(DANCER_HEAD), p(DANCER_TORSO), p(DANCER_LEG_LEFT)]
    if not animated:
        return parts + [p(DANCER_LEG_RIGHT), p(DANCER_ARM_UP), p(DANCER_ARM_OUT)]
    leg = group([p(DANCER_LEG_RIGHT)], osc('transform', ['0 12 13', '-6 12 13', '0 12 13', '2 12 13'], 2, kind='rotate'))
    arm_up = group([p(DANCER_ARM_UP)], osc('transform', ['0 13.3 8', '-14 13.3 8', '0 13.3 8', '10 13.3 8'], 2, kind='rotate'))
    arm_out = group([p(DANCER_ARM_OUT)], osc('transform', ['0 13.3 8', '14 13.3 8', '0 13.3 8', '-12 13.3 8'], 2, kind='rotate'))
    return [group(parts + [leg, arm_up, arm_out], osc('transform', ['0 8 21', '5 8 21', '0 8 21', '-4 8 21'], 2, kind='rotate'))]


# ---------- playground: a swing set; the swing goes to and fro ----------
SWING_FRAME = 'M3 21l3 -15h12l3 15'
SWING_ROPES = 'M10 6v9m4 -9v9'
SWING_SEAT = 'M9 15h6'


@design('playground')
def playground(animated):
    swing = [p(SWING_ROPES), p(SWING_SEAT)]
    if not animated:
        return [p(SWING_FRAME)] + swing
    return [p(SWING_FRAME), group(swing, osc('transform', ['0 12 6', '22 12 6', '0 12 6', '-22 12 6'], 2.6, kind='rotate'))]


# ---------- tram: a streetcar under its wire; it bobs and the wheels turn ----------
TRAM_WIRE = 'M3 2.5h18'
TRAM_POLE = 'M12 7l-2 -2.5l2 -2l2 2l-2 2.5'
TRAM_BODY = 'M4 9a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2l0 -7'
TRAM_WINDOWS = 'M7 10h3v3h-3l0 -3m7 0h3v3h-3l0 -3'
TRAM_STRIPE = 'M4 15h16'
TRAM_WHEELS = [8, 16]


@design('tram')
def tram(animated):
    wheels = [f'<circle cx="{x}" cy="19.5" r="1.5"/>' for x in TRAM_WHEELS]
    body = [p(TRAM_POLE), p(TRAM_BODY), p(TRAM_WINDOWS), p(TRAM_STRIPE)]
    if not animated:
        return [p(TRAM_WIRE)] + body + wheels
    hubs = [group([f'<path d="M{x - 0.7} 19.5h1.4"/>'], spin(x, 19.5, 1.2)) for x in TRAM_WHEELS]
    return [p(TRAM_WIRE), group(body, osc('transform', ['0 0', '0 0.4'], 1.2, kind='translate'))] + wheels + hubs


# ---------- accordion: two end boards and bellows; it squeezes and opens ----------
def bellows(x0, x1):
    xs = [x0 + (x1 - x0) * i / 6 for i in range(7)]
    d = f'M{fmt(xs[0])} 7'
    for i, x in enumerate(xs[1:], 1):
        d += f'L{fmt(x)} {17 if i % 2 else 7}'
    return d + f'M{fmt(x0)} 7H{fmt(x1)}M{fmt(x0)} 17H{fmt(x1)}'


def board(x):
    return f'M{fmt(x)} 6.5a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v11a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 -11'


@design('accordion')
def accordion(animated):
    keys_l = 'M5 9v.01m0 3v.01m0 3v.01'
    if not animated:
        return [p(board(3)), p(board(17)), p(bellows(7, 17)), p(keys_l)]
    sq = 1.6
    left = group([p(board(3)), p(keys_l)], osc('transform', ['0 0', f'{sq} 0'], 2.4, kind='translate'))
    right = group([p(board(17))], osc('transform', ['0 0', f'{-sq} 0'], 2.4, kind='translate'))
    bel = f'<path d="{bellows(7, 17)}">\n    {osc("d", [bellows(7, 17), bellows(7 + sq, 17 - sq)], 2.4)}\n</path>'
    return [left, right, bel]


# ---------- harp: column, neck and soundbox with strings plucked in turn ----------
HARP_COLUMN = 'M7 20v-15'
HARP_NECK = 'M7 5q5 -3 11 2'
HARP_BOX = 'M18 7l-9 13'
HARP_BASE = 'M5.5 20.5h5'
HARP_XS = [9.5, 11.5, 13.5, 15.5]


def harp_string(x, bow=0):
    # from the neck down to the soundbox at x
    t = next(t / 1000 for t in range(1001) if (1 - t / 1000) ** 2 * 7 + 2 * (1 - t / 1000) * (t / 1000) * 12 + (t / 1000) ** 2 * 18 >= x)
    top = (1 - t) ** 2 * 5 + 2 * (1 - t) * t * 2 + t * t * 7
    bottom = 7 + (18 - x) * 13 / 9
    return f'M{fmt(x)} {fmt(top + 0.6)}q{fmt(bow)} {fmt((bottom - top - 1.2) / 2)} 0 {fmt(bottom - top - 1.2)}'


@design('harp')
def harp(animated):
    frame = [p(HARP_COLUMN), p(HARP_NECK), p(HARP_BOX), p(HARP_BASE)]
    if not animated:
        return frame + [p(harp_string(x)) for x in HARP_XS]
    out = list(frame)
    for i, x in enumerate(HARP_XS):
        a = 0.05 + i * 0.2
        pairs = [(0, harp_string(x)), (a, harp_string(x)), (a + 0.04, harp_string(x, 0.9)), (a + 0.1, harp_string(x, -0.6)),
                 (a + 0.16, harp_string(x, 0.3)), (a + 0.24, harp_string(x)), (1, harp_string(x))]
        out.append(f'<path d="{harp_string(x)}">\n    {keys("d", pairs, 3.2)}\n</path>')
    return out


# ---------- electric guitar: solid body on the diagonal; the strings ring ----------
EGUITAR_BODY = 'M9.5 12.5l1 -2.5h3l1 2.5c1 0 2 1 2 3c0 3 -2 5.5 -4.5 5.5s-4.5 -2.5 -4.5 -5.5c0 -2 1 -3 2 -3'
EGUITAR_HEAD = 'M11 2h2l-.2 2h-1.6l-.2 -2'
EGUITAR_PICKUPS = 'M10.5 14.5h3m-3 2.5h3'
EGUITAR_KNOB = 'M14.5 19v.01'


@design('electric-guitar', rot=40)
def electric_guitar(animated):
    base = [p(EGUITAR_BODY), p(EGUITAR_HEAD), p(EGUITAR_PICKUPS), p(EGUITAR_KNOB)]
    shape = lambda a: f'M12 4q{fmt(a)} 6 0 12'
    if not animated:
        return base + [p(shape(0))]
    pairs = [(0, shape(0)), (0.05, shape(1.3)), (0.12, shape(-1)), (0.2, shape(0.7)), (0.28, shape(-0.45)), (0.36, shape(0.2)), (0.45, shape(0)), (1, shape(0))]
    return base + [f'<path d="{shape(0)}">\n    {keys("d", pairs, 1.8)}\n</path>']


# ---------- synthesizer: keys below, knobs and sliders above; the controls move ----------
SYNTH_CASE = 'M3 9a1 1 0 0 1 1 -1h16a1 1 0 0 1 1 1v10a1 1 0 0 1 -1 1h-16a1 1 0 0 1 -1 -1l0 -10'
SYNTH_KEYS = 'M3 14h18m-15 0v6m3 -6v6m3 -6v6m3 -6v6m3 -6v6'
SYNTH_KNOBS = 'M5 11a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0m3.6 0a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0'
SYNTH_TRACKS = 'M14 10v2.5m2.5 -2.5v2.5m2.5 -2.5v2.5'


@design('synthesizer')
def synthesizer(animated):
    base = [p(SYNTH_CASE), p(SYNTH_KEYS), p(SYNTH_KNOBS), p(SYNTH_TRACKS)]
    pointers = ['M6.2 11v-1.2', 'M9.8 11v-1.2']
    caps = [(14, 11.5), (16.5, 10.5), (19, 12)]
    cap = lambda x, y: f'M{fmt(x - 0.7)} {fmt(y)}h1.4'
    if not animated:
        return base + [p(d) for d in pointers] + [p(cap(x, y)) for x, y in caps]
    out = list(base)
    out.append(group([p(pointers[0])], osc('transform', ['0 6.2 11', '70 6.2 11', '0 6.2 11', '-50 6.2 11'], 3, kind='rotate')))
    out.append(group([p(pointers[1])], osc('transform', ['0 9.8 11', '-60 9.8 11', '0 9.8 11', '40 9.8 11'], 2, kind='rotate')))
    lows = [(10, 12.5), (10.5, 12.5), (12.5, 10)]
    for (x, y), (lo, hi), dur in zip(caps, lows, (2, 3, 1.5)):
        out.append(group([p(cap(x, y))], osc('transform', ['0 0', f'0 {fmt(hi - y)}', '0 0', f'0 {fmt(lo - y)}'], dur, kind='translate')))
    return out


# ---------- barber pole: stripes climb the pole forever ----------
POLE_TUBE = 'M9 5h6v14h-6l0 -14'
POLE_CAPS = 'M8 5h8m-8 14h8m-4 -14v-1.5m0 15.5v1.5'
POLE_KNOBS = 'M11 2.5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0'


def stripes(dy=0):
    return ''.join(f'M9 {fmt(y + dy)}l6 -3' for y in (6, 10, 14, 18, 22))


@design('barber-pole')
def barber_pole(animated):
    clip = '<clipPath id="tube"><rect x="9" y="5" width="6" height="14"/></clipPath>'
    base = [p(POLE_TUBE), p(POLE_CAPS), p(POLE_KNOBS)]
    st = f'<path d="{stripes()}"'
    if not animated:
        return [f'<defs>{clip}</defs>'] + base + [f'<g clip-path="url(#tube)">\n    {st}/>\n</g>']
    moving = f'{st}>\n        <animateTransform attributeName="transform" type="translate" values="0 0;0 -4" dur="2s" repeatCount="indefinite"/>\n    </path>'
    return [f'<defs>{clip}</defs>'] + base + [f'<g clip-path="url(#tube)">\n    {moving}\n</g>']


# ---------- ferris wheel: the wheel turns; the cars hang level ----------
WHEEL_RIM = 'M5 10a7 7 0 1 0 14 0a7 7 0 1 0 -14 0'
WHEEL_SPOKES = 'M12 3v14m-7 -7h14m-12 -5l10 10m0 -10l-10 10'
WHEEL_LEGS = 'M12 10l-4 11m4 -11l4 11m-10 0h12'
CAR_ANGLES = [0, 90, 180, 270]


def car_at(deg):
    x, y = 12 + 7 * math.cos(math.radians(deg)), 10 + 7 * math.sin(math.radians(deg))
    return x, y


@design('ferris-wheel')
def ferris_wheel(animated):
    legs = p(WHEEL_LEGS)
    wheel = [p(WHEEL_RIM), p(WHEEL_SPOKES)]
    car = '<path d="M-1 .6h2v1.6h-2l0 -1.6"/>'
    if not animated:
        cars = [f'<path d="M{fmt(x - 1)} {fmt(y + 0.6)}h2v1.6h-2l0 -1.6"/>' for x, y in map(car_at, CAR_ANGLES)]
        return [legs] + wheel + cars
    out = [legs, group(wheel, spin(12, 10, 12))]
    for a in CAR_ANGLES:
        ring = 'M' + ' '.join(f'{fmt(car_at(a + d)[0] - car_at(a)[0])} {fmt(car_at(a + d)[1] - car_at(a)[1])}' if i == 0 else f'L{fmt(car_at(a + d)[0] - car_at(a)[0])} {fmt(car_at(a + d)[1] - car_at(a)[1])}' for i, d in enumerate(range(0, 361, 10)))
        x, y = car_at(a)
        out.append(group([car], f'<animateMotion path="{ring}" dur="12s" repeatCount="indefinite"/>', attrs=f'transform="translate({fmt(x)} {fmt(y)})"'))
    return out


# ---------- toy blocks: three blocks stacked; the top one rocks ----------
BLOCKS_BOTTOM = 'M4 14h7v7h-7l0 -7m9 0h7v7h-7l0 -7'
BLOCK_TOP = 'M8.5 7h7v7h-7l0 -7'
BLOCK_LETTER = 'M10.5 12.5l1.5 -4l1.5 4m-2.5 -1.5h2'


@design('toy-blocks')
def toy_blocks(animated):
    top = [p(BLOCK_TOP), p(BLOCK_LETTER)]
    if not animated:
        return [p(BLOCKS_BOTTOM)] + top
    return [p(BLOCKS_BOTTOM), group(top, osc('transform', ['0 12 14', '6 15.5 14', '0 12 14', '-6 8.5 14'], 2.4, kind='rotate'))]


# ---------- fireworks: two bursts of sparks arcing down under gravity, each on the trail it rose by ----------
# in turn, each burst falls away, its trail rises again, and it blooms. Each spark carries the rocket's heading on top of
# its own throw, so those thrown ahead fly farther than those thrown back.
BURSTS = [  # center, spark radii (start, end), gravity drop at the end, gap to the end's point, angles,
            # trail angle, launch point and bend, momentum along the rocket's heading
    # the trails launch side by side at the bottom center, each bending over toward its burst
    ((14.5, 9), (2.4, 5.4), 1.2, 2, range(0, 360, 45), 110, (11.6, 21), 5.1, 1.4),
    ((5.5, 16.5), (1.5, 3.2), 0.6, 1.7, range(0, 360, 60), 20, (10.4, 21), 2.5, 0.8),
]
FIREWORK_TIMES = [(0.1, 0.3, 0.45, 0.65), (0.55, 0.72, 0.82, 1)]  # fall, gone, risen, bloomed
POINT_CLEAR = 2.5  # how near a spark's point may come to another before it is left out
POINT_AREA = (1, 23)  # the area a point stays inside
TRAIL_CLEAR = 1.5  # how near a spark may come to its launch trail in the static icon, where both show


def sparks(center, radii, drop, gap, angles, trail, launch, bend, momentum):
    """The sparks of a burst, each leaving the center at [radii][0] and thrown out to [radii][1] plus [momentum] along
    the rocket's heading, falling by [drop] at its end, its point following on at [gap]; and the trail curving up from
    [launch] to meet the center at angle [trail], [bend] setting how far it carries that heading back. The rocket's
    heading is the trail's at the center."""
    (cx, cy), (r0, r1) = center, radii
    hx, hy = -math.cos(math.radians(trail)), -math.sin(math.radians(trail))
    out = []
    for a in angles:
        ux, uy = math.cos(math.radians(a)), math.sin(math.radians(a))
        # p(t) = start + v t + (0, drop t^2), a quadratic curve, v the throw plus the momentum
        x0, y0 = cx + r0 * ux, cy + r0 * uy
        vx, vy = (r1 - r0) * ux + momentum * hx, (r1 - r0) * uy + momentum * hy
        bx, by = x0 + vx / 2, y0 + vy / 2
        x1, y1 = x0 + vx, y0 + vy + drop
        tx, ty = vx, vy + 2 * drop
        n = math.hypot(tx, ty)
        out.append((f'M{fmt(x0)} {fmt(y0)}Q{fmt(bx)} {fmt(by)} {fmt(x1)} {fmt(y1)}', (x1 + gap * tx / n, y1 + gap * ty / n)))
    ux, uy = math.cos(math.radians(trail)), math.sin(math.radians(trail))
    ex, ey = cx + r0 * ux, cy + r0 * uy
    trail_d = f'M{fmt(launch[0])} {fmt(launch[1])}Q{fmt(ex + bend * ux)} {fmt(ey + bend * uy)} {fmt(ex)} {fmt(ey)}'
    return out, trail_d


def placed_points(bursts):
    """Each burst's arcs with their points, a point left out where it would come within [POINT_CLEAR] of one kept before
    it, or leave [POINT_AREA]."""
    kept, out = [], []
    for arcs in bursts:
        placed = []
        for d, (x, y) in arcs:
            inside = all(POINT_AREA[0] <= v <= POINT_AREA[1] for v in (x, y))
            clear = all(math.hypot(x - kx, y - ky) >= POINT_CLEAR for kx, ky in kept)
            if inside and clear:
                kept.append((x, y))
                placed.append((d, f'M{fmt(x)} {fmt(y)}v.01'))
            else:
                placed.append((d, None))
        out.append(placed)
    return out


def quad_points(d, steps=24):
    """Points along a path of one quadratic, `Mx yQbx by x1 y1`."""
    x0, y0, bx, by, x1, y1 = map(float, re.findall(r'-?\d*\.?\d+', d))
    return [((1 - t) ** 2 * x0 + 2 * (1 - t) * t * bx + t * t * x1, (1 - t) ** 2 * y0 + 2 * (1 - t) * t * by + t * t * y1)
            for t in (i / steps for i in range(steps + 1))]


def clear_of(d, trail):
    """Whether the spark [d] keeps [TRAIL_CLEAR] from the [trail]."""
    path = quad_points(trail)
    return all(math.hypot(x - a, y - b) >= TRAIL_CLEAR for x, y in quad_points(d) for a, b in path)


def held(pairs):
    return pairs + ([(1, pairs[-1][1])] if pairs[-1][0] < 1 else [])


def dashed(d, anim, dashes='1 1.1'):
    return f'<path d="{d}" pathLength="1" stroke-dasharray="{dashes}">\n    {anim}\n</path>'


# a comet: a dash two thirds the trail's length swept along it, so its tail follows its head two thirds of the way behind
COMET_DASHES = '0.667 2'
COMET_HIDDEN = ('0.7', '-1.05')  # before and after the sweep, clear of the path's ends
COMET_SPAN = 1.75  # the sweep's length over the head's travel; the head reaches the end 0.553 of the way through


@design('fireworks')
def fireworks(animated):
    out = []
    shapes = [sparks(*burst) for burst in BURSTS]
    points = placed_points([arcs for arcs, _ in shapes])
    for (arcs, trail), placed, (fall, gone, risen, bloomed) in zip(shapes, points, FIREWORK_TIMES):
        if not animated:
            # the trail and the sparks show together only here, so a spark thrown back across the trail gives way
            out += [p(trail)] + [p(d) for arc in placed if clear_of(arc[0], trail) for d in arc if d]
            continue
        # the trail rises as a comet, its tail gone by the time the burst has bloomed;
        # the sparks draw away from where they started, the points fall and fade, then the sparks draw out again
        start = gone + 0.001
        end = start + (risen - start) * COMET_SPAN
        out.append(dashed(trail, keys('stroke-dashoffset', held([(0, COMET_HIDDEN[0]), (start, COMET_HIDDEN[0]), (end, COMET_HIDDEN[1])]), 3), COMET_DASHES))
        away = [(0, '0'), (fall, '0'), (gone, '-1.1'), (gone + 0.001, '1.1')]
        line = keys('stroke-dashoffset', held(away + [(risen, '1.1'), (bloomed, '0')]), 3)
        fade = keys('opacity', held([(0, '1'), (fall, '1'), (gone, '0'), (bloomed - 0.02, '0'), (bloomed, '1')]), 3)
        drop = keys('transform', held([(0, '0 0'), (fall, '0 0'), (gone, '0 1.2'), (gone + 0.001, '0 0'), (bloomed, '0 0')]), 3, kind='translate')
        for arc, dot in placed:
            out.append(dashed(arc, line))
            if dot:
                out.append(f'<path d="{dot}">\n    {fade}\n    {drop}\n</path>')
    return out


# ---------- family: a child in front at 2/3 the size of the adult behind, whose lines stop short of the child ----------
# from Tabler's users; the child hops
FAMILY_CLEAR = 2.75  # how near the adult's lines come to the child's
K = 2 / 3
CHILD_HEAD = (3 + 6 * K, 21 - 14 * K, 4 * K)  # center x, y, radius
ADULT_HEAD = (15, 7, 4)


def child_body():
    r = 4 * K
    return f'M3 21v{fmt(-2 * K)}a{fmt(r)} {fmt(r)} 0 0 1 {fmt(r)} {fmt(-r)}h{fmt(r)}a{fmt(r)} {fmt(r)} 0 0 1 {fmt(r)} {fmt(r)}v{fmt(2 * K)}'


def child_outline():
    """Points along the child's outline, for measuring how near the adult comes."""
    hx, hy, hr = CHILD_HEAD
    r = 4 * K
    pts = [(hx + hr * math.cos(t / 90 * math.pi), hy + hr * math.sin(t / 90 * math.pi)) for t in range(180)]
    pts += [(3, 21 - t / 20 * 2 * K) for t in range(21)] + [(11, 21 - t / 20 * 2 * K) for t in range(21)]
    pts += [(3 + r + r * math.cos(math.radians(180 + t)), 21 - 2 * K + r * math.sin(math.radians(180 + t))) for t in range(91)]
    pts += [(3 + r + t / 20 * r, 21 - 2 * K - r) for t in range(21)]
    pts += [(3 + 2 * r + r * math.cos(math.radians(270 + t)), 21 - 2 * K + r * math.sin(math.radians(270 + t))) for t in range(91)]
    return pts


def adult_body():
    """The adult's shoulders from the right, up to where they come within [FAMILY_CLEAR] of the child."""
    out = child_outline()
    near = lambda x, y: min(math.hypot(x - a, y - b) for a, b in out) < FAMILY_CLEAR
    d = 'M21 21v-2'
    # right shoulder: center (17, 19), 0 -> -90 degrees
    for t in range(0, 91):
        x, y = 17 + 4 * math.cos(math.radians(-t)), 19 + 4 * math.sin(math.radians(-t))
        if near(x, y):
            return d + f'A4 4 0 0 0 {fmt(x)} {fmt(y)}'
    d += 'a4 4 0 0 0 -4 -4'
    for i in range(0, 41):
        x = 17 - i / 10
        if near(x, 15):
            return d + f'H{fmt(x)}'
    d += 'h-4'
    # left shoulder: center (13, 19), -90 -> -180 degrees
    for t in range(90, 181):
        x, y = 13 + 4 * math.cos(math.radians(-t)), 19 + 4 * math.sin(math.radians(-t))
        if near(x, y):
            return d + f'A4 4 0 0 0 {fmt(x)} {fmt(y)}'
    return d + 'a4 4 0 0 0 -4 4v2'


def circle(cx, cy, r):
    return f'M{fmt(cx - r)} {fmt(cy)}a{fmt(r)} {fmt(r)} 0 1 0 {fmt(2 * r)} 0a{fmt(r)} {fmt(r)} 0 1 0 {fmt(-2 * r)} 0'


@design('family')
def family(animated):
    adult = [p(circle(*ADULT_HEAD)), p(adult_body())]
    child = [p(circle(*CHILD_HEAD)), p(child_body())]
    if not animated:
        return adult + child
    hop = keys('transform', [(0, '0 0'), (0.15, '0 -0.8'), (0.3, '0 0'), (0.42, '0 -0.4'), (0.54, '0 0'), (1, '0 0')], 2.4, kind='translate')
    return adult + [group(child, hop)]


def build(label):
    fn, rot = DESIGNS[label]
    static = wrapped(fn(False), rot)
    for v, w in (('small', 2), ('large', 1), ('very-large', 0.5)):
        write(f'{SVG_DIR}/{label}-{v}.svg', header(label, w), [FRAME] + static)
    write(f'{SVG_DIR}/{label}-animated.svg', header(label, 1, 'animated'), [FRAME] + wrapped(fn(True), rot))


if __name__ == '__main__':
    for label in sys.argv[1:] or DESIGNS:
        build(label)
        print('forged', label)

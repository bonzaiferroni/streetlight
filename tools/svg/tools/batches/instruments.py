"""Forged instruments for InstrumentSvg values whose icon stood in for another instrument (forge.py's helpers).

Usage: python3 instruments.py [labels...]   (default: all of these). Tabler style on the 24 grid.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import fmt, group, osc, EASE
from forge import design, build, p, keys

LABELS = []


def instrument(label, rot=0):
    LABELS.append(label)
    return design(label, rot)


def hits(pairs, dur, kind, attr='transform'):
    """A keyed animation that rests between strikes: [pairs] of (time fraction, value)."""
    return keys(attr, pairs, dur, kind=kind)


# ---------- bagpipes: a bag tucked under the arm, three drones over the shoulder, a chanter below ----------
def tilted_oval(cx, cy, rx, ry, deg):
    a = math.radians(deg)
    x0, y0 = cx - rx * math.cos(a), cy - rx * math.sin(a)
    x1, y1 = cx + rx * math.cos(a), cy + rx * math.sin(a)
    return (f'M{fmt(x0)} {fmt(y0)}A{fmt(rx)} {fmt(ry)} {fmt(deg)} 1 0 {fmt(x1)} {fmt(y1)}'
            f'A{fmt(rx)} {fmt(ry)} {fmt(deg)} 1 0 {fmt(x0)} {fmt(y0)}')


BAG = tilted_oval(10.5, 15.5, 5.2, 3.8, -28)
def on_oval(cx, cy, rx, ry, deg, t):
    """The point at parameter [t] degrees on an oval tilted [deg]."""
    a, u = math.radians(deg), math.radians(t)
    x, y = rx * math.cos(u), ry * math.sin(u)
    return cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a)


DRONES = [(on_oval(10.5, 15.5, 5.2, 3.8, -28, t), length, lean) for t, length, lean in ((-75, 8.4, 18), (-45, 7.6, 30), (-15, 6.6, 42))]
BLOW_BASE, CHANTER_BASE = on_oval(10.5, 15.5, 5.2, 3.8, -28, -125), on_oval(10.5, 15.5, 5.2, 3.8, -28, 155)
BLOWPIPE = f'M{fmt(BLOW_BASE[0])} {fmt(BLOW_BASE[1])}L4 7.5'
CHANTER = f'M{fmt(CHANTER_BASE[0])} {fmt(CHANTER_BASE[1])}L5.5 21.5'


def drone(base, length, lean):
    a = math.radians(lean)
    x, y = base[0] + length * math.sin(a), base[1] - length * math.cos(a)
    cx, cy = math.cos(a) * 0.9, math.sin(a) * 0.9
    return f'M{fmt(base[0])} {fmt(base[1])}L{fmt(x)} {fmt(y)}M{fmt(x - cx)} {fmt(y - cy)}L{fmt(x + cx)} {fmt(y + cy)}'


@instrument('bagpipes')
def bagpipes(animated):
    base = [p(BLOWPIPE), p(CHANTER)]
    drones = [drone(*d) for d in DRONES]
    if not animated:
        return base + [p(BAG)] + [p(d) for d in drones]
    # the bag is squeezed and refilled; the drones sway from where they leave it, out of step
    squeeze = group([group([p(BAG)], attrs='transform="translate(-10.5 -15.5)"')], osc('transform', ['1 1', '1.05 0.93'], 2.4, kind='scale'))
    out = base + [group([squeeze], attrs='transform="translate(10.5 15.5)"')]
    for i, ((bx, by), _, _) in enumerate(DRONES):
        c = f'{fmt(bx)} {fmt(by)}'
        sway = 3 if i % 2 == 0 else -3
        out.append(group([p(drones[i])], osc('transform', [f'0 {c}', f'{sway} {c}', f'0 {c}', f'{-sway} {c}'], 2.4, kind='rotate')))
    return out


# ---------- harmonica: a bar of holes under cover plates; it slides along as it's played ----------
HARMONICA_CASE = 'M3 10a1 1 0 0 1 1 -1h16a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-16a1 1 0 0 1 -1 -1l0 -4'
HARMONICA_PLATES = 'M3 10.8h18m-18 2.4h18'
HARMONICA_HOLES = ''.join(f'M{fmt(x)} 11.6v.8' for x in (5.5, 8, 10.5, 13, 15.5, 18))


@instrument('harmonica')
def harmonica(animated):
    base = [p(HARMONICA_CASE), p(HARMONICA_PLATES), p(HARMONICA_HOLES)]
    if not animated:
        return base
    return [group(base, osc('transform', ['0 0', '-1.5 0', '0 0', '1.5 0'], 3.2, kind='translate'))]


# ---------- tambourine: a hoop with jingles set in it; a shake, then rest ----------
JINGLES = [(12 + 7.5 * math.cos(math.radians(a)), 12 + 7.5 * math.sin(math.radians(a))) for a in (-90, -18, 54, 126, 198)]


def jingle(x, y):
    return f'M{fmt(x - 1.4)} {fmt(y)}a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0'


@instrument('tambourine')
def tambourine(animated):
    hoop = [p('M4.5 12a7.5 7.5 0 1 0 15 0a7.5 7.5 0 1 0 -15 0')]
    jingles = [p(jingle(x, y)) for x, y in JINGLES]
    if not animated:
        return hoop + jingles
    shake = [(0, '0 12 12'), (0.06, '-9 12 12'), (0.14, '8 12 12'), (0.22, '-6 12 12'), (0.3, '4 12 12'), (0.38, '0 12 12'), (1, '0 12 12')]
    rattled = []
    for i, (x, y) in enumerate(JINGLES):
        c = f'{fmt(x)} {fmt(y)}'
        rattled.append(group([p(jingle(x, y))], hits([(0, f'0 {c}'), (0.08, f'40 {c}'), (0.18, f'-30 {c}'), (0.3, f'15 {c}'), (0.42, f'0 {c}'), (1, f'0 {c}')], 2.4, 'rotate')))
    return [group(hoop + rattled, hits(shake, 2.4, 'rotate'))]


# ---------- cymbals: a crash on its stand; struck, it wobbles and settles ----------
CYMBAL = 'M3.5 9.5c2.6 -1.3 5.6 -2 8.5 -2s5.9 .7 8.5 2c-2.6 .9 -5.6 1.4 -8.5 1.4s-5.9 -.5 -8.5 -1.4'
CYMBAL_BELL = 'M10.3 7.8c.2 -1 .9 -1.7 1.7 -1.7s1.5 .7 1.7 1.7'
STAND = 'M12 11v7.5m0 0l-4 2.5m4 -2.5l4 2.5m-4 -2.5v2.5'


@instrument('cymbals')
def cymbals(animated):
    top = [p(CYMBAL), p(CYMBAL_BELL)]
    if not animated:
        return top + [p(STAND)]
    wobble = [(0, '0 12 10'), (0.05, '10 12 10'), (0.15, '-7 12 10'), (0.26, '5 12 10'), (0.38, '-3 12 10'), (0.5, '1.5 12 10'), (0.6, '0 12 10'), (1, '0 12 10')]
    return [p(STAND), group(top, hits(wobble, 2.6, 'rotate'))]


# ---------- cajon: a box drum, its playing face toward us; boom, then tak ----------
CAJON_FRONT = 'M5 8h10v13h-10z'
CAJON_TOP = 'M5 8l4 -3h10l-4 3'
CAJON_SIDE = 'M15 8l4 -3v13l-4 3'
CAJON_SCREWS = 'M6.5 9.5h.01m6.99 0h.01'
CAJON_PORT = 'M16 12.8a1 1.7 0 1 0 2 0a1 1.7 0 1 0 -2 0'


@instrument('cajon')
def cajon(animated):
    body = [p(CAJON_FRONT), p(CAJON_TOP), p(CAJON_SIDE), p(CAJON_SCREWS), p(CAJON_PORT)]
    if not animated:
        return body
    # the face flexes as it's struck low (boom) and high at the corner (tak)
    beat = [(0, '1 1'), (0.04, '1.06 0.94'), (0.16, '1 1'), (0.5, '1 1'), (0.53, '1.03 0.97'), (0.62, '1 1'), (1, '1 1')]
    inner = group(body, attrs='transform="translate(-12 -21)"')
    return [group([group([inner], hits(beat, 1.6, 'scale'))], attrs='transform="translate(12 21)"')]


# ---------- xylophone: bars falling in length; two mallets play a phrase across all four, the struck bar dipping ----------
BARS = [(4.2 + 4 * i, 9 + 1.1 * i, 21 - 1.1 * i) for i in range(4)]
PHRASE = [0, 3, 1, 2, 0, 2, 1, 3]  # a bar per beat; hands alternate, left first, the left taking bars 0 and 1
XYLO_DUR = 3.2


def bar(x, top, bottom):
    return f'M{fmt(x)} {fmt(top + 1.2)}a1.2 1.2 0 0 1 2.4 0v{fmt(bottom - top - 2.4)}a1.2 1.2 0 0 1 -2.4 0z'


def mallet(hand, i, struck):
    """The [hand] mallet (-1 left, 1 right) over bar [i], resting on it when [struck]; its stick leans outward."""
    x, top, _ = BARS[i]
    hx, hy = x + 1.2 + 0.5 * hand * (0 if struck else 1), top - (1.4 if struck else 3.1)
    gx, gy = hx + 2 * hand, hy - 3
    ex, ey = hx + 0.55 * hand, hy - 0.83
    return f'M{fmt(gx)} {fmt(gy)}L{fmt(ex)} {fmt(ey)}M{fmt(hx - 1)} {fmt(hy)}a1 1 0 1 0 2 0a1 1 0 1 0 -2 0'


def strokes(hand):
    """Keyed mallet paths for one hand: hovering over its next bar, dropping onto it on its beat."""
    beats = [(k, b) for k, b in enumerate(PHRASE) if (k % 2 == 0) == (hand < 0)]
    first = beats[0][1]
    pairs = [(0, mallet(hand, first, False))]
    for k, b in beats:
        t = (k + 0.5) / len(PHRASE)
        pairs += [(t - 0.045, mallet(hand, b, False)), (t, mallet(hand, b, True)), (t + 0.06, mallet(hand, b, False))]
    return pairs + [(1, mallet(hand, first, False))]


@instrument('xylophone')
def xylophone(animated):
    bars = [bar(*b) for b in BARS]
    if not animated:
        return [p(b) for b in bars] + [p(mallet(-1, PHRASE[0], False)), p(mallet(1, PHRASE[1], False))]
    out = []
    for i, b in enumerate(bars):
        dips = [(0, '0 0')]
        for k, bi in enumerate(PHRASE):
            if bi == i:
                t = (k + 0.5) / len(PHRASE)
                dips += [(t, '0 0'), (t + 0.02, '0 0.45'), (t + 0.06, '0 0')]
        out.append(group([p(b)], hits(dips + [(1, '0 0')], XYLO_DUR, 'translate')))
    for hand in (-1, 1):
        pairs = strokes(hand)
        out.append(f'<path d="{pairs[0][1]}">\n    {keys("d", pairs, XYLO_DUR)}\n</path>')
    return out


# ---------- trombone: a bell over a long slide; the slide reaches out and back ----------
TROMBONE_BELL_TUBE = 'M8.5 7.5h6.5'
TROMBONE_BELL = 'M15 7.5l5.5 -3v6l-5.5 -3'
TROMBONE_CROOK = 'M8.5 7.5a2.25 2.25 0 0 0 0 4.5'
TROMBONE_INNER = 'M8.5 12h7.5m-7.5 2.5h7.5'
TROMBONE_MOUTH = 'M10.5 14.5v2.5'
TROMBONE_SLIDE = 'M12 12h5a1.25 1.25 0 0 1 0 2.5h-5m4 -2.5v2.5'


@instrument('trombone')
def trombone(animated):
    base = [p(TROMBONE_BELL_TUBE), p(TROMBONE_BELL), p(TROMBONE_CROOK), p(TROMBONE_INNER), p(TROMBONE_MOUTH)]
    if not animated:
        return base + [p(TROMBONE_SLIDE)]
    return base + [group([p(TROMBONE_SLIDE)], osc('transform', ['0 0', '3 0'], 2.4, kind='translate'))]


# ---------- tuba: a bell open to the sky over a coiled body; it rocks on the beat as its valves press ----------
TUBA_BELL = 'M7 3h10m-10 0c1.6 .6 2.6 2.4 2.8 6m7.2 -6c-1.6 .6 -2.6 2.4 -2.8 6'
TUBA_BODY = 'M9.8 9c-1.6 .7 -2.8 2.8 -2.8 5.5c0 2.8 2.2 5.5 5 5.5s5 -2.7 5 -5.5c0 -2.7 -1.2 -4.8 -2.8 -5.5'
TUBA_MOUTH = 'M7.2 13h-2.2l-1 -1.5'
TUBA_VALVES = [11, 13]


@instrument('tuba')
def tuba(animated):
    base = [p(TUBA_BELL), p(TUBA_BODY), p(TUBA_MOUTH)]
    valves = [p(f'M{fmt(x)} 15.5v-3.5') for x in TUBA_VALVES]
    if not animated:
        return base + valves
    pressed = []
    for i, v in enumerate(valves):
        a = 0.1 + i * 0.25
        pressed.append(group([v], keys('transform', [(0, '0 0'), (a, '0 0'), (a + 0.08, '0 1'), (a + 0.2, '0 0'), (1, '0 0')], 2, kind='translate')))
    rock = osc('transform', ['0 12 20', '3 12 20', '0 12 20', '-3 12 20'], 2, kind='rotate')
    return [group(base + pressed, rock)]


# ---------- banjo: a drum-headed pot on a long neck, the fifth peg partway up; drawn upright, shown on the diagonal ----------
BANJO_POT = 'M7 16a5 5 0 1 0 10 0a5 5 0 1 0 -10 0'
BANJO_NECK = 'M11.3 11.05v-6.85m1.4 0v6.85'
BANJO_HEAD = 'M11 4.2l-.3 -2.2h2.6l-.3 2.2z'
BANJO_PEG = 'M11.3 8h-1'
BANJO_BRIDGE = 'M10.9 17.6h2.2'


@instrument('banjo', rot=40)
def banjo(animated):
    base = [p(BANJO_POT), p(BANJO_NECK), p(BANJO_HEAD), p(BANJO_PEG), p(BANJO_BRIDGE)]
    shape = lambda a: f'M12 4.2q{fmt(a)} 6.7 0 13.4'
    if not animated:
        return base + [p(shape(0))]
    # a bright pluck: the string snaps out and rings down quickly, twice
    pairs = [(0, shape(0)), (0.04, shape(1.8)), (0.09, shape(-1.3)), (0.14, shape(0.8)), (0.2, shape(-0.35)), (0.26, shape(0)),
             (0.5, shape(0)), (0.54, shape(1.5)), (0.59, shape(-1)), (0.65, shape(0.5)), (0.72, shape(0)), (1, shape(0))]
    return base + [f'<path d="{shape(0)}">\n    {keys("d", pairs, 2)}\n</path>']


# ---------- hand drum: a djembe, goblet body under a roped head; the head gives under two strikes ----------
DJEMBE_BODY = 'M6 5.5c0 4 3.5 5.2 3.5 8.5c0 3 -1.5 4.5 -1.5 7h8c0 -2.5 -1.5 -4 -1.5 -7c0 -3.3 3.5 -4.5 3.5 -8.5'
DJEMBE_ROPE = 'M7 8.2l2.2 3.3l1.4 -3.8l1.4 4l1.4 -4l1.4 3.8l2.2 -3.3'


def djembe_head(ry):
    return f'M6 5.5a6 {fmt(ry)} 0 1 0 12 0a6 {fmt(ry)} 0 1 0 -12 0'


@instrument('hand-drum')
def hand_drum(animated):
    base = [p(DJEMBE_BODY), p(DJEMBE_ROPE)]
    if not animated:
        return base + [p(djembe_head(1.8))]
    beat = [(0, djembe_head(1.8)), (0.04, djembe_head(1.1)), (0.14, djembe_head(1.8)), (0.32, djembe_head(1.8)),
            (0.36, djembe_head(1.3)), (0.46, djembe_head(1.8)), (1, djembe_head(1.8))]
    return base + [f'<path d="{djembe_head(1.8)}">\n    {keys("d", beat, 1.8)}\n</path>']


if __name__ == '__main__':
    for label in sys.argv[1:] or LABELS:
        build(label)
        print('forged', label)

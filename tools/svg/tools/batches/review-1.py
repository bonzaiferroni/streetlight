"""Navigator review round 1: presentation, needle-thread, tools-kitchen, rainbow, building-church."""
import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE, fmt


def head(label):
    large = open(f'{SVG_DIR}/{label}-large.svg').read()
    return large.split('\n    <path stroke="none"')[0].replace('icons-tabler-outline', 'icons-tabler-animated')


def write(label, body):
    lines = ['<path stroke="none" d="M0 0h24v24H0z" fill="none"/>'] + body
    open(f'{SVG_DIR}/{label}-animated.svg', 'w').write(head(label) + '\n' + '\n'.join('    ' + l for l in '\n'.join(lines).split('\n')) + '\n</svg>\n')
    print(label)


def anim(attr, pairs, dur, kind=None):
    if pairs[0][0] > 0:
        pairs = [(0, pairs[0][1])] + pairs
    if pairs[-1][0] < 1:
        pairs = pairs + [(1, pairs[-1][1])]
    tag = 'animateTransform' if kind else 'animate'
    return (f'<{tag} attributeName="{attr}"' + (f' type="{kind}"' if kind else '') +
            f' values="{";".join(v for _, v in pairs)}" keyTimes="{";".join(fmt(t) for t, _ in pairs)}" dur="{dur}s"'
            f' repeatCount="indefinite" calcMode="spline" keySplines="{";".join([EASE] * (len(pairs) - 1))}"/>')


def dashed(d, pairs, dur):
    """[d] shown by its dash: 0 whole, 1.1 not yet drawn from its start, -1.1 undrawn past its end."""
    return f'<path d="{d}" pathLength="1" stroke-dasharray="1 1.1">\n    {anim("stroke-dashoffset", pairs, dur)}\n</path>'


def draw_cycle(hold, undrawn, hidden, drawn):
    """Whole until [hold], its tail follows to undraw it by [undrawn], hidden until [hidden], drawn again by [drawn]."""
    return [(0, '0'), (hold, '0'), (undrawn, '-1.1'), (undrawn + 0.001, '1.1'), (hidden, '1.1'), (drawn, '0')]


# ---------- presentation (Lecture): the chart line draws left to right, holds, and its tail follows to undraw it ----------
write('presentation', [
    '<path d="M3 4l18 0"/>',
    '<path d="M4 4v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2 -2v-10"/>',
    '<path d="M12 16l0 4"/>',
    '<path d="M9 20l6 0"/>',
    dashed('M8 12l3 -3l2 2l3 -3', draw_cycle(0.45, 0.65, 0.72, 1), 4),
])


# ---------- needle-thread (Crafting): the thread draws from its left end through the eye, then its tail follows ----------
def bezier_len(p0, p1, p2, p3, n=200):
    pts = [tuple((1 - t) ** 3 * a + 3 * (1 - t) ** 2 * t * b + 3 * (1 - t) * t ** 2 * c + t ** 3 * d
                 for a, b, c, d in zip(p0, p1, p2, p3)) for t in (i / n for i in range(n + 1))]
    return sum(math.dist(pts[i], pts[i + 1]) for i in range(n))


TAIL = 'M2 12C2 13.5 4.346 14.86 5.739 15.425'
LEAD = 'M10.25 16.808C12.637 17.285 14.944 17.268 16 16C18.161 13.406 10 9 10 5C10 4 10.5 3 12 3C13.5 3 14.667 4.333 17 7'
tail_len = bezier_len((2, 12), (2, 13.5), (4.346, 14.86), (5.739, 15.425))
EYE_END = 'M19.5 9.5l1.5 1.5'
end_len = math.hypot(1.5, 1.5)
lead_len = sum(bezier_len(*seg) for seg in [((10.25, 16.808), (12.637, 17.285), (14.944, 17.268), (16, 16)),
                                             ((16, 16), (18.161, 13.406), (10, 9), (10, 5)),
                                             ((10, 5), (10, 4), (10.5, 3), (12, 3)),
                                             ((12, 3), (13.5, 3), (14.667, 4.333), (17, 7))])
total = tail_len + lead_len + end_len
cuts = [tail_len / total, (tail_len + lead_len) / total]
HOLD, UNDRAWN, HIDDEN, DRAWN = 0.3, 0.6, 0.66, 1
u1, u2 = (HOLD + (UNDRAWN - HOLD) * c for c in cuts)
d1, d2 = (HIDDEN + (DRAWN - HIDDEN) * c for c in cuts)
write('needle-thread', [
    '<path d="M3 21c-.667 -.667 3.262 -6.236 11.785 -16.709a3.5 3.5 0 1 1 5.078 4.791c-10.575 8.612 -16.196 12.585 -16.863 11.918"/>',
    '<path d="M17.5 6.5l-1 1"/>',
    dashed(TAIL, draw_cycle(HOLD, u1, HIDDEN, d1), 5),
    dashed(LEAD, draw_cycle(u1, u2, d1, d2), 5),
    dashed(EYE_END, draw_cycle(u2, UNDRAWN, d2, DRAWN), 5),
])


# ---------- tools-kitchen (Food & Drink): the fork lifts and settles, then the knife, once each loop ----------
def lift(start):
    return anim('transform', [(start, '0 0'), (start + 0.1, '0 -1.2'), (start + 0.2, '0 0')], 5, kind='translate')


write('tools-kitchen', [
    f'<g>\n    {lift(0.1)}\n    <path d="M8 4v17m-3 -17v3a3 3 0 1 0 6 0v-3"/>\n</g>',
    f'<g>\n    {lift(0.4)}\n    <path d="M19 3v12h-5c-.023 -3.681 .184 -7.406 5 -12m0 12v6h-1v-3"/>\n</g>',
])


# ---------- rainbow (LGBTQ): a wave of fading runs outward through the bands, each fade starting before the last ends; then rest ----------
BANDS = ['M14 17a2 2 0 1 0 -4 0', 'M18 17a6 6 0 1 0 -12 0', 'M22 17c0 -5.523 -4.477 -10 -10 -10c-5.523 0 -10 4.477 -10 10']
write('rainbow', [f'<path d="{d}">\n    {anim("opacity", [(0.02 + 0.15 * i, "1"), (0.17 + 0.15 * i, "0.25"), (0.32 + 0.15 * i, "1")], 3.6)}\n</path>'
                  for i, d in enumerate(BANDS)])


# ---------- building-church (Church): the cross swells and eases back; the door keeps opening ----------
# the leaf starts on the left half of the arch and straightens as it swings to the right jamb, as tall as the frame,
# then fades; it returns unseen on the frame
swell = anim('transform', [(0, '1'), (0.5, '1.12'), (1, '1')], 2, kind='scale')
LEAF_SHUT, LEAF_OPEN = 'M10 21V17C10 15.9 10.9 15 12 15', 'M14 21V17C14 16.33 14 15.67 14 15'
door = (anim('d', [(0.15, LEAF_SHUT), (0.6, LEAF_OPEN), (0.8, LEAF_OPEN), (0.801, LEAF_SHUT)], 6) + '\n    ' +
        anim('opacity', [(0.6, '1'), (0.8, '0'), (0.801, '1')], 6))
write('building-church', [
    '<path d="M3 21l18 0"/>',
    '<path d="M10 21v-4a2 2 0 0 1 4 0v4"/>',
    f'<path d="{LEAF_SHUT}">\n    {door}\n</path>',
    '<path d="M6 21v-7m-2 2l8 -8l8 8m-2 -2v7"/>',
    f'<g transform="translate(12 5.5)">\n    <g>\n        {swell}\n        <g transform="translate(-12 -5.5)">\n'
    f'            <path d="M10 5l4 0"/>\n            <path d="M12 3l0 5"/>\n        </g>\n    </g>\n</g>',
])

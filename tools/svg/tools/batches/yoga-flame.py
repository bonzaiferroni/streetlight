"""Yoga (Wellness), every variant, with the flame head. Replaces yoga-legs.py and yoga-warrior.py.

The figure is drawn on its old coordinates, then scaled by SCALE and lowered by DY to make room for the flame's tips.
Animated: the arms rise as the body sinks into the pose by half as much; the head rides with it
and the legs bend deeper while the feet stay planted.
"""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE, fmt, flame_head, affine

SCALE, DY = 0.9, 1.75
HEAD = (12, 4.5, 2.5)  # flame body center and radius; its bottom sits where the old circle's did
SMALL_HEAD_STROKE = 1.5
DUR = 4
SINK = 0.75
HIP, BEND, DROOP = 1.8, 0.5, 0.35


def front(dy, cx, cy):
    """The front leg arcs (control (cx, cy)) to a rounded seat, then the back bends gently up to the shoulders."""
    h = 15 + dy
    return (f'M17 20Q{fmt(cx)} {fmt(cy)} {fmt(11 + HIP)} {fmt(h)}'
            f'C{fmt(11 + HIP * 0.3)} {fmt(h)} 10.75 {fmt(h - HIP * 0.45)} 10.8 {fmt(h - HIP)}'
            f'C10.85 {fmt(12.2 + dy)} {fmt(11.6 - BEND)} {fmt(10 + dy)} 12 {fmt(8 + dy)}')


def arms(left, shoulder, right):
    """One smooth line through the shoulders at height [shoulder]; each arm bows up by DROOP so the hands fall away."""
    k = (right - left) / 16
    return (f'M4 {fmt(left)}C6.7 {fmt(left + (shoulder - left) / 3 - DROOP)} 9.3 {fmt(shoulder - 2.7 * k)} 12 {fmt(shoulder)}'
            f'C14.7 {fmt(shoulder + 2.7 * k)} 17.3 {fmt(shoulder + (right - shoulder) * 2 / 3 - DROOP)} 20 {fmt(right)}')


FRAMES = {  # rest, then sunk
    'leg_back': ('M4 20Q7.71 20.26 9.5 17', 'M4 20Q7.9 20.4 9.5 17.75'),
    'leg_front': (front(0, 15.92, 15.2), front(SINK, 16.1, 15.75)),
    'arms': (arms(10, 8, 11), arms(8.6, 8.75, 9.4)),
}


def place(d):
    return affine(d, SCALE, dy=DY)


def head_d():
    cx, cy, r = HEAD
    return place(flame_head(cx, cy, r))


def svg_open(width, kind):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" '
            f'stroke="currentColor"\n     stroke-width="{width}" stroke-linecap="round" stroke-linejoin="round"\n'
            f'     class="icon icon-tabler icons-tabler-{kind} icon-tabler-yoga">')


def write(variant, width, kind, body):
    lines = '\n'.join(body).split('\n')
    open(f'{SVG_DIR}/yoga-{variant}.svg', 'w').write(
        svg_open(width, kind) + '\n' + '\n'.join('    ' + l for l in lines) + '\n</svg>\n')


def anim(attr, values, dur, kind=None):
    vals = values + [values[0]]
    tag = f'animateTransform attributeName="transform" type="{kind}"' if kind else f'animate attributeName="{attr}"'
    keys = ';'.join(fmt(i / (len(vals) - 1)) for i in range(len(vals)))
    return (f'<{tag} values="{";".join(vals)}" keyTimes="{keys}" dur="{dur}s" repeatCount="indefinite" '
            f'calcMode="spline" keySplines="{";".join([EASE] * (len(vals) - 1))}"/>')


FRAME = '<path stroke="none" d="M0 0h24v24H0z" fill="none"/>'
for variant, width in (('small', 2), ('large', 1), ('very-large', 0.5)):
    stroke = f' stroke-width="{SMALL_HEAD_STROKE}"' if variant == 'small' else ''
    write(variant, width, 'outline',
          [FRAME] + [f'<path d="{place(rest)}"/>' for rest, _ in FRAMES.values()] + [f'<path{stroke} d="{head_d()}"/>'])

body = [FRAME]
for rest, sunk in FRAMES.values():
    body.append(f'<path d="{place(rest)}">\n    {anim("d", [place(rest), place(sunk)], DUR)}\n</path>')
sink = anim(None, ['0 0', f'0 {fmt(SINK * SCALE)}'], DUR, kind='translate')
body.append(f'<g>\n    {sink}\n    <path d="{head_d()}"/>\n</g>')
write('animated', 1, 'animated', body)
print('yoga')

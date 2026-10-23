"""Building store: the awning's flaps sway in turn from right to left, as a breeze passes, then hang still.
Each flap skews about the awning's edge at y 7, so its top stays on the roof."""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE, fmt

DUR = 3.6
SWAY = [0, -18, 6, -1.5, 0]  # degrees of skew through one gust
SPAN = 0.4  # fraction of the loop one flap's sway takes
LAG = 0.12  # how far each flap trails the one to its right


def flap(x, start):
    ts = [0] + [start + SPAN * f for f in (0, 0.3, 0.6, 0.85, 1)] + [1]
    vs = [0] + SWAY + [0]
    anim = (f'<animateTransform attributeName="transform" type="skewX" values="{";".join(fmt(v) for v in vs)}" '
            f'keyTimes="{";".join(fmt(t) for t in ts)}" dur="{DUR}s" repeatCount="indefinite" calcMode="spline" '
            f'keySplines="{";".join([EASE] * (len(ts) - 1))}"/>')
    return (f'    <g transform="translate(0 7)">\n        <g>\n            {anim}\n'
            f'            <path transform="translate(0 -7)" d="M{x} 7v1a3 3 0 0 0 6 0v-1"/>\n        </g>\n    </g>')


large = open(f'{SVG_DIR}/building-store-large.svg').read()
head = large.split('\n    <path stroke="none"')[0].replace('icons-tabler-outline', 'icons-tabler-animated')
body = [
    '    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>',
    '    <path d="M3 21l18 0"/>',
    '    <path d="M3 7h18l-2 -4h-14l-2 4"/>',
] + [flap(x, 0.05 + LAG * i) for i, x in enumerate((15, 9, 3))] + [
    '    <path d="M5 21l0 -10.15"/>',
    '    <path d="M19 21l0 -10.15"/>',
    '    <path d="M9 21v-4a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v4"/>',
]
open(f'{SVG_DIR}/building-store-animated.svg', 'w').write(head + '\n' + '\n'.join(body) + '\n</svg>\n')
print('building-store')

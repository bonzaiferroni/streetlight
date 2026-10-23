"""Yoga (Wellness) animated: the arms rise as the body sinks into the pose by half as much; the head rides with it and
the legs bend deeper while the feet stay planted."""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE

DUR = 4
FRAMES = {  # rest, then sunk
    'leg_back': ('M4 20Q7.71 20.26 9.5 17', 'M4 20Q7.9 20.4 9.5 17.75'),
    'leg_front': ('M17 20Q15.92 15.2 11 15L12 8', 'M17 20Q16.1 15.75 11 15.75L12 8.75'),
    'arms': ('M4 10L8 9L12 8L16 9.5L20 11', 'M4 8.6L8 8.675L12 8.75L16 9.075L20 9.4'),
}


def morph(rest, sunk):
    return (f'<animate attributeName="d" values="{rest};{sunk};{rest}" keyTimes="0;0.5;1" dur="{DUR}s" '
            f'repeatCount="indefinite" calcMode="spline" keySplines="{EASE};{EASE}"/>')


large = open(f'{SVG_DIR}/yoga-large.svg').read()
head = large.split('\n    <path stroke="none"')[0].replace('icons-tabler-outline', 'icons-tabler-animated')
body = ['<path stroke="none" d="M0 0h24v24H0z" fill="none"/>']
body += [f'<path d="{rest}">\n    {morph(rest, sunk)}\n</path>' for rest, sunk in FRAMES.values()]
sink = (f'<animateTransform attributeName="transform" type="translate" values="0 0;0 0.75;0 0" keyTimes="0;0.5;1" '
        f'dur="{DUR}s" repeatCount="indefinite" calcMode="spline" keySplines="{EASE};{EASE}"/>')
body.append(f'<g>\n    {sink}\n    <path d="M10.007 5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"/>\n</g>')
lines = '\n'.join(body).split('\n')
open(f'{SVG_DIR}/yoga-animated.svg', 'w').write(head + '\n' + '\n'.join('    ' + l for l in lines) + '\n</svg>\n')
print('yoga')

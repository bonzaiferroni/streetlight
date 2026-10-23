"""Trekking (Hike), reworked: no forward arm, a gap between the back arm and the body, the front leg and the right
arm each one gentle arc, the head nearer the body under a hat. Only the right arm and the stick move.
Run smooth.py on trekking-animated.svg after."""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, osc, group

HEAD = 'M10.8 6a1 1 0 1 0 2 0a1 1 0 1 0 -2 0'
HAT = 'M9.8 4.4h4.2M10.8 4.4v-.9a1 .8 0 0 1 2 0v.9'
LEG_BACK = 'M7 21l2 -4'
LEG_AND_TORSO = 'M13 21Q13.15 16.79 10 14l1 -6'
ARM_BACK = 'M8.9 13.27l-.727 -.488a2 2 0 0 1 -.831 -2.15l.28 -1.117a2 2 0 0 1 1.939 -1.515h.1'
ARM = 'M11 8Q13.15 11.85 17 14'
STICK = 'M17 12v9M16 20h2'

large = open(f'{SVG_DIR}/trekking-large.svg').read()
head = large.split('\n    <path stroke="none"')[0]
frame = '<path stroke="none" d="M0 0h24v24H0z" fill="none"/>'
still = [HEAD, HAT, LEG_BACK, LEG_AND_TORSO, ARM_BACK]


def write(variant, width, kind, body):
    h = head.replace('stroke-width="1"', f'stroke-width="{width}"').replace('icons-tabler-outline', f'icons-tabler-{kind}')
    lines = '\n'.join(body).split('\n')
    open(f'{SVG_DIR}/trekking-{variant}.svg', 'w').write(h + '\n' + '\n'.join('    ' + l for l in lines) + '\n</svg>\n')


for variant, width in (('small', 2), ('large', 1), ('very-large', 0.5)):
    write(variant, width, 'outline', [frame] + [f'<path d="{d}"/>' for d in still + [ARM, STICK]])
swing = osc('transform', ['0 11 8', '-4 11 8', '0 11 8', '3 11 8'], 1.6, kind='rotate')
write('animated', 1, 'animated', [frame] + [f'<path d="{d}"/>' for d in still] + [group([f'<path d="{ARM}"/>', f'<path d="{STICK}"/>'], swing)])
print('trekking')

"""The point: Tabler's `point`, a field's icon when nothing else fits. Its animation is the fallback, a slow grow
and shrink.

Usage: python3 point.py
"""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, pulse
from forge import header, write, FRAME

DOT = '<path d="M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"/>'

for variant, width in (('small', 2), ('large', 1), ('very-large', 0.5)):
    write(f'{SVG_DIR}/point-{variant}.svg', header('point', width), [FRAME, DOT])
write(f'{SVG_DIR}/point-animated.svg', header('point', 1, 'animated'), [FRAME, pulse([DOT], amount=0.12, dur=4)])
print('point')

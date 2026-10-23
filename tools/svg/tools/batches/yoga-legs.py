"""Yoga: each leg one gentle arc between its old endpoints, in every variant."""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR

SWAPS = [
    ('<path d="M4 20h4l1.5 -3"/>', '<path d="M4 20Q7.71 20.26 9.5 17"/>'),
    ('<path d="M17 20l-1 -5h-5l1 -7"/>', '<path d="M17 20Q15.92 15.2 11 15l1 -7"/>'),
]
for v in ('small', 'large', 'very-large', 'animated'):
    path = f'{SVG_DIR}/yoga-{v}.svg'
    s = open(path).read()
    for old, new in SWAPS:
        s = s.replace(old, new)
    open(path, 'w').write(s)
    print(v, all(new in s for _, new in SWAPS))

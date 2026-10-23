"""The flame pack, from www/svg/flame.svg: small/large/very-large/filled plus animated, on the flame's own viewBox.
The animated tips rise and settle out of phase."""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE, fmt

VIEWBOX = '3.708 0 16.732 22.754'


def flame_d(a=0.0, b=0.0):
    """The flame with its top tip raised by [a] and its right tip by [b]."""
    return (f'M12 10.941C14.333 7.633 12.167 {fmt(3.118 - a)} 11 {fmt(2 - a)}'
            f'C11 {fmt(5.395 - a)} 8.765 7.299 7.333 8.706C5.903 10.114 5 12.327 5 14.294'
            f'C5 17.998 8.134 21 12 21C15.866 21 19 17.998 19 14.294'
            f'C19 12.582 {fmt(17.768)} {fmt(9.891 - b)} 16.667 {fmt(8.706 - b)}'
            f'C14.583 {fmt(12.059 - b)} 13.41 12.059 12 10.941')


def head(width, kind):
    stroke = 'fill="currentColor" stroke="none"' if kind == 'filled' else f'fill="none" stroke="currentColor" stroke-width="{width}" stroke-linecap="round"'
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{VIEWBOX}" {stroke}\n     class="icon icon-streetlight icons-streetlight-{kind} icon-streetlight-flame">'


def write(variant, width, kind, body):
    open(f'{SVG_DIR}/flame-{variant}.svg', 'w').write(head(width, kind) + '\n    ' + body + '\n</svg>\n')


for variant, width in (('small', 2), ('large', 1), ('very-large', 0.5)):
    write(variant, width, 'outline', f'<path d="{flame_d()}"/>')
write('filled', 0, 'filled', f'<path d="{flame_d()}"/>')

TIMES = [0, 0.25, 0.5, 0.75, 1]
TOP = [0, 0.7, 0, -0.2, 0]
RIGHT = [0, -0.2, 0, 0.6, 0]
values = ';'.join(flame_d(a, b) for a, b in zip(TOP, RIGHT))
anim = (f'<animate attributeName="d" values="{values}" keyTimes="{";".join(fmt(t) for t in TIMES)}" dur="2.4s" '
        f'repeatCount="indefinite" calcMode="spline" keySplines="{";".join([EASE] * 4)}"/>')
write('animated', 1, 'animated', f'<path d="{flame_d()}">\n        {anim}\n    </path>')
print('forged flame')

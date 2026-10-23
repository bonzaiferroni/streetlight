"""Helpers for stick figures with the flame head (see notes.md, Stick Figures).

A figure is drawn in its own coordinates, then placed on the icon by a uniform scale about (12, 12) and a shift, so the
flame's tips fit. Paths are absolute M, L, Q, C and A only.
"""
import re
from anim import SVG_DIR, EASE, fmt, flame_head

CMD = re.compile(r'([MLQCA])([^MLQCA]*)')
NUM = re.compile(r'-?\d*\.?\d+')
ARGS = {'M': 2, 'L': 2, 'Q': 4, 'C': 6, 'A': 7}
FRAME = '<path stroke="none" d="M0 0h24v24H0z" fill="none"/>'


class Fig:
    """A figure placed by scale [s] about (12, 12), then moved by (dx, dy); [head] is the flame's (cx, cy, r)."""

    def __init__(self, label, head, s=1.0, dx=0.0, dy=0.0, small_head=1.5):
        self.label, self.headc, self.s, self.dx, self.dy, self.small_head = label, head, s, dx, dy, small_head

    def x(self, v):
        return (v - 12) * self.s + 12 + self.dx

    def y(self, v):
        return (v - 12) * self.s + 12 + self.dy

    def pt(self, x, y):
        """A placed point, as 'x y'."""
        return f'{fmt(self.x(x))} {fmt(self.y(y))}'

    def p(self, d):
        """Placed path [d]."""
        assert not re.search(r'[a-zHVSTZ]', d), d
        out = []
        for c, args in CMD.findall(d):
            n = [float(v) for v in NUM.findall(args)]
            assert len(n) % ARGS[c] == 0, (c, args)
            for i in range(0, len(n), ARGS[c]):
                g = n[i:i + ARGS[c]]
                if c == 'A':
                    vals = [fmt(g[0] * self.s), fmt(g[1] * self.s), fmt(g[2]), fmt(g[3]), fmt(g[4]),
                            fmt(self.x(g[5])), fmt(self.y(g[6]))]
                else:
                    vals = [fmt(self.x(v)) if k % 2 == 0 else fmt(self.y(v)) for k, v in enumerate(g)]
                out.append(c + ' '.join(vals))
        return ''.join(out)

    def head(self, ddx=0.0, ddy=0.0):
        """The placed flame head, moved by (ddx, ddy) in figure units."""
        cx, cy, r = self.headc
        return self.p(flame_head(cx + ddx, cy + ddy, r))

    def bounds(self, ds):
        """The placed bounding box of the points of paths [ds] (control points included), with the head."""
        xs, ys = [], []
        for d in list(ds) + [self.head()]:
            for c, args in CMD.findall(d):
                n = [float(v) for v in NUM.findall(args)]
                for i in range(0, len(n), ARGS[c]):
                    g = n[i:i + ARGS[c]]
                    pts = g[5:7] if c == 'A' else g
                    xs += pts[0::2]
                    ys += pts[1::2]
        return min(xs), min(ys), max(xs), max(ys)

    def open_tag(self, width, kind):
        src = open(f'{SVG_DIR}/{self.label}-large.svg').read()
        tag = re.search(r'<svg\b[^>]*>', src).group(0)
        tag = re.sub(r'stroke-width="[^"]*"', f'stroke-width="{width}"', tag)
        return re.sub(r'icons-tabler-\w+', f'icons-tabler-{kind}', tag)

    def write(self, parts, animated):
        """Writes small, large, very-large from placed [parts] (path d strings or markup, head added) and animated from body
        lines [animated] (markup)."""
        tags = {v: self.open_tag(w, k) for v, w, k in
                (('small', 2, 'outline'), ('large', 1, 'outline'), ('very-large', 0.5, 'outline'), ('animated', 1, 'animated'))}
        for variant in ('small', 'large', 'very-large'):
            stroke = f' stroke-width="{fmt(self.small_head)}"' if variant == 'small' else ''
            body = [FRAME] + [d if d.startswith('<') else f'<path d="{d}"/>' for d in parts] + [f'<path{stroke} d="{self.head()}"/>']
            emit(f'{SVG_DIR}/{self.label}-{variant}.svg', tags[variant], body)
        emit(f'{SVG_DIR}/{self.label}-animated.svg', tags['animated'], [FRAME] + animated)


def emit(path, tag, body):
    lines = '\n'.join(body).split('\n')
    open(path, 'w').write(tag + '\n' + '\n'.join('    ' + l for l in lines) + '\n</svg>\n')


def anim(values, dur, attr='d', kind=None, times=None, splines=None, begin=None):
    """An animate (or animateTransform of [kind]) through [values] and back to the first, eased, looping."""
    vals = list(values)
    if vals[-1] != vals[0]:
        vals.append(vals[0])
    n = len(vals)
    tag = f'animateTransform attributeName="transform" type="{kind}"' if kind else f'animate attributeName="{attr}"'
    keys = times or ';'.join(fmt(i / (n - 1)) for i in range(n))
    spl = splines or ';'.join([EASE] * (n - 1))
    b = f' begin="{begin}"' if begin else ''
    return (f'<{tag} values="{";".join(vals)}" keyTimes="{keys}" dur="{fmt(dur)}s"{b} repeatCount="indefinite" '
            f'calcMode="spline" keySplines="{spl}"/>')


def path(d, *anims):
    """A path with animations [anims]."""
    if not anims:
        return f'<path d="{d}"/>'
    return f'<path d="{d}">\n' + '\n'.join('    ' + a for a in anims) + '\n</path>'


def group(children, *anims):
    """A <g> holding [children] with animations [anims]."""
    inner = [('    ' + l) for c in list(anims) + list(children) for l in c.split('\n')]
    return '<g>\n' + '\n'.join(inner) + '\n</g>'


def rotate(d, deg, cx, cy):
    """Path [d] (absolute M, L, Q, C only) rotated by [deg] about (cx, cy)."""
    import math
    a = math.radians(deg)
    out = []
    for c, args in CMD.findall(d):
        assert c != 'A', d
        n = [float(v) for v in NUM.findall(args)]
        pts = []
        for x, y in zip(n[0::2], n[1::2]):
            x, y = x - cx, y - cy
            pts += [fmt(cx + x * math.cos(a) - y * math.sin(a)), fmt(cy + x * math.sin(a) + y * math.cos(a))]
        out.append(c + ' '.join(pts))
    return ''.join(out)

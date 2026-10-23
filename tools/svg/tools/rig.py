"""A bone rig for stick figures: joints in the figure's static coordinates, poses as rotations, smooth limbs.

A Rig is a tree of joints. A pose rotates bones (each joint's bone to its parent, carrying its subtree), moves the root,
and pins two-bone chains to targets (IK). Chains of joints are drawn as one cubic curve each, with a softness per
interior joint (0 is a corner). Keyframed poses become `animate d` values with the same commands in every frame.
"""
import math
from anim import EASE, fmt as fmt3


def fmt(x):
    """A compact number at two decimals, enough for a 24 unit icon."""
    s = f'{x:.2f}'.rstrip('0').rstrip('.')
    return '0' if s in ('-0', '') else s


def rotv(x, y, deg):
    a = math.radians(deg)
    return x * math.cos(a) - y * math.sin(a), x * math.sin(a) + y * math.cos(a)


class Rig:
    """Joints as (name, parent, (x, y)), parents first; the first is the root."""

    def __init__(self, joints):
        self.order = [j[0] for j in joints]
        self.parent = {j[0]: j[1] for j in joints}
        self.rest = {j[0]: j[2] for j in joints}

    def pose(self, rot=None, move=(0, 0), ik=None):
        """Joint positions for bone rotations [rot] (degrees, clockwise), root shift [move], and IK pins [ik]:
        {end: (target, bend)} where end's grandparent stays and its parent is solved; bend is +1 or -1."""
        rot = rot or {}
        pos, ang = {}, {}
        for j in self.order:
            p = self.parent[j]
            if p is None:
                x, y = self.rest[j]
                pos[j] = (x + move[0], y + move[1])
                ang[j] = rot.get(j, 0)
                continue
            ang[j] = ang[p] + rot.get(j, 0)
            vx, vy = self.rest[j][0] - self.rest[p][0], self.rest[j][1] - self.rest[p][1]
            dx, dy = rotv(vx, vy, ang[j])
            pos[j] = (pos[p][0] + dx, pos[p][1] + dy)
        for end, (target, bend) in (ik or {}).items():
            mid = self.parent[end]
            root = self.parent[mid]
            pos[mid] = self.solve(pos[root], target, self.length(root, mid), self.length(mid, end), bend)
            pos[end] = target
        pos['_ang'] = ang
        return pos

    def length(self, a, b):
        (ax, ay), (bx, by) = self.rest[a], self.rest[b]
        return math.hypot(bx - ax, by - ay)

    @staticmethod
    def solve(a, c, l1, l2, bend):
        """The middle joint of a two-bone chain from [a] to [c]; [bend] picks the side."""
        dx, dy = c[0] - a[0], c[1] - a[1]
        d = min(math.hypot(dx, dy), l1 + l2 - 1e-6)
        along = (l1 * l1 - l2 * l2 + d * d) / (2 * d)
        h = math.sqrt(max(l1 * l1 - along * along, 0))
        ux, uy = dx / math.hypot(dx, dy), dy / math.hypot(dx, dy)
        return a[0] + ux * along - bend * uy * h, a[1] + uy * along + bend * ux * h


def curve(pts, soft):
    """One cubic path through [pts]; [soft] gives each interior point's roundness (a number for all, or a list)."""
    n = len(pts)
    if not isinstance(soft, (list, tuple)):
        soft = [soft] * (n - 2)
    hin, hout = [None] * n, [None] * n
    for i in range(1, n - 1):
        (px, py), (x, y), (nx, ny) = pts[i - 1], pts[i], pts[i + 1]
        tx, ty = nx - px, ny - py
        t = math.hypot(tx, ty) or 1
        tx, ty = tx / t, ty / t
        a, b = math.hypot(x - px, y - py) * soft[i - 1], math.hypot(nx - x, ny - y) * soft[i - 1]
        hin[i], hout[i] = (x - tx * a, y - ty * a), (x + tx * b, y + ty * b)
    d = f'M{fmt(pts[0][0])} {fmt(pts[0][1])}'
    for i in range(n - 1):
        (x0, y0), (x1, y1) = pts[i], pts[i + 1]
        c1 = hout[i] or (x0 + (x1 - x0) / 3, y0 + (y1 - y0) / 3)
        c2 = hin[i + 1] or (x1 - (x1 - x0) / 3, y1 - (y1 - y0) / 3)
        d += f'C{fmt(c1[0])} {fmt(c1[1])} {fmt(c2[0])} {fmt(c2[1])} {fmt(x1)} {fmt(y1)}'
    return d


def morph(frames, dur, times=None, linear=False):
    """An `animate d` through path [frames] (the first repeated at the end unless already there)."""
    vals = list(frames)
    if vals[-1] != vals[0]:
        vals.append(vals[0])
    n = len(vals)
    keys = times or ';'.join(fmt3(i / (n - 1)) for i in range(n))
    ease = '' if linear else f' calcMode="spline" keySplines="{";".join([EASE] * (n - 1))}"'
    return f'<animate attributeName="d" values="{";".join(vals)}" keyTimes="{keys}" dur="{fmt(dur)}s" repeatCount="indefinite"{ease}/>'


def slide(offsets, dur, times=None, linear=False):
    """An animateTransform translate through [offsets] ((dx, dy) pairs), looping."""
    vals = [f'{fmt(x)} {fmt(y)}' for x, y in offsets]
    if vals[-1] != vals[0]:
        vals.append(vals[0])
    n = len(vals)
    keys = times or ';'.join(fmt3(i / (n - 1)) for i in range(n))
    ease = '' if linear else f' calcMode="spline" keySplines="{";".join([EASE] * (n - 1))}"'
    return (f'<animateTransform attributeName="transform" type="translate" values="{";".join(vals)}" keyTimes="{keys}" '
            f'dur="{fmt(dur)}s" repeatCount="indefinite"{ease}/>')

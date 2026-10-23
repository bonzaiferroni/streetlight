"""Rigged stick figures: bones and keyframed poses (tools/rig.py), flame head, refined lines (notes.md, Stick Figures).

Usage: python3 rigged.py [labels...]   (default: all). Supersedes stick-figures.py for these labels.
Pose 0 is the static icon. Poses ease in and out between keys, so extremes come to rest the way a swing does.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import fmt, flame_head
from stick import Fig, path, group
from rig import Rig, curve, morph, slide, rotv

FIGURES = {}


def figure(fn):
    FIGURES[fn.__name__.replace('_', '-')] = fn
    return fn


class Rigged:
    """A [rig] drawn as [chains] ((joint names, softness) pairs) with the flame [head] ((cx, cy, r)) riding [anchor]."""

    def __init__(self, fig, rig, chains, head, anchor):
        self.f, self.rig, self.chains, self.head, self.anchor = fig, rig, chains, head, anchor

    def draw(self, pos):
        f = self.f
        return [curve([(f.x(pos[j][0]), f.y(pos[j][1])) for j in names], soft) for names, soft in self.chains]

    def head_at(self, pos):
        """The head's offset from rest for pose [pos], in placed units."""
        ax, ay = self.rig.rest[self.anchor]
        ox, oy = self.head[0] - ax, self.head[1] - ay
        rx, ry = rotv(ox, oy, pos['_ang'][self.anchor])
        px, py = pos[self.anchor]
        return ((px + rx - self.head[0]) * self.f.s, (py + ry - self.head[1]) * self.f.s)

    def build(self, poses, dur, times=None, bob=None, props=(), props_anim=(), before=(), linear=False):
        """Writes the pack. [poses] are rig.pose results; [bob] (dy, figure units) lifts the figure twice per loop;
        [props] are placed static paths; [props_anim] replace them in the animated file; [before] go under the
        figure in the animated file."""
        f = self.f
        frames = [self.draw(p) for p in poses]
        rest = frames[0]
        head_d = f.p(flame_head(*self.head))
        f.headc = self.head
        f.write(list(props) + rest, [])
        parts = [path(d, morph([fr[i] for fr in frames], dur, times, linear)) for i, d in enumerate(rest)]
        offsets = [self.head_at(p) for p in poses]
        if any(abs(x) > 1e-3 or abs(y) > 1e-3 for x, y in offsets):
            parts.append(group([path(head_d)], slide(offsets, dur, times, linear)))
        else:
            parts.append(path(head_d))
        if bob:
            parts = [group(parts, slide([(0, 0), (0, bob * f.s)], dur / 2))]
        body = list(before) + (list(props_anim) if props_anim else [path(d) for d in props]) + parts
        from stick import emit, FRAME
        from anim import SVG_DIR
        emit(f'{SVG_DIR}/{f.label}-animated.svg', f.open_tag(1, 'animated'), [FRAME] + body)
        return rest + list(props)


@figure
def walk():
    f = Fig('walk', head=(13.2, 4.4, 2.2), s=0.92, dy=0.6)
    rig = Rig([
        ('hip', None, (11, 14)),
        ('kneeF', 'hip', (14, 17)), ('footF', 'kneeF', (16, 21)),
        ('kneeB', 'hip', (10, 17)), ('footB', 'kneeB', (7, 21)),
        ('mid', 'hip', (10.75, 11)), ('neck', 'mid', (12, 8)),
        ('elbowL', 'neck', (8.3, 9)), ('handL', 'elbowL', (6, 12)),
        ('elbowR', 'neck', (15, 11)), ('handR', 'elbowR', (18, 12)),
    ])
    chains = [(['footF', 'kneeF', 'hip', 'mid', 'neck'], [0.3, 0.45, 0.45]),
              (['kneeB', 'footB'], 0),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], [0.3, 0.3, 0.3])]
    r = Rigged(f, rig, chains, (13.2, 4.4, 2.2), 'neck')
    a = rig.pose()
    b = rig.pose({'kneeF': 12, 'footF': 6, 'kneeB': -13, 'footB': -4, 'elbowL': -14, 'handL': -8, 'elbowR': 12, 'handR': 6})
    return f, r.build([a, b], 1.2, bob=-0.35)


@figure
def run():
    f = Fig('run', head=(13.1, 4.8, 2.2), s=0.92, dy=0.4)
    rig = Rig([
        ('hip', None, (11, 14)),
        ('kneeF', 'hip', (14.8, 17)), ('footF', 'kneeF', (15, 21)),
        ('thighB', 'hip', (9.75, 16.5)), ('kneeB', 'thighB', (9, 18)), ('footB', 'kneeB', (4, 17)),
        ('mid', 'hip', (10.75, 11)), ('neck', 'mid', (12, 8)),
        ('elbowL', 'neck', (7.2, 9)), ('handL', 'elbowL', (7, 12)),
        ('elbowR', 'neck', (15, 11)), ('handR', 'elbowR', (18, 12)),
    ])
    chains = [(['footF', 'kneeF', 'hip', 'mid', 'neck'], [0.3, 0.45, 0.45]),
              (['thighB', 'kneeB', 'footB'], 0.3),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], [0.3, 0.3, 0.3])]
    r = Rigged(f, rig, chains, (13.1, 4.8, 2.2), 'neck')
    a = rig.pose()
    b = rig.pose({'kneeF': 11, 'footF': 10, 'thighB': -11, 'footB': -12, 'elbowL': -13, 'handL': -5, 'elbowR': 12, 'handR': 8})
    return f, r.build([a, b], 1, bob=-0.5)


@figure
def bike():
    f = Fig('bike', head=(15.1, 4.8, 2.2), s=0.92, dy=0.4)
    rig = Rig([
        ('hip', None, (9.4, 12.2)),
        ('knee', 'hip', (11.7, 15.3)), ('foot', 'knee', (11.9, 19)),
        ('mid', 'hip', (11.2, 9.6)), ('neck', 'mid', (14, 8)),
        ('elbow', 'neck', (16, 10.6)), ('hand', 'elbow', (19, 11)),
    ])
    chains = [(['foot', 'knee', 'hip', 'mid', 'neck', 'elbow', 'hand'], [0.3, 0.1, 0.4, 0.3, 0.3])]
    r = Rigged(f, rig, chains, (15.1, 4.8, 2.2), 'neck')
    crank, radius = (11.4, 17.6), 1.4
    a0 = math.degrees(math.atan2(19 - crank[1], 11.9 - crank[0]))
    poses = []
    for k in range(8):
        lift = -0.25 * math.sin(math.radians(k * 90)) ** 2
        def pedal(deg):
            t = math.radians(a0 + deg)
            return (crank[0] + radius * math.cos(t), crank[1] + radius * math.sin(t))
        poses.append(rig.pose({'mid': lift * 4}, (0, lift),
                              ik={'foot': (pedal(k * 45), -1), 'hand': ((19, 11), 1)}))
    wheels = ['M2 18A3 3 0 1 0 8 18A3 3 0 1 0 2 18', 'M16 18A3 3 0 1 0 22 18A3 3 0 1 0 16 18']
    props = [f.p(d) for d in wheels]
    hubs = []
    for cx in (5, 19):
        c = f.pt(cx, 18)
        hubs.append(group([path(f.p(f'M{fmt(cx - 1.8)} 18L{fmt(cx + 1.8)} 18'))],
                          f'<animateTransform attributeName="transform" type="rotate" from="0 {c}" to="360 {c}" dur="1.5s" repeatCount="indefinite"/>'))
    return f, r.build(poses, 1.5, props=props, props_anim=[path(d) for d in props] + hubs, linear=True)


def ski(foot, deg, side):
    """A ski through [foot], turned [deg] about it; [side] is 1 for the right ski."""
    if side > 0:
        pts, tip = [(15.103, 21.58), (21.865, 7.078)], (20.897, 4.421)
    else:
        pts, tip = [(8.897, 21.58), (2.135, 7.077)], (3.103, 4.42)
    rest = (17, 17.5) if side > 0 else (7, 17.5)
    def place(p):
        x, y = rotv(p[0] - rest[0], p[1] - rest[1], deg)
        return foot[0] + x, foot[1] + y
    (x0, y0), (x1, y1), (x2, y2) = place(pts[0]), place(pts[1]), place(tip)
    sweep = 0 if side > 0 else 1
    return f'M{fmt(x0)} {fmt(y0)}L{fmt(x1)} {fmt(y1)}A2 2 0 0 {sweep} {fmt(x2)} {fmt(y2)}'


@figure
def ski_jumping():
    f = Fig('ski-jumping', head=(12, 3.8, 2.2), s=0.88, dy=1.1)
    rig = Rig([
        ('hip', None, (12, 13)),
        ('kneeL', 'hip', (9.6, 15.1)), ('footL', 'kneeL', (7, 17.5)),
        ('kneeR', 'hip', (14.4, 15.1)), ('footR', 'kneeR', (17, 17.5)),
        ('mid', 'hip', (12, 10)), ('neck', 'mid', (12, 7)),
        ('shL', 'neck', (10, 7.6)), ('handL', 'shL', (7, 11)),
        ('shR', 'neck', (14, 7.6)), ('handR', 'shR', (17, 11)),
    ])
    chains = [(['footL', 'kneeL', 'hip', 'kneeR', 'footR'], [0.15, 0.3, 0.15]),
              (['hip', 'mid', 'neck'], 0.3),
              (['handL', 'shL', 'neck', 'shR', 'handR'], [0.4, 0.45, 0.4])]
    r = Rigged(f, rig, chains, (12, 3.8, 2.2), 'neck')
    a = rig.pose()
    b = rig.pose({'kneeL': 5, 'kneeR': -5, 'handL': 12, 'handR': -12}, (0, -0.7))
    poses = [a, b]

    def skis(p, deg):
        return [f.p(ski(p['footR'], deg, 1)), f.p(ski(p['footL'], -deg, -1))]
    rest, open_ = skis(a, 0), skis(b, 3)
    from rig import morph as m
    props_anim = [path(rest[i], m([rest[i], open_[i]], 3)) for i in range(2)]
    return f, r.build(poses, 3, props=rest, props_anim=props_anim)


@figure
def old():
    f = Fig('old', head=(8.6, 4.8, 2.2), s=0.92, dy=0.5)
    cane_tip, grip = (14, 21), (14.3, 11.4)
    rig = Rig([
        ('hip', None, (8, 14)),
        ('kneeF', 'hip', (10, 17.2)), ('footF', 'kneeF', (11, 21)),
        ('kneeB', 'hip', (7, 17)), ('footB', 'kneeB', (5, 21)),
        ('mid', 'hip', (7.5, 11)), ('neck', 'mid', (8, 8)),
        ('elbowL', 'neck', (4.4, 10.8)), ('handL', 'elbowL', (5, 14)),
        ('elbowR', 'neck', (10.8, 10.2)), ('handR', 'elbowR', grip),
    ])
    chains = [(['footF', 'kneeF', 'hip', 'mid', 'neck'], [0.3, 0.45, 0.45]),
              (['kneeB', 'footB'], 0),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], [0.3, 0.3, 0.3])]
    r = Rigged(f, rig, chains, (8.6, 4.8, 2.2), 'neck')

    def cane(deg):
        top, crook, end = (14, 12.5), (17, 12.5), (17, 13)
        pts = []
        for p in (top, crook, end):
            x, y = rotv(p[0] - cane_tip[0], p[1] - cane_tip[1], deg)
            pts.append((cane_tip[0] + x, cane_tip[1] + y))
        (x0, y0), (x1, y1), (x2, y2) = pts
        return f'M14 21L{fmt(x0)} {fmt(y0)}A1.5 1.5 0 0 1 {fmt(x1)} {fmt(y1)}L{fmt(x2)} {fmt(y2)}'

    def grip_at(deg):
        x, y = rotv(grip[0] - cane_tip[0], grip[1] - cane_tip[1], deg)
        return (cane_tip[0] + x, cane_tip[1] + y)
    a = rig.pose(ik={'handR': (grip_at(0), 1)})
    b = rig.pose({'kneeF': 8, 'kneeB': -8, 'elbowL': -10}, (0, -0.3), ik={'handR': (grip_at(5), 1)})
    rest, moved = f.p(cane(0)), f.p(cane(5))
    props_anim = [path(rest, morph([rest, moved], 2.4))]
    return f, r.build([a, b], 2.4, props=[rest], props_anim=props_anim)


@figure
def pray():
    f = Fig('pray', head=(11.6, 5.4, 2.2), s=1, dy=0.6)
    rig = Rig([
        ('hip', None, (11, 15.4)),
        ('knee', 'hip', (15, 20)), ('foot', 'knee', (7, 20)),
        ('mid', 'hip', (10.75, 12.2)), ('neck', 'mid', (11, 9)),
        ('elbow', 'neck', (14.8, 11.8)), ('hand', 'elbow', (17, 10)),
    ])
    chains = [(['foot', 'knee', 'hip', 'mid', 'neck', 'elbow', 'hand'], [0.15, 0.4, 0.4, 0.3, 0.3])]
    r = Rigged(f, rig, chains, (11.6, 5.4, 2.2), 'neck')
    a = rig.pose()
    b = rig.pose({'mid': 4, 'neck': 7, 'elbow': -5, 'hand': -6})
    return f, r.build([a, b], 3)


@figure
def stretching():
    f = Fig('stretching', head=(16.2, 5.5, 2.2), s=0.95, dy=0.4)
    rig = Rig([
        ('hip', None, (12.6, 15)),
        ('kneeF', 'hip', (18, 15)), ('footF', 'kneeF', (18, 20)),
        ('thighB', 'hip', (11, 17.6)), ('kneeB', 'thighB', (10, 19.5)), ('footB', 'kneeB', (5, 20)),
        ('mid', 'hip', (13.7, 11.8)), ('neck', 'mid', (15, 8.5)),
        ('elbow', 'neck', (9.8, 9.4)), ('hand', 'elbow', (11, 11.5)),
    ])
    chains = [(['footF', 'kneeF', 'hip', 'mid', 'neck', 'elbow', 'hand'], [0.25, 0.4, 0.35, 0.25, 0.2]),
              (['thighB', 'kneeB', 'footB'], 0.25)]
    r = Rigged(f, rig, chains, (16.2, 5.5, 2.2), 'neck')
    a = rig.pose()
    b = rig.pose({'mid': -6, 'neck': -2, 'elbow': -4, 'hand': -16}, (-0.4, 0.5),
                 ik={'footF': ((18, 20), -1), 'footB': ((5, 20), -1)})
    return f, r.build([a, b], 3)


@figure
def swimming():
    f = Fig('swimming', head=(16.2, 10, 1.9), s=1, dy=0)
    rig = Rig([
        ('shoulder', None, (13.5, 12)),
        ('chest', 'shoulder', (12, 14)),
        ('elbow', 'shoulder', (10, 9)), ('hand', 'elbow', (6, 11)),
    ])
    chains = [(['hand', 'elbow', 'shoulder', 'chest'], [0.25, 0.3])]
    r = Rigged(f, rig, chains, (16.2, 10, 1.9), 'shoulder')
    a = rig.pose()
    b = rig.pose({'elbow': 22, 'hand': 18}, (0, 0.5))
    water = ('M3 16.75A2.4 2.4 0 0 0 4 17A2.4 2.4 0 0 0 6 16A2.4 2.4 0 0 1 8 15A2.4 2.4 0 0 1 10 16A2.4 2.4 0 0 0 12 17'
             'A2.4 2.4 0 0 0 14 16A2.4 2.4 0 0 1 16 15A2.4 2.4 0 0 1 18 16A2.4 2.4 0 0 0 20 17A2.4 2.4 0 0 0 21 16.75')
    w = f.p(water)
    props_anim = [group([path(w)], slide([(0, 0), (0, -0.3)], 2))]
    return f, r.build([a, b], 2, props=[w], props_anim=props_anim)


@figure
def massage():
    f = Fig('massage', head=(9, 4.4, 2), s=0.92, dy=0.6)
    rig = Rig([
        ('hip', None, (15, 14)),
        ('mid', 'hip', (14.4, 9.5)), ('neck', 'mid', (12, 8)),
        ('elbow', 'neck', (11, 11.8)), ('hand', 'elbow', (8, 14)),
    ])
    chains = [(['hip', 'mid', 'neck', 'elbow', 'hand'], [0.5, 0.45, 0.3])]
    r = Rigged(f, rig, chains, (9, 4.4, 2), 'neck')
    a = rig.pose()
    b = rig.pose({'mid': -5, 'neck': -3}, ik={'hand': ((8, 14.6), -1)})
    client = f.p(flame_head(4.4, 15.4, 1.8))
    props = [client, f.p('M4 22L8 20L8 17L20 17'), f.p('M11 20L20 20')]
    out = r.build([a, b], 1.6, props=props)
    from anim import SVG_DIR
    p = f'{SVG_DIR}/massage-small.svg'
    s = open(p).read()
    s = s.replace(f'<path d="{client}"/>', f'<path stroke-width="{fmt(f.small_head)}" d="{client}"/>')
    open(p, 'w').write(s)
    return f, out


if __name__ == '__main__':
    for label in sys.argv[1:] or FIGURES:
        FIGURES[label]()
        print(label)

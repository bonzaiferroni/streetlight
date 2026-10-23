import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


def calendar():
    # the date square turns 90 degrees per second
    head, els = load('calendar')
    square = els[5]
    body = els[:5] + [group([square], spin(9, 16, 4))]
    save('calendar', head, body)


def clock():
    # minute hand turns every 2s, hour hand every 24s
    head, els = load('clock')
    minute = '<path d="M12 12v-5"/>'
    hour = '<path d="M12 12l3 3"/>'
    body = els[:2] + [group([minute], spin(12, 12, 2)), group([hour], spin(12, 12, 24))]
    save('clock', head, body)


def adjustments_horizontal():
    # each knob slides along its rail, the rail segments follow
    head, els = load('adjustments-horizontal')
    # (knob center x, y, left rail start, right rail end, offsets, dur)
    rows = [(14, 6, 4, 20, [1.5, -1.5], 4), (8, 12, 4, 20, [1.5, -1], 6), (17, 18, 4, 20, [-2, 0.5], 4)]
    body = [els[0]]
    for cx, y, x0, x1, offs, dur in rows:
        xs = [0] + offs
        knob = f'<path d="M{fmt(cx - 2)} {y}a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"/>'
        left = [f'M{x0} {y}l{fmt(cx - 2 + o - x0)} 0' for o in xs]
        right = [f'M{fmt(cx + 2 + o)} {y}l{fmt(x1 - (cx + 2 + o))} 0' for o in xs]
        body.append(group([knob], osc('transform', [f'{fmt(o)} 0' for o in xs], dur, kind='translate')))
        body.append(f'<path d="{left[0]}">\n    {osc("d", left, dur)}\n</path>')
        body.append(f'<path d="{right[0]}">\n    {osc("d", right, dur)}\n</path>')
    save('adjustments-horizontal', head, body)


def ambulance():
    # the body bobs, hubs turn in the wheels
    head, els = load('ambulance')
    wheels = els[1:3]
    hubs = [group([f'<path d="M{cx - 0.8} 17h1.6"/>'], spin(cx, 17, 1.5)) for cx in (7, 17)]
    body = [els[0]] + wheels + hubs + [shift(els[3:5], 0, -0.6, 1.2)]
    save('ambulance', head, body)


def aperture():
    # the blades turn on their rim points, closing the opening, then open again
    head, els = load('aperture')
    blades = []
    for e in els[2:]:
        d = re.search(r'd="([^"]+)"', e).group(1)
        nums = [float(x) for x in re.findall(r'-?\d*\.?\d+', d)]
        if 'h' in d:
            p, q = (nums[0], nums[1]), (nums[0] + nums[2], nums[1])
        else:
            p, q = (nums[0], nums[1]), (nums[0] + nums[2], nums[1] + nums[3])
        blades.append((p, q))

    def line_hit(p, d, a, e):
        # intersection of p + t d with a + s e
        den = d[0] * e[1] - d[1] * e[0]
        t = ((a[0] - p[0]) * e[1] - (a[1] - p[1]) * e[0]) / den
        return p[0] + t * d[0], p[1] + t * d[1]

    def on_line(pt, a, b):
        return abs((b[0] - a[0]) * (pt[1] - a[1]) - (b[1] - a[1]) * (pt[0] - a[0])) / math.dist(a, b) < 0.1

    # each blade ends on another blade
    ends = []
    for i, (p, q) in enumerate(blades):
        j = next(j for j, (a, b) in enumerate(blades) if j != i and on_line(q, a, b))
        ends.append(j)

    def rot(v, deg):
        r = math.radians(deg)
        return v[0] * math.cos(r) - v[1] * math.sin(r), v[0] * math.sin(r) + v[1] * math.cos(r)

    # the direction that closes: towards the center
    def frame(deg):
        dirs = [rot((q[0] - p[0], q[1] - p[1]), deg) for p, q in blades]
        ds = []
        for i, (p, q) in enumerate(blades):
            j = ends[i]
            end = line_hit(p, dirs[i], blades[j][0], dirs[j])
            ds.append(f'M{fmt(p[0])} {fmt(p[1])}L{fmt(end[0])} {fmt(end[1])}')
        return ds

    # choose sign that moves the first blade towards the center
    p, q = blades[0]
    sign = 1
    d1 = rot((q[0] - p[0], q[1] - p[1]), 5)
    dist = lambda d: abs(d[0] * (12 - p[1]) - d[1] * (12 - p[0])) / math.hypot(*d)
    if dist(d1) > dist((q[0] - p[0], q[1] - p[1])):
        sign = -1
    steps = [sign * 14 * (1 - math.cos(math.pi * k / 8)) / 2 for k in range(9)]  # eased 0..14 deg
    frames = [frame(a) for a in steps]
    seq = frames + frames[-2::-1]  # close then open
    body = els[:2]
    for i in range(len(blades)):
        vals = [f[i] for f in seq]
        body.append(f'<path d="{vals[0]}">\n    {osc("d", vals, 4, ease=False)}\n</path>')
    save('aperture', head, body)


def maximize():
    # the corners push out and back
    head, els = load('maximize')
    dirs = [(-1, -1), (-1, 1), (1, -1), (1, 1)]
    body = [els[0]] + [shift([e], 0.8 * dx, 0.8 * dy, 2) for e, (dx, dy) in zip(els[1:], dirs)]
    save('maximize', head, body)


def atom():
    # electrons travel the two orbits
    head, els = load('atom')
    orbits = [re.search(r'd="([^"]+)"', e).group(1) for e in els[2:4]]
    electrons = []
    # opposite directions, the second starts a third of the way round
    for d, rev, begin in zip(orbits, (False, True), ('0s', '-1s')):
        kp = ' keyPoints="1;0" keyTimes="0;1" calcMode="linear"' if rev else ''
        electrons.append(f'<circle r="0.9" fill="currentColor" stroke="none">\n    '
                         f'<animateMotion path="{d}" dur="3s" begin="{begin}" repeatCount="indefinite"{kp}/>\n</circle>')
    save('atom', head, els + electrons)


def navigation():
    # grows and shrinks
    head, els = load('navigation')
    save('navigation', head, [els[0], pulse(els[1:], amount=0.08, dur=3)])


for f in [calendar, clock, adjustments_horizontal, ambulance, aperture, maximize, atom, navigation]:
    f()
    print('wrote', f.__name__)

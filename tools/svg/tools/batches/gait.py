"""Full-cycle figures: legs and arms alternate through the whole stride (notes.md, Gait Figures).

Usage: python3 gait.py [labels...]   (default: all). Supersedes rigged.py for walk, run, old and swimming.
A loop is one cycle sampled into N keyframes and played linearly; frame 0 is the static icon. The near limbs join the
body; the far limbs start a little way out from the hip or shoulder, a gap that reads as depth.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, flame_head
from stick import Fig, path, group, emit, FRAME
from rig import Rig, curve, morph, slide, fmt

FIGURES = {}
TAU = 2 * math.pi


def figure(fn):
    FIGURES[fn.__name__] = fn
    return fn


def polar(o, deg, length):
    a = math.radians(deg)
    return o[0] + length * math.cos(a), o[1] + length * math.sin(a)


def lerp(a, b, t):
    return a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t


def foot_path(phase, center, ground, stride, lift, stance, early=1.0):
    """A foot's place at [phase] (0 to 1, 0 the moment it lands in front): planted and sliding back through
    [stance], then lifted by up to [lift] and carried forward. [early] below 1 lifts sooner in the swing (a kick)."""
    phase %= 1
    if phase < stance:
        u = phase / stance
        return center + stride * (1 - 2 * u), ground
    u = (phase - stance) / (1 - stance)
    ease = (1 - math.cos(math.pi * u)) / 2
    return center - stride + 2 * stride * ease, ground - lift * math.sin(math.pi * u ** early)


def leg(hip, foot, thigh, shin):
    """Knee and foot for a leg from [hip] to [foot], the knee forward (to the right)."""
    knee = Rig.solve(hip, foot, thigh, shin, -1)
    d = math.hypot(foot[0] - hip[0], foot[1] - hip[1])
    if d > thigh + shin:
        foot = lerp(hip, foot, (thigh + shin) / d)
    return knee, foot


def arm(shoulder, swing, bend, upper, fore):
    """Elbow and hand for an arm hanging from [shoulder], swung forward by [swing] degrees, the elbow bent [bend]."""
    elbow = polar(shoulder, 90 - swing, upper)
    return elbow, polar(elbow, 90 - swing - bend, fore)


class Cycle:
    """Collects each frame's chains (as point lists with softness) and head offset, then writes the pack."""

    def __init__(self, fig, head):
        self.f, self.head, self.frames, self.offsets = fig, head, [], []

    def frame(self, chains, head_at):
        """Adds a frame: [chains] as (points, softness) and [head_at], a point the head keeps its distance from."""
        f = self.f
        self.frames.append([curve([(f.x(x), f.y(y)) for x, y in pts], soft) for pts, soft in chains])
        self.offsets.append(head_at)

    def write(self, dur, props=(), props_anim=None, clip=None, behind=False, linear=True):
        """Writes the pack. [clip] is (id, path): the limbs show only inside it, statics included. [behind] hides the
        limbs where they pass behind the head."""
        f = self.f
        f.headc = self.head
        rest = self.frames[0]
        static = [path(d) for d in rest]
        if behind:
            static = [behind_group(static, f'{f.label}-head', f.p(flame_head(*self.head)), None)]
        if clip:
            static = [clip_group(static, clip)]
        f.write(list(props) + static, [])
        head_d = f.p(flame_head(*self.head))
        (x0, y0) = self.offsets[0]
        offsets = [((x - x0) * f.s, (y - y0) * f.s) for x, y in self.offsets]
        parts = [path(d, morph([fr[i] for fr in self.frames], dur, linear=linear)) for i, d in enumerate(rest)]
        if behind:
            parts = [behind_group(parts, f'{f.label}-head', head_d, slide(offsets, dur, linear=linear))]
        if clip:
            parts = [clip_group(parts, clip)]
        parts.append(group([path(head_d)], slide(offsets, dur, linear=linear)))
        body = list(props_anim if props_anim is not None else [path(d) for d in props]) + parts
        emit(f'{SVG_DIR}/{f.label}-animated.svg', f.open_tag(1, 'animated'), [FRAME] + body)


def behind_group(parts, mask_id, head_d, move):
    """[parts] masked out inside the head [head_d] (moved by [move]), as if passing behind it."""
    inner = '\n'.join('    ' + l for p in parts for l in p.split('\n'))
    return (f'<mask id="{mask_id}" maskUnits="userSpaceOnUse" x="0" y="0" width="24" height="24">\n'
            f'    <rect width="24" height="24" fill="#fff"/>\n'
            + (f'    <path d="{head_d}" fill="#000" stroke="#000" stroke-width="4">\n        {move}\n    </path>\n</mask>\n'
               if move else f'    <path d="{head_d}" fill="#000" stroke="#000" stroke-width="4"/>\n</mask>\n')
            + f'<g mask="url(#{mask_id})">\n{inner}\n</g>')


def clip_group(parts, clip):
    inner = '\n'.join('    ' + l for p in parts for l in p.split('\n'))
    return (f'<clipPath id="{clip[0]}">\n    <path d="{clip[1]}"/>\n</clipPath>\n'
            f'<g clip-path="url(#{clip[0]})">\n{inner}\n</g>')


def bob_at(phase, amount, high):
    """A lift of [amount] peaking twice per cycle, at [high] and half a cycle on."""
    return -amount * (0.5 + 0.5 * math.cos(2 * TAU * (phase - high)))


def stand(phase, hip_x, center, stride, stance, length, flex):
    """The hips' height on whichever foot is planted at [phase] (the lower, with both down): the stance leg near
    straight, flexed by up to [flex] of its [length] at mid-stance, so the hips vault over it."""
    h = None
    for off in (0, 0.5):
        p = (phase + off) % 1
        if p < stance:
            u = p / stance
            dx = center + stride * (1 - 2 * u) - hip_x
            l = length * (1 - flex * math.sin(math.pi * u))
            h = math.sqrt(l * l - dx * dx) if h is None else min(h, math.sqrt(l * l - dx * dx))
    return h


@figure
def walk():
    f = Fig('walk', head=(13.2, 4.4, 2.2), s=0.9, dy=0.8)
    head = (13.2, 4.4, 2.2)
    n, dur = 12, 1.2
    hip0, ground, thigh, shin = (11.2, 14), 21, 4, 4.3
    stride, lift, stance, center = 4.2, 1.4, 0.5, 11.2
    cyc = Cycle(f, head)
    for k in range(n):
        ph = k / n
        y = ground - stand(ph, hip0[0], center, stride, stance, thigh + shin, 0.025) - hip0[1]
        hip = (hip0[0], hip0[1] + y)
        mid, neck = (11.2, 11 + y), (12, 8 + y)
        kneeN, footN = leg(hip, foot_path(ph, center, ground, stride, lift, stance), thigh, shin)
        kneeF, footF = leg(hip, foot_path(ph + 0.5, center, ground, stride, lift, stance), thigh, shin)
        swing = 58 * math.cos(TAU * (ph - stance / 2 + 0.25))
        elbowN, handN = arm(neck, -14 - swing, 30 + 12 * swing / 58, 3.8, 3.4)
        elbowF, handF = arm(neck, -14 + swing, 30 - 12 * swing / 58, 3.8, 3.4)
        cyc.frame([([footN, kneeN, hip, mid, neck], [0.3, 0.4, 0.4]),
                   ([lerp(hip, kneeF, 0.45), kneeF, footF], 0.3),
                   ([neck, elbowN, handN], 0.3),
                   ([lerp(neck, elbowF, 0.45), elbowF, handF], 0.3)],
                  (head[0], head[1] + y))
    cyc.write(dur)


def pump(shoulder, reach, lean):
    """A runner's arm at [reach] (-1 back, 1 forward), swung from the trunk leaning [lean] degrees: about 55° behind
    it and only 30° ahead, the elbow near 90°, tighter forward (hand to the chest) and opening behind."""
    rel = 30 * reach if reach > 0 else 55 * reach
    return arm(shoulder, rel - lean, 87 + 13 * reach, 3.4, 3)


def stride_path(phase, hip_x, ground, land, push, reach, lift, stance):
    """A runner's foot at [phase] (0 the landing, under the hips at hip + [land]): planted back to hip - [push], then
    heel kicked up to [lift], knee driven so the foot reaches hip + [reach], and pawed back to land."""
    phase %= 1
    if phase < stance:
        u = phase / stance
        return hip_x + land - (land + push) * u, ground
    u = (phase - stance) / (1 - stance)
    y = ground - lift * math.sin(math.pi * u ** 0.55)
    if u < 0.8:
        e = (1 - math.cos(math.pi * u / 0.8)) / 2
        return hip_x - push + (push + reach) * e, y
    e = (1 - math.cos(math.pi * (u - 0.8) / 0.2)) / 2
    return hip_x + reach - (reach - land) * e, y


# Running gait, angles in degrees through one stride (0 is the foot landing, toe-off at 0.35). Hip is the thigh's
# flexion forward of the trunk, knee the flexion behind the thigh. Shaped on running gait curves: hip about 75° of arc,
# peaking in late swing; knee near 120° in swing for an energetic stride, about 40° in stance.
RUN_HIP = [(0, 40), (0.15, 20), (0.35, -18), (0.5, 8), (0.65, 42), (0.8, 58), (0.92, 50)]
RUN_KNEE = [(0, 22), (0.15, 42), (0.35, 22), (0.5, 92), (0.63, 122), (0.8, 72), (0.93, 22)]
RUN_STANCE = 0.35


def curve_at(table, phase):
    """A periodic Catmull-Rom through [table] ((phase, value) pairs) at [phase]."""
    phase %= 1
    n = len(table)
    i = max(k for k in range(n) if table[k][0] <= phase)
    t0, v1 = table[i]
    t1, v2 = table[(i + 1) % n]
    t1 = t1 if i + 1 < n else t1 + 1
    v0, v3 = table[i - 1][1], table[(i + 2) % n][1]
    u = (phase - t0) / (t1 - t0)
    return 0.5 * (2 * v1 + (v2 - v0) * u + (2 * v0 - 5 * v1 + 4 * v2 - v3) * u * u + (3 * v1 - v0 - 3 * v2 + v3) * u ** 3)


def run_leg(phase, tilt, thigh, shin):
    """Knee and foot offsets from the hip at [phase]; [tilt] turns the hip angle into the world (forward lean)."""
    a = math.radians(curve_at(RUN_HIP, phase) - tilt)
    b = a - math.radians(curve_at(RUN_KNEE, phase))
    knee = (thigh * math.sin(a), thigh * math.cos(a))
    return knee, (knee[0] + shin * math.sin(b), knee[1] + shin * math.cos(b))


@figure
def run(lean=20, n=16, dur=0.8, start=0.3):
    """A runner driven by hip and knee angle curves, the whole body tilted [lean] degrees as one line so the weight
    falls forward. The hips ride on whichever foot is planted and fly between steps. Frame 0 is the near foot's
    push-off, at [start]."""
    hip_x, ground, thigh, shin, flight = 11, 21.2, 4.1, 4.3, 0.5
    tilt = lean * 0.6

    def legs(ph):
        return run_leg(ph, tilt, thigh, shin), run_leg(ph + 0.5, tilt, thigh, shin)

    def height(ph):
        """The hips' height above the ground at [ph]: standing on the planted foot, or in flight between."""
        ph %= 1
        for off in (0, 0.5):
            p = (ph - off) % 1
            if p < RUN_STANCE:
                return run_leg(p, tilt, thigh, shin)[1][1]
        off = 0 if ph < 0.5 else 0.5
        u = (ph - off - RUN_STANCE) / (0.5 - RUN_STANCE)
        h0 = run_leg(RUN_STANCE, tilt, thigh, shin)[1][1]
        h1 = run_leg(0, tilt, thigh, shin)[1][1]
        return h0 + (h1 - h0) * u + flight * 4 * u * (1 - u)

    def body(hip):
        mid = polar(hip, -90 + lean, 3)
        return mid, polar(mid, -90 + lean + 3, 3.1)
    neck0 = body((hip_x, ground - height(start)))[1]
    hx, hy = polar(neck0, -90 + lean + 3, 3.3)
    head = (hx, hy, 2.2)
    f = Fig('run', head=head, s=0.86, dx=-0.6, dy=0.4)
    cyc = Cycle(f, head)
    for k in range(n):
        ph = start + k / n
        hip = (hip_x, ground - height(ph))
        mid, neck = body(hip)
        (kn, fn), (kf, ff) = legs(ph)
        kneeN, footN = (hip[0] + kn[0], hip[1] + kn[1]), (hip[0] + fn[0], hip[1] + fn[1])
        kneeF, footF = (hip[0] + kf[0], hip[1] + kf[1]), (hip[0] + ff[0], hip[1] + ff[1])
        reach = -math.cos(TAU * (ph - 0.8))  # the near arm's: back as the near knee drives, forward at its push-off
        elbowN, handN = pump(neck, reach, lean)
        elbowF, handF = pump(neck, -reach, lean)
        cyc.frame([([footN, kneeN, hip, mid, neck], [0.25, 0.3, 0.3]),
                   ([lerp(hip, kneeF, 0.45), kneeF, footF], 0.25),
                   ([neck, elbowN, handN], 0.2),
                   ([lerp(neck, elbowF, 0.45), elbowF, handF], 0.2)],
                  neck)
    cyc.write(dur)
    return f


@figure
def old():
    f = Fig('old', head=(8.7, 4.8, 2.2), s=0.92, dy=0.5)
    head = (8.7, 4.8, 2.2)
    n, dur = 12, 2.4
    hip0, ground, thigh, shin = (8, 14), 21, 3.7, 4
    stride, lift, stance, center = 2.6, 0.8, 0.65, 8.3
    cane_len, cane_center = 9.6, 14
    cyc = Cycle(f, head)
    canes = []
    for k in range(n):
        ph = k / n
        y = bob_at(ph, 0.15, stance / 2)
        hip = (hip0[0], hip0[1] + y)
        mid, neck = (7.5, 11 + y), (8, 8 + y)
        kneeN, footN = leg(hip, foot_path(ph, center, ground, stride, lift, stance), thigh, shin)
        kneeF, footF = leg(hip, foot_path(ph + 0.5, center, ground, stride, lift, stance), thigh, shin)
        tip = foot_path(ph + 0.5, cane_center, ground, 1.4, 0.7, stance)
        tilt = -4 * (tip[0] - cane_center) / 1.4
        grip = polar(tip, -90 + tilt, cane_len)
        grip = (grip[0] + 0.3, grip[1])
        elbowN = Rig.solve(neck, grip, 3.6, 3.6, 1)
        swing = 14 * math.cos(TAU * (ph - stance / 2 + 0.25))
        elbowF, handF = arm(neck, -52 + swing, 63, 3.8, 3.3)
        canes.append(cane(f, tip, tilt))
        cyc.frame([([footN, kneeN, hip, mid, neck], [0.3, 0.45, 0.45]),
                   ([lerp(hip, kneeF, 0.45), kneeF, footF], 0.3),
                   ([neck, elbowN, grip], 0.3),
                   ([lerp(neck, elbowF, 0.45), elbowF, handF], 0.3)],
                  (head[0], head[1] + y))
    cyc.write(dur, props=[canes[0]], props_anim=[path(canes[0], morph(canes, dur, linear=True))])


def cane(f, tip, tilt):
    """The cane standing on [tip], tilted [tilt] degrees: a shaft and a crook opening to the right."""
    def at(dx, dy):
        a = math.radians(tilt)
        return tip[0] + dx * math.cos(a) - dy * math.sin(a), tip[1] + dx * math.sin(a) + dy * math.cos(a)
    top, crook, end = at(0, -8.5), at(3, -8.5), at(3, -8)
    return (f'M{fmt(f.x(tip[0]))} {fmt(f.y(tip[1]))}L{fmt(f.x(top[0]))} {fmt(f.y(top[1]))}'
            f'A{fmt(1.5 * f.s)} {fmt(1.5 * f.s)} 0 0 1 {fmt(f.x(crook[0]))} {fmt(f.y(crook[1]))}'
            f'L{fmt(f.x(end[0]))} {fmt(f.y(end[1]))}')


WATER = ('M3 16.75A2.4 2.4 0 0 0 4 17A2.4 2.4 0 0 0 6 16A2.4 2.4 0 0 1 8 15A2.4 2.4 0 0 1 10 16A2.4 2.4 0 0 0 12 17'
         'A2.4 2.4 0 0 0 14 16A2.4 2.4 0 0 1 16 15A2.4 2.4 0 0 1 18 16A2.4 2.4 0 0 0 20 17A2.4 2.4 0 0 0 21 16.75')


@figure
def swimming():
    f = Fig('swimming', head=(16.4, 11.4, 1.9), s=1, dy=0)
    head = (16.4, 11.4, 1.9)
    n, dur = 12, 2.4
    shoulder0, upper, fore = (13.6, 13.4), 3.7, 3.6
    start = 222
    cyc = Cycle(f, head)
    for k in range(n):
        ph = k / n
        y = 0.35 * math.sin(2 * TAU * ph)
        shoulder = (shoulder0[0], shoulder0[1] + y)
        chest = (shoulder[0] - 1.6, shoulder[1] + 2.2)
        limbs = []
        for off in (0, 180):
            a = start + 360 * ph + off
            rel = -42 - 26 * math.cos(math.radians(a - 270))
            elbow = polar(shoulder, a, upper)
            hand = polar(elbow, a + rel, fore)
            limbs.append((elbow, hand))
        (elbowN, handN), (elbowF, handF) = limbs
        cyc.frame([([handN, elbowN, shoulder, chest], [0.25, 0.3]),
                   ([lerp(shoulder, elbowF, 0.35), elbowF, handF], 0.25)],
                  (head[0], head[1] + y))
    water = f.p(WATER)
    surface = WATER + 'L21 0L3 0Z'
    waves = group([path(water)], slide([(0, 0), (0, -0.3)], dur / 2, linear=False))
    cyc.write(dur, props=[water], props_anim=[waves], clip=('swimming-surface', surface), behind=True)


PACK = 'M8.9 13.27L8.173 12.782A2 2 0 0 1 7.342 10.632L7.622 9.515A2 2 0 0 1 9.561 8L9.661 8M7.5 10.4Q8.7 11.1 9.95 10.5'


def shifted(d, dy):
    """Path [d] (absolute M, L, Q, A) moved down by [dy]."""
    import re
    out = []
    for c, args in re.findall(r'([MLQA])([^MLQA]*)', d):
        n = [float(v) for v in re.findall(r'-?\d*\.?\d+', args)]
        if c == 'A':
            n[6] += dy
        else:
            n = [v + dy if i % 2 else v for i, v in enumerate(n)]
        out.append(c + ' '.join(fmt(v) for v in n))
    return ''.join(out)


@figure
def trekking():
    """A hiker under a pack: legs through a full stride, the near hand pinned to a pole that is planted with the far
    foot and swung forward with the near one."""
    head = (11.8, 4.8, 2.2)
    f = Fig('trekking', head=head, s=0.92, dy=0.6)
    n, dur = 12, 1.6
    hip0, ground, thigh, shin = (10.3, 13.9), 21, 3.7, 3.95
    stride, lift, stance, center = 3, 1.2, 0.6, 10
    pole_len, grip_at, pole_stride, pole_center = 9, 7, 2, 16.3
    upper, fore = 4.4, 4.4
    cyc = Cycle(f, head)
    packs, poles = [], []
    for k in range(n):
        ph = k / n
        y = bob_at(ph, 0.35, stance / 2)
        hip = (hip0[0], hip0[1] + y)
        mid, neck = (10.2, 11 + y), (11, 8 + y)
        kneeN, footN = leg(hip, foot_path(ph, center, ground, stride, lift, stance), thigh, shin)
        kneeF, footF = leg(hip, foot_path(ph + 0.5, center, ground, stride, lift, stance), thigh, shin)
        tip = foot_path(ph + 0.5, pole_center, ground, pole_stride, 0.9, stance)
        tilt = -10 * (tip[0] - pole_center) / pole_stride
        top, grip = polar(tip, -90 + tilt, pole_len), polar(tip, -90 + tilt, grip_at)
        elbow = Rig.solve(neck, grip, upper, fore, 1)
        a = math.radians(tilt)
        bar = [(tip[0] + dx * math.cos(a) + math.sin(a), tip[1] + dx * math.sin(a) - math.cos(a)) for dx in (-1, 1)]
        poles.append(f'M{f.pt(*tip)}L{f.pt(*top)}M{f.pt(*bar[0])}L{f.pt(*bar[1])}')
        packs.append(f.p(shifted(PACK, y)))
        cyc.frame([([footN, kneeN, hip, mid, neck], [0.3, 0.4, 0.4]),
                   ([lerp(hip, kneeF, 0.45), kneeF, footF], 0.3),
                   ([neck, elbow, grip], 0.25)],
                  (head[0], head[1] + y))
    cyc.write(dur, props=[packs[0], poles[0]],
              props_anim=[path(packs[0], morph(packs, dur, linear=True)), path(poles[0], morph(poles, dur, linear=True))])


if __name__ == '__main__':
    for label in sys.argv[1:] or FIGURES:
        FIGURES[label]()
        print(label)

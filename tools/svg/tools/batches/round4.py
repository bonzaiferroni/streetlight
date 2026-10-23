"""New icons, round 4: TalentTag values whose icon stood in for something else (notes.md, New Icons).

Usage: python3 round4.py [labels...]   (default: all of these). Tabler style on the 24 grid, built with round2's packer.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, os.path.dirname(__file__))
from anim import fmt, group, flame_head
from forge import keys, p
import round2
from round2 import pack, build, head_el, figure_frames
from rig import Rig
from round3 import circle, dot, animated, bump, track

round2.PACKS.clear()


def about(children, cx, cy, anim):
    """[children] under [anim], a scale about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    return group([group([inner], anim)], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


# ---------- lectern: a speaker behind a podium with its gooseneck mic; they lean in and nod as they talk ----------
PODIUM_TOP = 'M5.5 11h13l-.5 2a1.6 1.6 0 0 1 -1.55 1.2h-8.9a1.6 1.6 0 0 1 -1.55 -1.2l-.5 -2'
PODIUM_LEGS = 'M8.3 14.2l.8 6.8m6.6 -6.8l-.8 6.8'
PODIUM_BASE = 'M7 21h10'
PODIUM_MIC = 'M7 11v-1.5a2.5 2.5 0 0 1 2.5 -2.5'
SPEAKER_HEAD = circle(12, 4, 2)
SPEAKER_BODY = 'M8.8 11C8.8 9.2 10.2 8 12 8C13.8 8 15.2 9.2 15.2 11'


@pack('lectern')
def lectern():
    static = [p(SPEAKER_HEAD), p(SPEAKER_BODY), p(PODIUM_TOP), p(PODIUM_LEGS), p(PODIUM_BASE), p(PODIUM_MIC)]
    dur = 3.2
    lean = keys('transform', track('0 12 11', [(0.1, '0 12 11'), (0.3, '-4 12 11'), (0.55, '-4 12 11'), (0.75, '2 12 11'), (0.9, '0 12 11')]), dur, kind='rotate')
    nod = keys('transform', track('0 0', bump('0 0', '0 .6', 0.32, 0.06, 0.1) + bump('0 0', '0 .6', 0.48, 0.06, 0.1)), dur, kind='translate')
    speaker = group([group([p(SPEAKER_HEAD)], nod), p(SPEAKER_BODY)], lean)
    return static, [speaker, p(PODIUM_TOP), p(PODIUM_LEGS), p(PODIUM_BASE), p(PODIUM_MIC)]


# ---------- model-pose: a model on the bone rig switching between two mirrored stances; camera flashes pop ----------
# Pose 0 is the static icon: left hand on hip, right arm loose, weight on the left leg. Pose 1 mirrors it. Feet stay
# planted (IK); the flame head rides the neck upright.
MODEL = Rig([
    ('hip', None, (12, 12.6)), ('chest', 'hip', (12, 10.3)), ('neck', 'chest', (12, 8)),
    ('elbowL', 'neck', (10.9, 11)), ('handL', 'elbowL', (10.5, 14)), ('elbowR', 'neck', (13.1, 11)), ('handR', 'elbowR', (13.5, 14)),
    ('kneeL', 'hip', (11.6, 16.8)), ('footL', 'kneeL', (11.2, 21)), ('kneeR', 'hip', (12.4, 16.8)), ('footR', 'kneeR', (12.8, 21)),
])
FEET = {'footL': (10.4, 21), 'footR': (13.6, 21)}
MODEL_POSES = [
    dict(move=(-0.6, 0.15), tilt=-3, hands={'handL': ((10.9, 12.4), 1), 'handR': ((14.4, 13.4), -1)}),
    dict(move=(0.6, 0.15), tilt=3, hands={'handL': ((9.6, 13.4), 1), 'handR': ((13.1, 12.4), -1)}),
]
FLASHES = [(19.5, 12, 2.2), (4.5, 6, 1.5)]


def model_frame(move, tilt, hands):
    knees = {'footL': (FEET['footL'], 1), 'footR': (FEET['footR'], -1)}
    pose = MODEL.pose(rot={'chest': tilt}, move=move, ik={**hands, **knees})
    pose['head'] = pose['neck']
    return pose


def flash(x, y, r):
    """A four-point sparkle centered at (x, y), [r] from center to tip."""
    return (f'M{fmt(x)} {fmt(y - r)}a{fmt(r)} {fmt(r)} 0 0 0 {fmt(r)} {fmt(r)}a{fmt(r)} {fmt(r)} 0 0 0 {fmt(-r)} {fmt(r)}'
            f'a{fmt(r)} {fmt(r)} 0 0 0 {fmt(-r)} {fmt(-r)}a{fmt(r)} {fmt(r)} 0 0 0 {fmt(r)} {fmt(-r)}')


@pack('model-pose')
def model_pose():
    p0, p1 = (model_frame(**m) for m in MODEL_POSES)
    chains = [(['footL', 'kneeL', 'hip', 'kneeR', 'footR'], 0.2), (['hip', 'chest', 'neck'], 0.3),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], [0.1, 0.35, 0.1])]
    dur = 3.2
    static, figure = figure_frames([p0, p0, p1, p1], chains, (12, 5.4, 2), dur)
    pops = []
    for (x, y, r), at in zip(FLASHES, (0.5, 0.02)):  # as each stance lands
        pop = keys('transform', track('1', bump('1', '1.5', at, 0.04, 0.12)), dur, kind='scale')
        pops.append(about([p(flash(x, y, r))], x, y, pop))
    return static + [p(flash(*f)) for f in FLASHES], figure + pops


# ---------- caricature: a pencil draws a flame-headed bust on the easel's pad; the page flips over for a fresh one ----------
EASEL_BOARD = 'M4 4a1 1 0 0 1 1 -1h14a1 1 0 0 1 1 1v10a1 1 0 0 1 -1 1h-14a1 1 0 0 1 -1 -1z'
EASEL_LEGS = 'M8 15l-2 6m10 -6l2 6m-6 -6v3'
SKETCH_HEAD = flame_head(11, 7.6, 2.4)
SKETCH_BODY = 'M7.2 13.4Q7.6 11.4 11 11.4Q14.4 11.4 14.8 13.4'
PENCIL = 'M5.5 -7l1.5 1.5l-5 5l-2 .5l.5 -2z'  # tip at the origin
PAGE_TOP = 3


def length(d):
    """The length of path [d] (absolute M, L, Q and C only), by sampling."""
    import re
    total, pen = 0.0, None
    for cmd, args in re.findall(r'([MLQC])([^MLQC]*)', d):
        v = [float(x) for x in re.findall(r'-?\d*\.?\d+', args)]
        if cmd == 'M':
            pen = (v[0], v[1])
            continue
        pts = [pen] + [(v[i], v[i + 1]) for i in range(0, len(v), 2)]
        def at(t, ps=pts):
            while len(ps) > 1:
                ps = [((1 - t) * a[0] + t * b[0], (1 - t) * a[1] + t * b[1]) for a, b in zip(ps, ps[1:])]
            return ps[0]
        prev = pen
        for k in range(1, 101):
            q = at(k / 100)
            total += math.hypot(q[0] - prev[0], q[1] - prev[1])
            prev = q
        pen = pts[-1]
    return total


def dashed(d, times, offsets, dur):
    return (f'<path d="{d}" pathLength="1" stroke-dasharray="1 1.1">\n'
            f'    <animate attributeName="stroke-dashoffset" values="{";".join(offsets)}" keyTimes="{";".join(fmt(t) for t in times)}" dur="{dur}s" repeatCount="indefinite"/>\n</path>')


@pack('caricature')
def caricature():
    end = (14.8, 13.4)
    pencil = group([p(PENCIL)], attrs=f'transform="translate({fmt(end[0])} {fmt(end[1])})"')
    drawing = [p(SKETCH_HEAD), p(SKETCH_BODY)]
    static = [p(EASEL_BOARD), p(EASEL_LEGS)] + drawing + [pencil]
    dur = 4.4
    # The top page, drawn, flips up over the easel and fades; the fresh page beneath is blank until the pencil draws.
    flip = (f'<animateTransform attributeName="transform" type="scale" values="1 1;1 1;1 -0.3;1 1;1 1" keyTimes="0;0.12;0.3;0.5;1" '
            f'dur="{dur}s" repeatCount="indefinite" calcMode="spline" keySplines="0 0 1 1;0.5 0 0.8 1;0 0 1 1;0 0 1 1"/>')
    gone = f'<animate attributeName="opacity" values="1;1;0;0;1;1" keyTimes="0;0.22;0.3;0.88;0.89;1" dur="{dur}s" repeatCount="indefinite"/>'
    top_page = group([group([p(EASEL_BOARD)] + drawing, attrs=f'transform="translate(0 {-PAGE_TOP})"')], flip)
    top_page = group([group([top_page], gone)], attrs=f'transform="translate(0 {PAGE_TOP})"')
    head_len, body_len = length(SKETCH_HEAD), length(SKETCH_BODY)
    split = head_len / (head_len + body_len)
    # hidden while the top page covers it, so the two never draw over each other; the swaps happen only under an opaque
    # top page (it returns over the finished drawing, then the ink hides), or the figure blinks
    ink = [dashed(SKETCH_HEAD, [0, 0.35, 0.65, 0.9, 0.905, 1], ['1', '1', '0', '0', '1', '1'], dur),
           dashed(SKETCH_BODY, [0, 0.68, 0.82, 0.9, 0.905, 1], ['1', '1', '0', '0', '1', '1'], dur)]
    # The pencil's tip rides the same path as the ink: the head, then the shoulders, at the ink's pace.
    draw = (f'<animateMotion path="{SKETCH_HEAD}{SKETCH_BODY}" calcMode="linear" keyPoints="1;1;0;0;{fmt(split)};{fmt(split)};1;1" '
            f'keyTimes="0;0.2;0.201;0.35;0.65;0.68;0.82;1" dur="{dur}s" repeatCount="indefinite"/>')
    lift = keys('transform', [(0, '0 0'), (0.06, '0 0'), (0.16, '3 -2'), (0.3, '3 -2'), (0.35, '0 0'), (1, '0 0')], dur, kind='translate')
    hand = group([group([p(PENCIL)], draw)], lift)
    # the fresh page shows only once the top page lifts, so frame 0 draws the board once
    under = f'<path d="{EASEL_BOARD}" opacity="0">\n    <animate attributeName="opacity" values="0;0;1;1;0;0" keyTimes="0;0.12;0.121;0.905;0.906;1" dur="{dur}s" repeatCount="indefinite"/>\n</path>'
    return static, [under, p(EASEL_LEGS)] + ink + [top_page, hand]


# ---------- bouquet: three blooms over a paper wrap; they sway in turn ----------
BLOOMS = [(12, 5.5, 2.4), (7.6, 8.6, 2), (16.4, 8.6, 2)]
WRAP = 'M6.5 12.5h11l-4.5 8.5h-2z'
BOW = 'M12 16.5l-2 -1.2v2.4zm0 0l2 -1.2v2.4z'


def bloom(cx, cy, r, petals=5):
    """A scalloped bloom of [petals] around (cx, cy), radius [r], with a dot at its heart."""
    d = ''
    for k in range(petals + 1):
        a = math.radians(-90 + k * 360 / petals)
        x, y = cx + r * math.cos(a), cy + r * math.sin(a)
        if k == 0:
            d = f'M{fmt(x)} {fmt(y)}'
        else:
            d += f'A{fmt(r * 0.6)} {fmt(r * 0.6)} 0 0 1 {fmt(x)} {fmt(y)}'
    return d + dot(cx, cy)


@pack('bouquet')
def bouquet():
    blooms = [bloom(*b) for b in BLOOMS]
    stems = 'M12 8v4.5M8.5 10.5l1.5 2M15.5 10.5l-1.5 2'
    static = [p(d) for d in blooms] + [p(stems), p(WRAP), p(BOW)]
    dur = 3.6
    out = []
    for i, (b, (cx, cy, r)) in enumerate(zip(blooms, BLOOMS)):
        at = 0.05 + i * 0.25
        sway = keys('transform', track('0 12 12.5', bump('0 12 12.5', f'{-4 if i != 1 else 4} 12 12.5', at, 0.15, 0.3)), dur, kind='rotate')
        out.append(group([p(b)], sway))
    return static, out + [p(stems), p(WRAP), p(BOW)]


if __name__ == '__main__':
    for label in sys.argv[1:] or round2.PACKS:
        build(label)
        print('built', label)

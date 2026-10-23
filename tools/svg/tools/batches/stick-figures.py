"""Stick figures with the flame head and refined lines (notes.md, Stick Figures). Yoga has its own script.

Usage: python3 stick-figures.py [labels...]   (default: all). Each figure keeps its old pose and motion.
"""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import fmt, SVG_DIR as SVG
from stick import Fig, anim, path, group

FIGURES = {}


def figure(fn):
    FIGURES[fn.__name__.replace('_', '-')] = fn
    return fn


def bob(f, children, dy, dur):
    """[children] rise or sink by [dy] figure units and back."""
    return group(children, anim(['0 0', f'0 {fmt(dy * f.s)}'], dur, kind='translate'))


def rot(f, children, deg, cx, cy, dur, begin=None):
    """[children] rock to [deg] about figure point (cx, cy) and back."""
    c = f.pt(cx, cy)
    return group(children, anim([f'0 {c}', f'{fmt(deg)} {c}'], dur, kind='rotate', begin=begin))


def whole(f, parts, *anims):
    """Every part and the head, moved together by [anims]."""
    return group([path(d) for d in parts] + [path(f.head())], *anims)


@figure
def walk():
    f = Fig('walk', head=(13.2, 4.4, 2.2), s=0.92, dy=0.6)
    body = 'M16 21C15.4 19.3 14.9 17.9 14 17C13.1 16 11.5 15.1 11 14C10.6 13 11 10.2 12 8'
    back_leg = 'M7 21Q8.7 19.2 10 17'
    arms = 'M6 12C6.6 10.4 7.4 9.3 8.3 9C9.5 8.5 10.8 8.2 12 8C13.2 8.9 14 10.2 15 11C15.9 11.7 17 11.9 18 12'
    parts = [f.p(d) for d in (back_leg, body, arms)]
    f.write(parts, [whole(f, parts, anim(['0 0', f'0 {fmt(-0.5 * f.s)}'], 1.2, kind='translate'))])
    return f, parts


@figure
def run():
    f = Fig('run', head=(13.1, 4.8, 2.2), s=0.92, dy=0.4)
    back_leg = 'M4 17C5.8 17.3 7.6 18.1 8.6 18C9.2 17.9 9.6 17.2 9.75 16.5'
    body = 'M15 21C15 19.4 15.2 17.8 14.2 16.9C13.2 16 11.6 15.2 11 14C10.6 13 11 10.2 12 8'
    arms = 'M7 12C6.9 10.8 6.9 9.6 7.6 9.1C8.8 8.4 10.6 8.2 12 8C13.2 8.9 14 10.2 15 11C15.9 11.7 17 11.9 18 12'
    parts = [f.p(d) for d in (back_leg, body, arms)]
    f.write(parts, [whole(f, parts, anim(['0 0', f'0 {fmt(-0.6 * f.s)}'], 1, kind='translate'))])
    return f, parts


@figure
def karate():
    f = Fig('karate', head=(18.3, 4.3, 2.2), s=0.92, dy=0.55)
    kick = 'M3 9C4.6 9.2 6.2 9.4 7.5 10C8.6 10.6 9.6 11.6 10.5 12.5'
    body = 'M13 21C13 18.5 13 15.5 13.2 14C13.5 12 14.8 9.6 16 7.5'
    arms = 'M8 4.5C9.4 5.3 10.6 6 12 6.5C13.4 7 14.7 7.3 16 7.5C17.6 8.7 19.4 9.8 19.8 11.2C20 12.4 19 13.6 18 14.5'
    parts = [f.p(d) for d in (kick, body, arms)]
    kicking = rot(f, [path(parts[0])], 12, 10.5, 12.5, 2)
    kicking = kicking.replace('keyTimes="0;0.5;1"', 'keyTimes="0;0.15;0.4;0.65;1"').replace(
        f'values="0 {f.pt(10.5, 12.5)};12 {f.pt(10.5, 12.5)};0 {f.pt(10.5, 12.5)}"',
        f'values="0 {f.pt(10.5, 12.5)};0 {f.pt(10.5, 12.5)};12 {f.pt(10.5, 12.5)};0 {f.pt(10.5, 12.5)};0 {f.pt(10.5, 12.5)}"').replace(
        'keySplines="0.42 0 0.58 1;0.42 0 0.58 1"', 'keySplines="0.42 0 0.58 1;0.42 0 0.58 1;0.42 0 0.58 1;0.42 0 0.58 1"')
    f.write(parts, [kicking, path(parts[1]), path(parts[2]), path(f.head())])
    return f, parts


@figure
def bike():
    f = Fig('bike', head=(15.1, 4.8, 2.2), s=0.92, dy=0.4)
    wheels = ['M2 18A3 3 0 1 0 8 18A3 3 0 1 0 2 18', 'M16 18A3 3 0 1 0 22 18A3 3 0 1 0 16 18']
    rider = 'M12 19C12 17.6 12 16.4 11.6 15.6C11.1 14.6 10.2 13.4 9.6 12.6Q9.1 11.8 9.8 11.1C11 10 12.6 8.8 14 8C14.9 8.9 15.4 10 16 10.6C16.6 11.1 17.6 11 19 11'
    parts = [f.p(d) for d in wheels + [rider]]
    hubs = []
    for cx in (5, 19):
        c = f.pt(cx, 18)
        hubs.append(group([path(f.p(f'M{fmt(cx - 1.8)} 18L{fmt(cx + 1.8)} 18'))],
                          f'<animateTransform attributeName="transform" type="rotate" from="0 {c}" to="360 {c}" dur="1.5s" repeatCount="indefinite"/>'))
    riding = group([path(parts[2]), path(f.head())], anim(['0 0', f'0 {fmt(-0.4 * f.s)}'], 1.5, kind='translate'))
    f.write(parts, [path(parts[0]), path(parts[1])] + hubs + [riding])
    return f, parts


@figure
def ski_jumping():
    f = Fig('ski-jumping', head=(12, 3.8, 2.2), s=0.88, dy=1.1)
    legs = 'M7 17.5C9 15.6 11 13.6 12 13C13 13.6 15 15.6 17 17.5'
    torso = 'M12 13L12 7'
    arms = 'M7 11C9 8.6 10.5 7 12 7C13.5 7 15 8.6 17 11'
    skis = ['M15.103 21.58L21.865 7.078A2 2 0 0 0 20.897 4.421', 'M8.897 21.58L2.135 7.077A2 2 0 0 1 3.103 4.42']
    parts = [f.p(d) for d in [legs, torso, arms] + skis]
    f.write(parts, [whole(f, parts, anim(['0 0', f'0 {fmt(-0.7 * f.s)}'], 3, kind='translate'))])
    return f, parts


@figure
def old():
    f = Fig('old', head=(8.6, 4.8, 2.2), s=0.92, dy=0.5)
    body = 'M11 21C10.8 19.6 10.6 18.2 10 17.2C9.4 16.2 8.4 15.2 8 14C7.6 12.4 7.4 9.8 8 8'
    arms = 'M5 14C4.6 13 4.2 12 4.2 11.4Q4.2 10.8 4.8 10.4C5.8 9.6 7 8.6 8 8C9 8.6 10 9.4 10.8 9.9C11.8 10.4 12.9 10.4 14 10.5'
    back_leg = 'M7 17Q6.1 19.1 5 21'
    cane = 'M16 21L16 12.5A1.5 1.5 0 0 1 19 12.5L19 13'
    parts = [f.p(d) for d in (body, arms, back_leg, cane)]
    walker = group([path(d) for d in parts[:3]] + [path(f.head())], anim(['0 0', f'0 {fmt(-0.4 * f.s)}'], 2, kind='translate'))
    c = f.pt(16, 21)
    cane = group([path(parts[3])], anim([f'0 {c}', f'6 {c}', f'0 {c}', f'-6 {c}'], 2, kind='rotate'))
    f.write(parts, [walker, cane])
    return f, parts


@figure
def pray():
    f = Fig('pray', head=(11.6, 5.4, 2.2), s=1, dy=0.6)
    lower = 'M7 20L13.8 20C15 20 15.3 19 14.6 18.4L11.8 16.1Q11 15.5 11 14.8'
    upper = 'M11 14.8C10.8 13 10.7 10.6 11 9C12.4 10 14 11.3 14.8 11.8C15.5 12.2 16.3 11 17 10'
    parts = [f.p(d) for d in (lower, upper)]
    bow = rot(f, [path(parts[1]), path(f.head())], 3.5, 11, 14.8, 3)
    f.write(parts, [path(parts[0]), bow])
    return f, parts


@figure
def stretching():
    f = Fig('stretching', head=(16.2, 5.5, 2.2), s=0.95, dy=0.4)
    back_leg = 'M5 20C7 19.8 8.8 19.7 9.8 19.5C10.4 19.3 10.8 18.4 11 17.5'
    lower = 'M18 20L18 16.4Q18 15 16.6 15L14.6 15Q12.6 15 13.3 13.1'
    upper = 'M13.3 13.1C13.8 11.7 14.3 10 15 8.5C13 8.7 11 9 9.8 9.4C9.3 9.6 9.9 10.8 11 11.5'
    parts = [f.p(d) for d in (back_leg, lower, upper)]
    lean = rot(f, [path(parts[2]), path(f.head())], -4, 13.3, 13.1, 3)
    f.write(parts, [path(parts[0]), path(parts[1]), lean])
    return f, parts


@figure
def swimming():
    f = Fig('swimming', head=(16.2, 10, 1.9), s=1, dy=0)
    body = 'M6 11C7.3 10.3 8.6 9.6 9.6 9.1Q10.2 8.8 10.8 9.3L12.9 11.4Q13.6 12.1 13 12.9L12 14'
    water = ('M3 16.75A2.4 2.4 0 0 0 4 17A2.4 2.4 0 0 0 6 16A2.4 2.4 0 0 1 8 15A2.4 2.4 0 0 1 10 16A2.4 2.4 0 0 0 12 17'
             'A2.4 2.4 0 0 0 14 16A2.4 2.4 0 0 1 16 15A2.4 2.4 0 0 1 18 16A2.4 2.4 0 0 0 20 17A2.4 2.4 0 0 0 21 16.75')
    parts = [f.p(d) for d in (body, water)]
    swimmer = group([path(parts[0]), path(f.head())], anim(['0 0', f'0 {fmt(0.5 * f.s)}'], 2, kind='translate'))
    waves = group([path(parts[1])], anim(['0 0', f'0 {fmt(-0.3 * f.s)}'], 2, kind='translate'))
    f.write(parts, [swimmer, waves])
    return f, parts


@figure
def massage():
    f = Fig('massage', head=(9, 4.4, 2), s=0.92, dy=0.6)
    client_head = (4.4, 15.4, 1.8)
    table = 'M4 22L8 20L8 17L20 17'
    legs = 'M11 20L20 20'
    masseur = 'M8 14C9 13.4 10.2 12.6 10.8 12.1Q11.2 11.8 11.3 11.2L12 8C15 9 15 12 15 14'
    parts = [f.p(d) for d in (table, legs, masseur)]
    from anim import flame_head
    client = f.p(flame_head(*client_head))
    stroke = f' stroke-width="{fmt(f.small_head)}"'
    working = group([path(parts[2]), path(f.head())], anim(['0 0', f'0 {fmt(0.7 * f.s)}'], 1.6, kind='translate'))
    f.write(parts, [path(client), path(parts[0]), path(parts[1]), working])
    for v in ('small', 'large', 'very-large'):
        p = f'{SVG}/massage-{v}.svg'
        s = open(p).read()
        head_attr = stroke if v == 'small' else ''
        s = s.replace(f'    <path d="{parts[0]}"/>', f'    <path{head_attr} d="{client}"/>\n    <path d="{parts[0]}"/>')
        open(p, 'w').write(s)
    return f, parts + [client]


@figure
def trekking():
    f = Fig('trekking', head=(11.8, 4.8, 2.2), s=0.92, dy=0.6)
    back_leg = 'M7 21L9 17'
    body = 'M13 21Q13.15 16.79 10.6 14.6Q10 14.1 10.1 13.3C10.3 11.4 10.5 9.6 11 8'
    pack = 'M8.9 13.27L8.173 12.782A2 2 0 0 1 7.342 10.632L7.622 9.515A2 2 0 0 1 9.561 8L9.661 8'
    arm = 'M11 8Q13.15 11.85 17 14'
    pole = ['M17 12L17 21', 'M16 20L18 20']
    parts = [f.p(d) for d in [back_leg, body, pack, arm] + pole]
    c = f.pt(11, 8)
    swing = group([path(parts[3]), path(parts[4]), path(parts[5])],
                  anim([f'0 {c}', f'-4 {c}', f'0 {c}', f'3 {c}'], 1.6, kind='rotate'))
    f.write(parts, [path(parts[0]), path(parts[1]), path(parts[2]), path(f.head()), swing])
    return f, parts


@figure
def dance():
    f = Fig('dance', head=(12.6, 4.0, 2.0), s=0.92, dy=0.6)
    torso = 'M13.5 7L12 13'
    leg = 'M12 13Q8.57 16.28 8 21'
    kick = 'M12 13Q14.49 16.16 18.5 16.5'
    arm_r = 'M13.3 8L17 5.5'
    arm_l = 'M13.3 8L9 9.5'
    parts = [f.p(d) for d in (torso, leg, kick, arm_r, arm_l)]

    def sway(deg, cx, cy):
        c = f.pt(cx, cy)
        vals = ';'.join(f'{fmt(d)} {c}' for d in deg)
        return (f'<animateTransform attributeName="transform" type="rotate" values="{vals}" dur="2s" '
                f'repeatCount="indefinite" keyTimes="0;0.25;0.5;0.75;1" calcMode="spline" '
                f'keySplines="0.33 0.66 0.67 1.00;0.33 0.00 0.67 0.34;0.33 0.66 0.67 1.00;0.33 0.00 0.67 0.34"/>')
    body = group([path(f.head()), path(parts[0]), path(parts[1]),
                  group([path(parts[2])], sway([0, -6, 0, 2, 0], 12, 13)),
                  group([path(parts[3])], sway([0, -14, 0, 10, 0], 13.3, 8)),
                  group([path(parts[4])], sway([0, 14, 0, -12, 0], 13.3, 8))],
                 sway([0, 5, 0, -4, 0], 8, 21))
    f.write(parts, [body])
    return f, parts


if __name__ == '__main__':
    for label in sys.argv[1:] or FIGURES:
        f, parts = FIGURES[label]()
        print(label, 'bounds', ' '.join(fmt(v) for v in f.bounds(parts)))

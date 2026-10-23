import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# ---------- local helpers ----------

def split(label):
    """The open tag, frame elements and visible elements of [label]."""
    head, els = load(label)
    frame = [e for e in els if is_frame(e) or 'stroke="none"' in e and 'fill="none"' in e]
    rest = [e for e in els if e not in frame]
    return head, frame, rest


def keyed(attr, pairs, dur, kind=None, ease=True, extra=''):
    """An animation through (time fraction, value) [pairs]; the first and last values should match."""
    ts = [p[0] for p in pairs]
    vals = [p[1] for p in pairs]
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"'
    if kind:
        a += f' type="{kind}"'
    a += f' values="{";".join(vals)}" keyTimes="{";".join(fmt(t) for t in ts)}" dur="{dur}s" repeatCount="indefinite"'
    if ease:
        a += f' calcMode="spline" keySplines="{splines(len(vals) - 1)}"'
    return a + extra + '/>'


def bump(slot, n, rest, peak, w=1.0):
    """Pairs that hold [rest], rise to [peak] and fall back within slot [slot] of [n]."""
    a = slot / n
    b = (slot + w) / n
    m = (a + b) / 2
    pts = [(0, rest), (a, rest), (m, peak), (b, rest), (1, rest)]
    out = []
    for t, v in pts:
        if out and abs(out[-1][0] - t) < 1e-9:
            out[-1] = (t, v)
        else:
            out.append((t, v))
    return out


def rock(children, cx, cy, deg, dur, phase=None):
    """Rocks [children] about (cx, cy) by +-[deg]."""
    vals = [f'0 {fmt(cx)} {fmt(cy)}', f'{fmt(deg)} {fmt(cx)} {fmt(cy)}', f'0 {fmt(cx)} {fmt(cy)}', f'{fmt(-deg)} {fmt(cx)} {fmt(cy)}']
    return group(children, osc('transform', vals, dur, kind='rotate'))


def tilt(children, cx, cy, values, dur):
    """Rotates [children] about (cx, cy) through degree [values], back to the first."""
    return group(children, osc('transform', [f'{fmt(v)} {fmt(cx)} {fmt(cy)}' for v in values], dur, kind='rotate'))


def turn(children, cx, cy, deg, dur):
    """Turns [children] by [deg] with an ease each loop; for shapes symmetric under [deg]."""
    a = (f'<animateTransform attributeName="transform" type="rotate" values="0 {fmt(cx)} {fmt(cy)};{fmt(deg)} {fmt(cx)} {fmt(cy)}" '
         f'keyTimes="0;1" calcMode="spline" keySplines="{EASE}" dur="{dur}s" repeatCount="indefinite"/>')
    return group(children, a)


def scale_at(children, cx, cy, values, dur, pairs=None):
    """Scales [children] about (cx, cy) through [values] (each 's' or 'sx sy')."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    if pairs:
        an = keyed('transform', pairs, dur, kind='scale')
    else:
        an = osc('transform', values, dur, kind='scale')
    mid = group([inner], an)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def with_anim(el, *anims):
    """[el] with animation children."""
    tag = re.match(r'<([a-z]+)', el).group(1)
    return el[:-2].rstrip() + '>\n' + '\n'.join('    ' + a for a in anims) + f'\n</{tag}>'


def erase(el, dur, from_end=True):
    """Erases [el] along its length and draws it back."""
    hidden = '1.2' if from_end else '-1.2'
    el2 = el.replace('<path ', '<path pathLength="1" stroke-dasharray="1 2" ', 1)
    return with_anim(el2, osc('stroke-dashoffset', ['0', hidden], dur))


def fade(el, pairs, dur, ease=True):
    """[el] with opacity through [pairs]."""
    return with_anim(el, keyed('opacity', pairs, dur, ease=ease))


def blink(el, dur):
    """[el] blinking on and off."""
    return with_anim(el, f'<animate attributeName="opacity" values="1;0" keyTimes="0;0.5" calcMode="discrete" dur="{dur}s" repeatCount="indefinite"/>')


def hub(cx, cy, r, dur, reverse=False):
    """A spoke turning in a wheel at (cx, cy)."""
    return group([f'<path d="M{fmt(cx - r)} {fmt(cy)}h{fmt(2 * r)}"/>'], spin(cx, cy, dur, reverse=reverse))


def d_of(el):
    return re.search(r'\bd="([^"]+)"', el).group(1)


def scale_rel_y(d, a):
    """Path [d] with every relative y offset scaled by [a]; the first M stays."""
    toks = re.findall(r'[a-zA-Z]|-?\d*\.?\d+(?:e-?\d+)?', d)
    out, cmd, idx = [], None, 0
    for t in toks:
        if t.isalpha():
            cmd, idx = t, 0
            out.append(t)
            continue
        v = float(t)
        if cmd in 'clsqt' and idx % 2 == 1:
            v *= a
        elif cmd == 'v':
            v *= a
        idx += 1
        out.append(fmt(v))
    s = ''
    for t in out:
        if t[0].isalpha():
            s += t
        elif s and not s[-1].isalpha():
            s += ' ' + t
        else:
            s += t
    return s


def amp(el, amps, dur):
    """[el] whose wave amplitude moves through [amps] (scale of relative y)."""
    d = d_of(el)
    vals = [scale_rel_y(d, a) for a in amps]
    return with_anim(el, osc('d', vals, dur))


def path(d, extra=''):
    return f'<path d="{d}"{extra}/>'


# ---------- icons ----------

def terminal():
    head, fr, e = split('terminal')
    save('terminal', head, fr + [e[0], blink(e[1], 1.2)])


def test_pipe():
    head, fr, e = split('test-pipe')
    # the drop falls and fades, then reforms
    drop = group([e[2]], keyed('transform', [(0, '0 0'), (0.55, '0 1.5'), (0.75, '0 1.5'), (0.8, '0 0'), (1, '0 0')], 2.4, kind='translate', ease=False))
    dg = group([drop], keyed('opacity', [(0, '1'), (0.4, '1'), (0.6, '0'), (0.8, '0'), (1, '1')], 2.4, ease=False))
    save('test-pipe', head, fr + [e[0], e[1], e[3], dg])


def thumb_up():
    head, fr, e = split('thumb-up')
    save('thumb-up', head, fr + [tilt(e, 5, 20, [0, -7, 0, -3], 2.4)])


def thumb_down():
    head, fr, e = split('thumb-down')
    save('thumb-down', head, fr + [tilt(e, 5, 4, [0, 7, 0, 3], 2.4)])


def ticket():
    head, fr, e = split('ticket')
    save('ticket', head, fr + [rock(e, 12, 12, 4, 3)])


def ticket_off():
    head, fr, e = split('ticket-off')
    save('ticket-off', head, fr + e[:3] + [erase(e[3], 3)])


def toilet_paper():
    head, fr, e = split('toilet-paper')
    # the sheet unrolls a little
    sheet = scale_at([e[3]], 15, 10, ['1 1', '1 1.12'], 2.5)
    save('toilet-paper', head, fr + [e[0], e[1], e[2], e[4], sheet])


def tool():
    head, fr, e = split('tool')
    save('tool', head, fr + [tilt(e, 12, 12, [0, -10, 0, 4], 2.5)])


def tools():
    head, fr, e = split('tools')
    # the pencil writes along its axis
    pencil = shift(e[:2], -0.7, 0.7, 2)
    save('tools', head, fr + e[2:] + [pencil])


def tools_kitchen():
    head, fr, e = split('tools-kitchen')
    save('tools-kitchen', head, fr + [rock(e, 12, 21, 4, 3)])


def tools_kitchen_3():
    head, fr, e = split('tools-kitchen-3')
    fork = tilt([e[0]], 7, 21, [0, -5, 0, 3], 3)
    spoon = tilt(e[1:], 17, 21, [0, 3, 0, -5], 3)
    save('tools-kitchen-3', head, fr + [fork, spoon])


def torii():
    head, fr, e = split('torii')
    save('torii', head, fr + [pulse(e, 12, 12, 0.06, 3.5)])


def tractor():
    head, fr, e = split('tractor')
    big, dot, small, axle, body, stack = e
    save('tractor', head, fr + [big, small, hub(7, 15, 1.4, 3), hub(19, 17, 0.8, 1.2, reverse=False),
                                shift([axle, body, stack], 0, -0.4, 1.2)])


def traffic_lights():
    head, fr, e = split('traffic-lights')
    out = fr + e
    for i, el in enumerate(e[1:]):
        vals = ['0'] * 4
        vals[i + 1] = '1'
        lit = el.replace('<path ', '<path fill="currentColor" ', 1)
        out.append(with_anim(lit.replace('/>', ' fill-opacity="0"/>'),
                             f'<animate attributeName="fill-opacity" values="{";".join(vals)}" keyTimes="0;0.25;0.5;0.75" calcMode="discrete" dur="4s" repeatCount="indefinite"/>'))
    save('traffic-lights', head, out)


def vehicle(label, wheels, body, hubs):
    head, fr, e = split(label)
    save(label, head, fr + [e[i] for i in wheels] + [hub(x, y, 0.8, 1.5) for x, y in hubs] + [shift([e[i] for i in body], 0, -0.5, 1.2)])


def transit_bus():
    vehicle('transit-bus', [0, 1], [2, 3, 4, 5, 6], [(6, 17), (18, 17)])


def truck():
    vehicle('truck', [0, 1], [2], [(7, 17), (17, 17)])


def truck_delivery():
    head, fr, e = split('truck-delivery')
    speed = group([e[3]], osc('transform', ['0 0', '-1.2 0'], 1.2, kind='translate'))
    save('truck-delivery', head, fr + [e[0], e[1], hub(7, 17, 0.8, 1.5), hub(17, 17, 0.8, 1.5),
                                       shift([e[2], speed], 0, -0.5, 1.2)])


def trash():
    head, fr, e = split('trash')
    lid = group([e[0], e[4]], keyed('transform', [(0, '0 0'), (0.25, '0 -1.2'), (0.5, '0 0'), (1, '0 0')], 2.4, kind='translate'))
    save('trash', head, fr + [e[1], e[2], e[3], lid])


def trash_x():
    head, fr, e = split('trash-x')
    lid = group([e[0], e[2]], keyed('transform', [(0, '0 0'), (0.25, '0 -1.2'), (0.5, '0 0'), (1, '0 0')], 2.4, kind='translate'))
    save('trash-x', head, fr + [e[1], e[3], lid])


def trees():
    head, fr, e = split('trees')
    pine = tilt(e[:2], 15, 21, [0, 3, 0, -3], 4)
    leafy = tilt(e[2:], 8, 21, [0, -3, 0, 3], 4)
    save('trees', head, fr + [pine, leafy])


def trekking():
    head, fr, e = split('trekking')
    save('trekking', head, fr + [shift(e, 0, -0.5, 1.4)])


def triangle():
    head, fr, e = split('triangle')
    save('triangle', head, fr + [rock(e, 12, 2.5, 4, 2)])


def trophy():
    head, fr, e = split('trophy')
    save('trophy', head, fr + [rock(e, 12, 21, 5, 2.5)])


def typography():
    head, fr, e = split('typography')
    save('typography', head, fr + [pulse(e, 12, 12, 0.06, 3)])


def underline():
    head, fr, e = split('underline')
    save('underline', head, fr + [e[0], erase(e[1], 3)])


def unlink():
    head, fr, e = split('unlink')
    a = shift([e[2], e[0], e[4]], 0.6, -0.6, 2.4)
    b = shift([e[3], e[5], e[6]], -0.6, 0.6, 2.4)
    save('unlink', head, fr + [e[1], a, b])


def upload():
    head, fr, e = split('upload')
    save('upload', head, fr + [e[0], shift(e[1:], 0, -1.2, 1.6)])


def user():
    head, fr, e = split('user')
    save('user', head, fr + [e[1], shift([e[0]], 0, -0.5, 2.5)])


def user_heart():
    head, fr, e = split('user-heart')
    beat = scale_at([e[2]], 18, 18.5, None, 1.6,
                    pairs=[(0, '1'), (0.15, '1.15'), (0.3, '1'), (0.45, '1.1'), (0.6, '1'), (1, '1')])
    save('user-heart', head, fr + e[:2] + [beat])


def user_minus():
    head, fr, e = split('user-minus')
    save('user-minus', head, fr + e[:2] + [scale_at([e[2]], 19, 19, ['1 1', '0.6 1'], 2)])


def user_plus():
    head, fr, e = split('user-plus')
    save('user-plus', head, fr + [e[0], e[3], scale_at(e[1:3], 19, 19, ['1', '1.25'], 2)])


def users_group():
    head, fr, e = split('users-group')
    people = [e[0:2], e[2:4], e[4:6]]
    out = fr
    for i, p in enumerate([people[2], people[0], people[1]]):
        out.append(group(p, keyed('transform', bump(i, 3, '0 0', '0 -0.6'), 3, kind='translate')))
    save('users-group', head, out)


def vaccine():
    head, fr, e = split('vaccine')
    plunger = shift(e[:2], -0.8, 0.8, 2)
    save('vaccine', head, fr + e[2:] + [plunger])


def vaccine_bottle():
    head, fr, e = split('vaccine-bottle')
    save('vaccine-bottle', head, fr + [e[0], e[1], e[3], e[4], rock([e[2]], 12, 12, 5, 2.4)])


def vector():
    head, fr, e = split('vector')
    centers = [(5, 5), (19, 5), (19, 19), (5, 19)]
    out = fr + e[4:]
    for i, (sq, (cx, cy)) in enumerate(zip(e[:4], centers)):
        # each handle turns a quarter in its own slot
        a, b = i / 4, (i + 1) / 4
        pts = [(0, f'0 {cx} {cy}'), (a, f'0 {cx} {cy}'), (b, f'90 {cx} {cy}'), (1, f'90 {cx} {cy}')]
        pts = [p for j, p in enumerate(pts) if j == 0 or abs(p[0] - pts[j - 1][0]) > 1e-9]
        if pts[-1][0] < 1:
            pts.append((1, pts[-1][1]))
        out.append(group([sq], keyed('transform', pts, 4, kind='rotate')))
    save('vector', head, out)


def video():
    head, fr, e = split('video')
    dot = blink('<circle cx="6.2" cy="9.2" r="0.9" fill="currentColor" stroke="none"/>', 1.6)
    save('video', head, fr + e + [dot])


def vinyl():
    head, fr, e = split('vinyl')
    groove = group(['<path d="M12 6.5a5.5 5.5 0 0 1 4.76 2.75"/>'], spin(12, 12, 3))
    save('vinyl', head, fr + e + [groove])


def volume():
    head, fr, e = split('volume')
    w1 = fade(e[0], [(0, '1'), (0.25, '0.25'), (0.5, '1'), (1, '1')], 2.4)
    w2 = fade(e[1], [(0, '1'), (0.25, '1'), (0.5, '0.25'), (0.75, '1'), (1, '1')], 2.4)
    save('volume', head, fr + [e[2], w1, w2])


def volume_off():
    head, fr, e = split('volume-off')
    save('volume-off', head, fr + e[:3] + [erase(e[3], 3)])


def walk():
    head, fr, e = split('walk')
    save('walk', head, fr + [shift(e, 0, -0.5, 1.2)])


def wand():
    head, fr, e = split('wand')
    s1 = scale_at([e[2]], 9, 5, None, 2.4, pairs=bump(0, 2, '1', '0.55'))
    s2 = scale_at([e[3]], 19, 15, None, 2.4, pairs=bump(1, 2, '1', '0.55'))
    save('wand', head, fr + e[:2] + [s1, s2])


def wash_hand():
    head, fr, e = split('wash-hand')
    save('wash-hand', head, fr + [e[0], e[1], e[3], shift([e[2]], 0.6, 0, 2, extra_values=['0 0', '-0.6 0'])])


def wash_machine():
    head, fr, e = split('wash-machine')
    save('wash-machine', head, fr + e[:5] + [group([e[5]], spin(12, 14, 3))])


def wave(label, amps, dur):
    head, fr, e = split(label)
    save(label, head, fr + [amp(e[0], amps, dur)])


def waves_electricity():
    head, fr, e = split('waves-electricity')
    out = fr
    for i, el in enumerate(e[:3]):
        out.append(group([el], keyed('transform', bump(i, 3, '0 0', '0.6 0'), 3, kind='translate')))
    out.append(fade(e[3], [(0, '1'), (0.8, '1'), (0.85, '0.3'), (0.9, '1'), (1, '1')], 3, ease=False))
    save('waves-electricity', head, out)


def wheelchair():
    head, fr, e = split('wheelchair')
    save('wheelchair', head, fr + e + [hub(8, 16, 2, 4), hub(19, 19, 0.8, 2)])


def wifi():
    head, fr, e = split('wifi')
    out = fr + [e[0]]
    for i, el in enumerate(e[1:]):
        out.append(fade(el, bump(i, 3, '1', '0.25'), 3))
    save('wifi', head, out)


def world():
    head, fr, e = split('world')

    def meridian(x0, lon):
        rx = 9 * abs(math.sin(math.radians(lon)))
        sweep = 0 if lon < 0 else 1
        return f'M{fmt(x0)} 3a{fmt(max(rx, 0.01))} 9 0 0 {sweep} 0 18'

    base = (-16.7, 16.7)
    steps = 16
    sway = [14 * math.sin(2 * math.pi * k / steps) for k in range(steps)]
    out = fr + e[:3]
    for el, x0, lon in ((e[3], 11.5, base[0]), (e[4], 12.5, base[1])):
        vals = [d_of(el)] + [meridian(x0, lon + s) for s in sway[1:]]
        out.append(with_anim(el, osc('d', vals, 4, ease=False)))
    save('world', head, out)


def world_off():
    head, fr, e = split('world-off')
    save('world-off', head, fr + e[:5] + [erase(e[5], 3)])


def world_upload():
    head, fr, e = split('world-upload')
    save('world-upload', head, fr + e[:5] + [shift([e[5]], 0, -1, 1.6)])


def writing():
    head, fr, e = split('writing')
    pen = group(e[:2], keyed('transform', [(0, '0 0'), (0.25, '-0.5 -0.6'), (0.5, '0 0'), (0.75, '-0.3 -0.4'), (1, '0 0')], 1.8, kind='translate'))
    save('writing', head, fr + [e[2], pen])


def x():
    head, fr, e = split('x')
    save('x', head, fr + [rock(e, 12, 12, 12, 3)])


def yoga():
    head, fr, e = split('yoga')
    rest = [(4, 10), (8, 9), (12, 8), (16, 9.5), (20, 11)]
    up = [(4, 8.6), (8, 8.3), (12, 8), (16, 8.7), (20, 9.4)]
    dd = lambda pts: 'M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in pts)
    arms = with_anim(e[2], osc('d', [dd(rest), dd(up)], 4))
    arms = arms.replace(d_of(e[2]), dd(rest), 1)
    save('yoga', head, fr + [e[0], e[1], e[3], arms])


def zoom_in():
    head, fr, e = split('zoom-in')
    save('zoom-in', head, fr + [e[3], scale_at(e[:3], 10, 10, ['1', '1.08'], 2.4)])


def zoom_out():
    head, fr, e = split('zoom-out')
    save('zoom-out', head, fr + [e[2], scale_at(e[:2], 10, 10, ['1', '0.92'], 2.4)])


ICONS = {
    'terminal': terminal, 'test-pipe': test_pipe, 'thumb-down': thumb_down, 'thumb-up': thumb_up,
    'ticket': ticket, 'ticket-off': ticket_off, 'toilet-paper': toilet_paper, 'tool': tool, 'tools': tools,
    'tools-kitchen': tools_kitchen, 'tools-kitchen-3': tools_kitchen_3, 'torii': torii, 'tractor': tractor,
    'traffic-lights': traffic_lights, 'transit-bus': transit_bus, 'trash': trash, 'trash-x': trash_x,
    'trees': trees, 'trekking': trekking, 'triangle': triangle, 'trophy': trophy, 'truck': truck,
    'truck-delivery': truck_delivery, 'typography': typography, 'underline': underline, 'unlink': unlink,
    'upload': upload, 'user': user, 'user-heart': user_heart, 'user-minus': user_minus, 'user-plus': user_plus,
    'users-group': users_group, 'vaccine': vaccine, 'vaccine-bottle': vaccine_bottle, 'vector': vector,
    'video': video, 'vinyl': vinyl, 'volume': volume, 'volume-off': volume_off, 'walk': walk, 'wand': wand,
    'wash-hand': wash_hand, 'wash-machine': wash_machine,
    'wave-saw-tool': lambda: wave('wave-saw-tool', [1, -1], 3),
    'wave-sine': lambda: wave('wave-sine', [1, -1], 3),
    'wave-square': lambda: wave('wave-square', [1, -1], 3),
    'waves-electricity': waves_electricity, 'wheelchair': wheelchair, 'wifi': wifi, 'world': world,
    'world-off': world_off, 'world-upload': world_upload, 'writing': writing, 'x': x, 'yoga': yoga,
    'zoom-in': zoom_in, 'zoom-out': zoom_out,
}

if __name__ == '__main__':
    want = sys.argv[1:] or open(os.path.join(os.path.dirname(__file__), '../../lists/08.txt')).read().split()
    for l in want:
        ICONS[l]()
    print('wrote', len(want))

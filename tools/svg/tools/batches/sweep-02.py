import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# ---- local helpers ----

def attach(el, *anims):
    """[el] with [anims] as children."""
    tag = re.match(r'<([a-z]+)', el).group(1)
    inner = '\n'.join('    ' + a for a in anims)
    return el[:-2].rstrip() + '>\n' + inner + f'\n</{tag}>'


def d_of(el):
    return re.search(r'd="([^"]+)"', el).group(1)


def path(d, *anims):
    el = f'<path d="{d}"/>'
    return attach(el, *anims) if anims else el


def rock(children, cx, cy, degs, dur, ease=True):
    """Rotates [children] about (cx, cy) through [degs], back to 0."""
    vals = [f'{fmt(a)} {fmt(cx)} {fmt(cy)}' for a in [0] + list(degs)]
    return group(children, osc('transform', vals, dur, kind='rotate', ease=ease))


def scale_at(children, cx, cy, scales, dur, ease=True):
    """Scales [children] about (cx, cy) through [scales] ('sx sy' or 's'), back to 1."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], osc('transform', ['1'] + list(scales), dur, kind='scale', ease=ease))
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def move(children, offsets, dur, ease=True):
    """Translates [children] through [offsets] (dx, dy), back to 0 0."""
    vals = ['0 0'] + [f'{fmt(x)} {fmt(y)}' for x, y in offsets]
    return group(children, osc('transform', vals, dur, kind='translate', ease=ease))


def fade(el, values, dur):
    """[el] with its opacity running through [values], starting at 1."""
    return attach(el, osc('opacity', ['1'] + [fmt(v) for v in values], dur))


def draw(el, dur, hold=0.4, erase=0.15):
    """[el] erased from its end, then drawn again from its start."""
    el = el.replace('<path ', '<path pathLength="1" stroke-dasharray="1 1" ', 1)
    a = (f'<animate attributeName="stroke-dashoffset" values="0;0;1;0" '
         f'keyTimes="0;{fmt(hold)};{fmt(hold + erase)};1" calcMode="spline" '
         f'keySplines="{splines(3)}" dur="{dur}s" repeatCount="indefinite"/>')
    return attach(el, a)


def spoke(cx, cy, dur, r=0.8):
    return group([f'<path d="M{fmt(cx - r)} {fmt(cy)}h{fmt(2 * r)}"/>'], spin(cx, cy, dur))


def wave(n, amp, phase=0.0):
    """n+1 samples of a sine wave starting at 0 (last equals first)."""
    return [amp * math.sin(2 * math.pi * (k / n) + phase) - amp * math.sin(phase) for k in range(n + 1)]


def linear(attr, vals, dur):
    return f'<animate attributeName="{attr}" values="{";".join(vals)}" dur="{dur}s" repeatCount="indefinite"/>'


def frame_and(els):
    return [e for e in els if is_frame(e)], [e for e in els if not is_frame(e)]


def flicker(children, cx, cy, dur=1.6):
    """A flame's flicker about its base (cx, cy)."""
    return scale_at(children, cx, cy, ['0.9 1.1', '1.06 0.94', '0.95 1.06'], dur)


def bob_vehicle(label, wheels, body_idx, wheel_idx, dur=1.2, lift=-0.5):
    head, els = load(label)
    body = [els[0]] + [els[i] for i in wheel_idx]
    body += [spoke(cx, cy, 1.5) for cx, cy in wheels]
    body += [move([els[i] for i in body_idx], [(0, lift)], dur)]
    save(label, head, body)


# ---- icons ----

def brain():
    # the halves breathe apart and together
    head, els = load('brain')
    right = [els[1], els[3], els[4]]
    left = [els[2], els[5], els[6]]
    save('brain', head, [els[0], rock(right, 12, 20, [3], 3), rock(left, 12, 20, [-3], 3)])


def bread():
    # the loaf rises
    head, els = load('bread')
    save('bread', head, [els[0], scale_at(els[1:], 12, 20, ['1.03 1.07'], 3)])


def briefcase():
    # swings gently from its handle
    head, els = load('briefcase')
    save('briefcase', head, [els[0], rock(els[1:], 12, 3, [4, -4], 3)])


def broadcast():
    # the waves ripple outward
    head, els = load('broadcast')
    inner = fade(els[2], [0.25, 1, 1], 2)
    outer = fade(els[1], [1, 0.25, 1], 2)
    save('broadcast', head, [els[0], outer, inner, els[3]])


def brush():
    # the handle sways on the bristles
    head, els = load('brush')
    save('brush', head, [els[0], els[1], rock(els[2:], 9.5, 14.5, [5, -3], 3)])


def bucket():
    # rocks on its base
    head, els = load('bucket')
    save('bucket', head, [els[0], rock(els[1:], 12, 21, [4, -4], 3)])


def building():
    # windows light and dim in turn
    head, els = load('building')
    wins = els[2:8]
    order = [0, 4, 2, 5, 1, 3]
    n = len(wins)
    out = []
    for i, w in enumerate(wins):
        vals = ['1'] * (n + 1)
        vals[order[i] + 1] = '0.15'
        vals.append('1')
        out.append(attach(w, osc('opacity', vals, 6)))
    save('building', head, [els[0], els[1], els[8]] + out)


def building_arch():
    # the arch of the door rises and settles
    head, els = load('building-arch')
    save('building-arch', head, els[:3] + [scale_at([els[3]], 12, 21, ['1 1.12'], 3)])


def building_bank():
    # the columns stretch in turn
    head, els = load('building-bank')
    cols = []
    for i, x in enumerate((8, 12, 16)):
        vals = ['M%d 14l0 3' % x] * 4
        vals[i + 1] = 'M%d 13l0 5' % x
        cols.append(path(vals[0], osc('d', vals, 4)))
    save('building-bank', head, els[:6] + cols)


def building_bridge():
    # the suspension cable bounces with its hanger
    head, els = load('building-bridge')
    cable = ['M3 8a7.5 7.5 0 0 0 3 -2a6.5 6.5 0 0 0 12 0a7.5 7.5 0 0 0 3 2',
             'M3 8a7.5 7.5 0 0 0 3 -2a6.1 6.1 0 0 0 12 0a7.5 7.5 0 0 0 3 2']
    hanger = ['M12 10l0 5', 'M12 11l0 4']
    save('building-bridge', head, els[:4] + [path(cable[0], osc('d', cable, 2.5)), path(hanger[0], osc('d', hanger, 2.5))])


def building_carousel():
    # the seats rise and fall like carousel horses
    head, els = load('building-carousel')
    seats = els[2:7]
    out = []
    for i, s in enumerate(seats):
        offs = [(0, 0)] * 5
        offs[i] = (0, -1)
        out.append(move([s], offs, 5))
    save('building-carousel', head, [els[0], els[1]] + out + [els[7]])


def building_castle():
    # a pennant flutters above the keep
    head, els = load('building-castle')
    pole = '<path d="M12 5v-3.5"/>'
    flag = ['M12 1.5l2.5 1l-2.5 1', 'M12 1.5l2 1.3l-2 .7', 'M12 1.5l2.4 .7l-2.4 1.3']
    save('building-castle', head, els + [pole, path(flag[0], osc('d', flag, 1.6))])


def building_church():
    # the cross sways like a ringing bell
    head, els = load('building-church')
    save('building-church', head, els[:3] + [els[5], rock(els[3:5], 12, 8, [8, -8], 3)])


def building_circus():
    # the flag flutters
    head, els = load('building-circus')
    flag = ['M12 7v-4l2 1h-2', 'M12 7v-4l1.6 1.4h-1.6', 'M12 7v-4l2 .6h-2']
    save('building-circus', head, els[:6] + [path(flag[0], osc('d', flag, 1.6))])


def building_community():
    # windows blink in turn
    head, els = load('building-community')
    out = []
    for i, w in enumerate(els[2:6]):
        vals = ['1'] * 5
        vals[i + 1] = '0.1'
        out.append(attach(w, osc('opacity', vals + ['1'], 4)))
    save('building-community', head, els[:2] + out)


def building_cottage():
    # the round window glows
    head, els = load('building-cottage')
    save('building-cottage', head, els[:3] + [els[4], scale_at([els[3]], 12, 9, ['1.18'], 3)])


def building_hospital():
    # the cross beats
    head, els = load('building-hospital')
    save('building-hospital', head, els[:4] + [scale_at(els[4:6], 12, 9, ['1.2', '1', '1.12'], 2)])


def building_lighthouse():
    # the beams sweep from side to side
    head, els = load('building-lighthouse')
    left = move([fade(els[3], [0.15, 1, 1], 3)], [(-0.6, 0), (0, 0), (0, 0)], 3)
    right = move([fade(els[4], [1, 1, 0.15], 3)], [(0, 0), (0, 0), (0.6, 0)], 3)
    save('building-lighthouse', head, els[:3] + [left, right])


def building_monument():
    # the obelisk stands tall
    head, els = load('building-monument')
    save('building-monument', head, [els[0], els[2], els[3], scale_at([els[1]], 12, 18, ['1 1.06'], 3)])


def building_mosque():
    # the crescent rocks, the minaret tops bob
    head, els = load('building-mosque')
    crescent = rock(els[8:10], 12, 5, [10, -10], 4)
    tops = [move([els[6]], [(0, -0.5), (0, 0)], 4), move([els[7]], [(0, 0), (0, -0.5)], 4)]
    save('building-mosque', head, els[:6] + tops + [crescent])


def building_stadium():
    # the flags flap
    head, els = load('building-stadium')
    out = []
    for i, x in enumerate((15, 7)):
        a = f'M{x} 6h4v-3h-4v7'
        b = f'M{x} 6h3.2v-3h-3.2v7'
        vals = [a, b, a, a] if i == 0 else [a, a, b, a]
        out.append(path(a, osc('d', vals, 2)))
    save('building-stadium', head, els[:3] + out)


def building_store():
    # gentle pulse
    head, els = load('building-store')
    save('building-store', head, [els[0], pulse(els[1:], cy=12, amount=0.06, dur=3)])


def building_warehouse():
    # gentle pulse
    head, els = load('building-warehouse')
    save('building-warehouse', head, [els[0], pulse(els[1:], cy=12.5, amount=0.06, dur=3)])


def bulb():
    # the rays glow
    head, els = load('bulb')
    rays = scale_at([fade(els[1], [0.3], 2)], 12, 11, ['1.08'], 2)
    save('bulb', head, [els[0], rays, els[2], els[3]])


def burger():
    # the top bun lifts
    head, els = load('burger')
    save('burger', head, [els[0], els[1], move([els[2]], [(0, -1)], 2), move([els[3]], [(0, -0.4)], 2)])


def bus_stop():
    # the bus idles at the stop
    head, els = load('bus-stop')
    body = [els[3], els[4], els[5], els[6], els[7]]
    save('bus-stop', head, [els[0], els[1], els[8], els[2], spoke(18, 17, 1.5), move(body, [(0, -0.5)], 1.2)])


def cactus():
    # the arms wave
    head, els = load('cactus')
    left = rock([els[1]], 10, 13, [8, -4], 3)
    right = rock([els[2]], 14, 16, [-4, 8], 3)
    save('cactus', head, [els[0], els[3], els[4], left, right])


def cake():
    # the candle flame flickers
    head, els = load('cake')
    save('cake', head, els[:3] + [flicker([els[3]], 12, 8)])


def calendar_check():
    # the check draws itself
    head, els = load('calendar-check')
    save('calendar-check', head, els[:5] + [draw(els[5], 3)])


def calendar_minus():
    # the minus breathes
    head, els = load('calendar-minus')
    vals = ['M16 19h6', 'M17 19h4']
    save('calendar-minus', head, els[:5] + [path(vals[0], osc('d', vals, 2))])


def calendar_pause():
    # the bars step in turn
    head, els = load('calendar-pause')
    a = path('M17 17v5', osc('d', ['M17 17v5', 'M17 18v3', 'M17 17v5', 'M17 17v5'], 2))
    b = path('M21 17v5', osc('d', ['M21 17v5', 'M21 17v5', 'M21 18v3', 'M21 17v5'], 2))
    save('calendar-pause', head, els[:5] + [a, b])


def calendar_plus():
    # the plus swells
    head, els = load('calendar-plus')
    save('calendar-plus', head, els[:5] + [scale_at(els[5:7], 19, 19, ['1.25'], 2)])


def calendar_question():
    # the question mark tilts
    head, els = load('calendar-question')
    save('calendar-question', head, els[:5] + [rock(els[5:7], 19, 19, [12, -12], 3)])


def calendar_repeat():
    # the repeat arrow turns
    head, els = load('calendar-repeat')
    save('calendar-repeat', head, els[:5] + [group(els[5:8], spin(19, 19, 4))])


def calendar_x():
    # the x shakes its head
    head, els = load('calendar-x')
    save('calendar-x', head, els[:5] + [rock(els[5:7], 19.5, 19.5, [12, -12, 8, 0, 0], 3)])


def camera():
    # the lens snaps
    head, els = load('camera')
    lens = scale_at([els[2]], 12, 13, ['1', '0.7', '1'], 3)
    save('camera', head, [els[0], els[1], lens])


def camera_selfie():
    # the smile widens
    head, els = load('camera-selfie')
    vals = ['M9.5 15a3.5 3.5 0 0 0 5 0', 'M9 14.6a4.2 4.2 0 0 0 6 0']
    save('camera-selfie', head, [els[0], els[1], path(vals[0], osc('d', vals, 3)), els[3], els[4]])


def campfire():
    # the flame flickers
    head, els = load('campfire')
    save('campfire', head, els[:3] + [flicker([els[3]], 12, 15, 1.8)])


def candle():
    # the flame flickers
    head, els = load('candle')
    save('candle', head, els[:2] + [flicker([els[2]], 12, 6.5)])


def car():
    # the body bobs, hubs turn
    bob_vehicle('car', [(7, 17), (17, 17)], [3], [1, 2])


def car_suv():
    bob_vehicle('car-suv', [(7, 17), (18, 17)], [3, 4, 5, 6, 7], [1, 2])


def caravan():
    head, els = load('caravan')
    save('caravan', head, [els[0], els[1], spoke(9, 18, 1.5), move(els[2:6], [(0, -0.5)], 1.2)])


def cards():
    # the top card fans out
    head, els = load('cards')
    save('cards', head, [els[0], els[2], els[3], rock([els[1]], 9, 20, [-7], 3)])


def caret_left():
    head, els = load('caret-left')
    save('caret-left', head, [els[0], move([els[1]], [(-1, 0)], 2)])


def caret_up():
    head, els = load('caret-up')
    save('caret-up', head, [els[0], move([els[1]], [(0, -1)], 2)])


def carousel_horizontal():
    # the center slide shifts between its neighbors
    head, els = load('carousel-horizontal')
    save('carousel-horizontal', head, [els[0], move([els[1]], [(-1, 0), (0, 0), (1, 0)], 4), els[2], els[3]])


def carrot():
    # the greens sway
    head, els = load('carrot')
    save('carrot', head, els[:4] + [rock(els[4:6], 16, 8, [6, -6], 3)])


def cash():
    # the coin flips
    head, els = load('cash')
    save('cash', head, els[:3] + [scale_at([els[3]], 14, 14, ['0.15 1'], 3)])


def certificate():
    # the seal and ribbon sway
    head, els = load('certificate')
    save('certificate', head, [els[0]] + els[3:7] + [rock(els[1:3], 15, 15, [6, -6], 3)])


def chalkboard():
    # the eraser slides along the tray
    head, els = load('chalkboard')
    save('chalkboard', head, [els[0], els[1], move([els[2]], [(-3, 0)], 4)])


def chart():
    # the series rise and fall
    head, els = load('chart')
    area = [(4, 19), (8, 13), (12, 15), (16, 10), (20, 14), (20, 19), (4, 19)]
    line = [(4, 12), (7, 8), (11, 10), (16, 4), (20, 8)]
    n = 8
    def frames(pts, amps, phase):
        out = []
        for k in range(n + 1):
            ps = [(x, y + a * math.sin(2 * math.pi * k / n + phase + i)) for i, ((x, y), a) in enumerate(zip(pts, amps))]
            out.append('M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in ps))
        return out
    def zeroed(pts, amps, phase):
        # offsets relative to frame 0 so frame 0 is the static geometry
        out = []
        for k in range(n + 1):
            ps = [(x, y + a * (math.sin(2 * math.pi * k / n + phase + i) - math.sin(phase + i))) for i, ((x, y), a) in enumerate(zip(pts, amps))]
            out.append('M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in ps))
        return out
    a = zeroed(area, [0, 1, 1, 1, 1, 0, 0], 0)
    l = zeroed(line, [0, 1, 1, 1, 1], 0.5)
    save('chart', head, [els[0], path(a[0], linear('d', a, 4)), path(l[0], linear('d', l, 4))])


def chart_bar():
    # the bars grow and shrink in turn
    head, els = load('chart-bar')
    def bar(x, top):
        h = 19 - (top + 1)
        return f'M{x} {fmt(top + 1)}a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v{fmt(h)}a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -{fmt(h)}'
    bars = [(3, [12, 10, 12, 13]), (15, [8, 8, 6, 10]), (9, [4, 6, 4, 5])]
    out = [path(bar(x, ts[0]), osc('d', [bar(x, t) for t in ts], 4)) for x, ts in bars]
    save('chart-bar', head, [els[0]] + out + [els[4]])


def chart_dots():
    # the points drift, their links follow
    head, els = load('chart-dots')
    pts = [(9, 9), (19, 7), (14, 15)]
    amps = [1, 0.8, -1]
    n = 8
    def pos(i, k):
        x, y = pts[i]
        return x, y + amps[i] * math.sin(2 * math.pi * k / n + i)  - amps[i] * math.sin(i)
    def seg(p, q):
        dx, dy = q[0] - p[0], q[1] - p[1]
        L = math.hypot(dx, dy)
        ux, uy = dx / L, dy / L
        a = (p[0] + 2 * ux, p[1] + 2 * uy)
        b = (q[0] - 2 * ux, q[1] - 2 * uy)
        return f'M{fmt(a[0])} {fmt(a[1])}L{fmt(b[0])} {fmt(b[1])}'
    dots = []
    for i in range(3):
        offs = [f'0 {fmt(pos(i, k)[1] - pts[i][1])}' for k in range(n + 1)]
        dots.append(group([els[2 + i]], f'<animateTransform attributeName="transform" type="translate" values="{";".join(offs)}" dur="4s" repeatCount="indefinite"/>'))
    l1 = [seg(pos(0, k), pos(2, k)) for k in range(n + 1)]
    l2 = [seg(pos(2, k), pos(1, k)) for k in range(n + 1)]
    save('chart-dots', head, [els[0], els[1]] + dots + [path(l1[0], linear('d', l1, 4)), path(l2[0], linear('d', l2, 4))])


def chart_line():
    # the line rises and falls
    head, els = load('chart-line')
    line = [(4, 15), (8, 9), (12, 11), (16, 6), (20, 10)]
    n = 8
    out = []
    for k in range(n + 1):
        ps = [(x, y + (0 if i == 0 else math.sin(2 * math.pi * k / n + i) - math.sin(i))) for i, (x, y) in enumerate(line)]
        out.append('M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in ps))
    save('chart-line', head, [els[0], els[1], path(out[0], linear('d', out, 4))])


def check():
    head, els = load('check')
    save('check', head, [els[0], draw(els[1], 3)])


def chef_hat():
    # the hat puffs up
    head, els = load('chef-hat')
    save('chef-hat', head, [els[0], scale_at(els[1:], 12, 21, ['1.03 1.06'], 3)])


def chevron(label, dx, dy):
    head, els = load(label)
    save(label, head, [els[0], move([els[1]], [(dx, dy)], 2)])


def circle_check():
    head, els = load('circle-check')
    save('circle-check', head, [els[0], els[1], draw(els[2], 3)])


def circles():
    # the circles hop in turn
    head, els = load('circles')
    out = []
    for i, c in enumerate(els[1:4]):
        offs = [(0, 0)] * 3
        offs[i] = (0, -1)
        out.append(move([c], offs, 3))
    save('circles', head, [els[0]] + out)


ALL = {
    'brain': brain, 'bread': bread, 'briefcase': briefcase, 'broadcast': broadcast, 'brush': brush,
    'bucket': bucket, 'building': building, 'building-arch': building_arch, 'building-bank': building_bank,
    'building-bridge': building_bridge, 'building-carousel': building_carousel, 'building-castle': building_castle,
    'building-church': building_church, 'building-circus': building_circus, 'building-community': building_community,
    'building-cottage': building_cottage, 'building-hospital': building_hospital,
    'building-lighthouse': building_lighthouse, 'building-monument': building_monument,
    'building-mosque': building_mosque, 'building-stadium': building_stadium, 'building-store': building_store,
    'building-warehouse': building_warehouse, 'bulb': bulb, 'burger': burger, 'bus-stop': bus_stop,
    'cactus': cactus, 'cake': cake, 'calendar-check': calendar_check, 'calendar-minus': calendar_minus,
    'calendar-pause': calendar_pause, 'calendar-plus': calendar_plus, 'calendar-question': calendar_question,
    'calendar-repeat': calendar_repeat, 'calendar-x': calendar_x, 'camera': camera, 'camera-selfie': camera_selfie,
    'campfire': campfire, 'candle': candle, 'car': car, 'car-suv': car_suv, 'caravan': caravan, 'cards': cards,
    'caret-left': caret_left, 'caret-up': caret_up, 'carousel-horizontal': carousel_horizontal, 'carrot': carrot,
    'cash': cash, 'certificate': certificate, 'chalkboard': chalkboard, 'chart': chart, 'chart-bar': chart_bar,
    'chart-dots': chart_dots, 'chart-line': chart_line, 'check': check, 'chef-hat': chef_hat,
    'chevron-down': lambda: chevron('chevron-down', 0, 1), 'chevron-left': lambda: chevron('chevron-left', -1, 0),
    'chevron-right': lambda: chevron('chevron-right', 1, 0), 'chevron-up': lambda: chevron('chevron-up', 0, -1),
    'circle-check': circle_check, 'circles': circles,
}

if __name__ == '__main__':
    only = sys.argv[1:]
    for label, f in ALL.items():
        if not only or label in only:
            f()
    print('wrote', len(only) or len(ALL))

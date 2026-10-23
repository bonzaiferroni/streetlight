import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# local helpers

def P(d, extra=''):
    return f'<path d="{d}"{extra}/>'


def d_of(el):
    return re.search(r' d="([^"]+)"', el).group(1)


def anim(attr, values, times, dur, kind=None, ease=True):
    """An animation through [values] at [times], looping."""
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"' + (f' type="{kind}"' if kind else '')
    a += f' values="{";".join(values)}" keyTimes="{";".join(fmt(t) for t in times)}" dur="{dur}s" repeatCount="indefinite"'
    if ease:
        a += f' calcMode="spline" keySplines="{splines(len(values) - 1)}"'
    return a + '/>'


def win(attr, rest, peaks, dur, a=0.0, b=1.0, kind=None):
    """Rests at [rest], passes through [peaks] between fractions [a] and [b] of the loop, rests again."""
    vals, ts = [rest], [0.0]
    if a > 0:
        vals.append(rest); ts.append(a)
    for i, p in enumerate(peaks):
        vals.append(p); ts.append(a + (b - a) * (i + 1) / (len(peaks) + 1))
    vals.append(rest); ts.append(b)
    if b < 1:
        vals.append(rest); ts.append(1.0)
    return anim(attr, vals, ts, dur, kind)


def rot(angles, cx, cy):
    return [f'{fmt(a)} {fmt(cx)} {fmt(cy)}' for a in angles]


def about(children, cx, cy, *anims):
    """Children scaled by [anims] (scale animateTransforms) about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], *anims)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def spoke(cx, cy, dur, r=0.8, reverse=False):
    return group([P(f'M{fmt(cx - r)} {fmt(cy)}h{fmt(2 * r)}')], spin(cx, cy, dur, reverse=reverse))


def redraw(el, dur, a=0.0, b=1.0, hold=0.3):
    """[el] erases from its start, then writes itself again from its start, between [a] and [b]."""
    d = d_of(el)
    mid = a + (b - a) * hold
    vals, ts = ['0'], [0.0]
    if a > 0:
        vals.append('0'); ts.append(a)
    vals += ['-1', '-2']; ts += [mid, b]
    if b < 1:
        vals.append('-2'); ts.append(1.0)
    an = anim('stroke-dashoffset', vals, ts, dur)
    return f'<path d="{d}" pathLength="1" stroke-dasharray="1 1">\n    {an}\n</path>'


def morph(d_values, dur, times=None):
    if times is None:
        return f'<path d="{d_values[0]}">\n    {osc("d", d_values, dur)}\n</path>'
    return f'<path d="{d_values[0]}">\n    {anim("d", d_values, times, dur)}\n</path>'


def clip(label, x, y, w, h):
    cid = f'{label}-clip'
    return cid, f'<clipPath id="{cid}"><rect x="{fmt(x)}" y="{fmt(y)}" width="{fmt(w)}" height="{fmt(h)}"/></clipPath>'


def split(els):
    """The frame path and the rest."""
    return [e for e in els if is_frame(e)], [e for e in els if not is_frame(e)]


WAVE_SEG = 'a2.4 2.4 0 0 0 2 1a2.4 2.4 0 0 0 2 -1a2.4 2.4 0 0 1 2 -1a2.4 2.4 0 0 1 2 1'


def water(label, dur=4):
    """Boat water: the wave line of sailboat/ship, travelling left one period per loop."""
    cid, cp = clip(label, 1.5, 17, 21, 6)
    d = 'M-6 20' + WAVE_SEG * 5
    g = group([P(d)], f'<animateTransform attributeName="transform" type="translate" from="0 0" to="-8 0" dur="{dur}s" repeatCount="indefinite"/>')
    return [cp, f'<g clip-path="url(#{cid})">\n' + '\n'.join('    ' + ln for ln in g.split('\n')) + '\n</g>']


S = {}  # label -> builder


def icon(fn):
    S[fn.__name__.replace('_', '-')] = fn
    return fn


@icon
def recycle(head, els):
    # each arrow pushes along its way in turn
    f, r = split(els)
    pairs = [(r[0:2], (-1, 0)), (r[2:4], (0.5, -0.866)), (r[4:6], (0.5, 0.866))]
    body = f[:]
    for i, (ps, (dx, dy)) in enumerate(pairs):
        a = i / 3
        body.append(group(ps, win('transform', '0 0', [f'{fmt(0.8 * dx)} {fmt(0.8 * dy)}'], 3, a, a + 1 / 3, kind='translate')))
    return body


def half_turn(els, dur=3, deg=180, cx=12, cy=12, keep=()):
    f, r = split(els)
    moving = [e for i, e in enumerate(r) if i not in keep]
    still = [e for i, e in enumerate(r) if i in keep]
    an = anim('transform', rot([0, deg, deg], cx, cy), [0, 0.75, 1], dur, kind='rotate')
    return f + still + [group(moving, an)]


@icon
def refresh(head, els):
    # the arrows turn half way round, which looks the same
    return half_turn(els)


@icon
def refresh_dot(head, els):
    return half_turn(els, keep=(2,))


@icon
def reload(head, els):
    return half_turn(els, deg=360)


@icon
def repeat(head, els):
    # top arrow pushes right, then bottom arrow pushes left
    f, r = split(els)
    return f + [group([r[0]], win('transform', '0 0', ['0.8 0'], 3, 0, 0.5, kind='translate')),
                group([r[1]], win('transform', '0 0', ['-0.8 0'], 3, 0.5, 1, kind='translate'))]


@icon
def restore(head, els):
    # the arrow winds back a full turn
    return half_turn(els, deg=-360, keep=(2,))


@icon
def ripple(head, els):
    # each wave flips in turn
    f, r = split(els)
    body = f[:]
    for i, e in enumerate(r):
        d = d_of(e)
        inv = re.sub(r'c3 -2 6 -2 9 0s6 2 9 0', 'c3 2 6 2 9 0s6 -2 9 0', d)
        a = [0, 0.15, 0.3][[0, 2, 1][i]]
        body.append(f'<path d="{d}">\n    {win("d", d, [inv], 3, a, a + 0.7)}\n</path>')
    return body


@icon
def road(head, els):
    # the centre line runs towards us
    f, r = split(els)
    cid, cp = clip('road', 0, 5, 24, 14)
    dashes = [P('M12 3v-2')] + r[2:]
    g = group(dashes, '<animateTransform attributeName="transform" type="translate" from="0 0" to="0 5" dur="2s" repeatCount="indefinite"/>')
    return f + r[:2] + [cp, f'<g clip-path="url(#{cid})">\n' + '\n'.join('    ' + ln for ln in g.split('\n')) + '\n</g>']


@icon
def roller_skating(head, els):
    # the boot bobs over turning wheels
    f, r = split(els)
    return f + r[1:3] + [spoke(8, 17, 1.5), spoke(16, 17, 1.5), shift([r[0]], 0, -0.5, 1.5)]


@icon
def rotate_clockwise(head, els):
    return half_turn(els, deg=360)


@icon
def ruler(head, els):
    # the ticks reach out one after another
    f, r = split(els)
    body = f + [r[0]]
    for i, e in enumerate(r[1:]):
        d = d_of(e)
        m = re.match(r'M(\S+) (\S+)l', d)
        long = f'M{m.group(1)} {m.group(2)}l-3 -3'
        a = i * 0.15
        body.append(f'<path d="{d}">\n    {win("d", d, [long], 3, a, a + 0.4)}\n</path>')
    return body


@icon
def ruler_measure(head, els):
    # the measure stretches between its stops
    f, r = split(els)
    body = f + r[:6]
    body.append(group([r[6]], win('transform', '0 0', ['-0.8 0'], 3, kind='translate')))
    body.append(morph(['M3 5h18', 'M2.2 5h19.6'], 3))
    body.append(group([r[8]], win('transform', '0 0', ['0.8 0'], 3, kind='translate')))
    return body


@icon
def run(head, els):
    # the runner bounds along
    f, r = split(els)
    return f + [shift(r, 0, -0.6, 1)]


@icon
def sailboat(head, els):
    # the boat rocks on moving water
    f, r = split(els)
    boat = group(r[1:], osc('transform', rot([0, -4, 0, 4], 12, 18), 4, kind='rotate'))
    return f + water('sailboat') + [boat]


@icon
def satellite(head, els):
    # the signal goes out, the satellite turns a little
    f, r = split(els)
    sat = group(r[:4], osc('transform', rot([0, -4, 0, 3], 9, 9), 4, kind='rotate'))
    sig1 = group([r[4]], win('opacity', '1', ['0.15'], 2, 0, 0.5))
    sig2 = group([r[5]], win('opacity', '1', ['0.15'], 2, 0.2, 0.7))
    return f + [sat, sig1, sig2]


@icon
def scale(head, els):
    # the beam tips and the pans follow
    f, r = split(els)
    ang = [0, -5, 0, 5]
    dy = [0, 0.52, 0, -0.52]
    beam = group([r[1]], osc('transform', rot(ang, 12, 5), 4, kind='rotate'))
    left = group([r[3]], osc('transform', [f'0 {fmt(v)}' for v in dy], 4, kind='translate'))
    right = group([r[4]], osc('transform', [f'0 {fmt(-v)}' for v in dy], 4, kind='translate'))
    return f + [r[0], r[2], beam, left, right]


@icon
def scan(head, els):
    # the scan line sweeps up and down
    f, r = split(els)
    return f + r[1:] + [group([r[0]], osc('transform', ['0 0', '0 -4.5', '0 0', '0 4.5'], 4, kind='translate'))]


@icon
def school(head, els):
    # the tassel swings
    f, r = split(els)
    cap = P('M22 9l-10 -4l-10 4l10 4l10 -4')
    tassel = group([P('M22 9v6')], osc('transform', rot([0, 8, 0, -8], 22, 9), 3, kind='rotate'))
    return f + [cap, r[1], tassel]


@icon
def scissors(head, els):
    # two snips, then a rest
    f, r = split(els)
    ts = [0, 0.15, 0.3, 0.45, 0.6, 1]
    a = anim('transform', rot([0, -9, 0, -9, 0, 0], 12, 12), ts, 2.5, kind='rotate')
    b = anim('transform', rot([0, 9, 0, 9, 0, 0], 12, 12), ts, 2.5, kind='rotate')
    return f + [group([r[0], r[2]], a), group([r[1], r[3]], b)]


@icon
def scooter(head, els):
    f, r = split(els)
    return f + r[:2] + [spoke(18, 17, 1.5), spoke(6, 17, 1.5), shift([r[2]], 0, -0.4, 1.5)]


@icon
def scoreboard(head, els):
    # the colon blinks
    f, r = split(els)
    colon = group(r[2:4], anim('opacity', ['1', '1', '0.1', '0.1', '1'], [0, 0.45, 0.5, 0.95, 1], 2, ease=False))
    return f + r[:2] + r[4:] + [colon]


@icon
def search(head, els):
    # the glass circles about, searching
    f, r = split(els)
    g = group(r, '<animateMotion path="M0 0a1 1 0 1 1 0 -2a1 1 0 1 1 0 2" dur="3s" repeatCount="indefinite"/>')
    return f + [g]


@icon
def seedling(head, els):
    # the leaves sway against each other
    f, r = split(els)
    left = group([r[0]], osc('transform', rot([0, -7, 0, 4], 12, 10), 3, kind='rotate'))
    right = group([r[1]], osc('transform', rot([0, 7, 0, -4], 12, 14), 3, kind='rotate'))
    return f + [r[2], left, right]


@icon
def send(head, els):
    # the plane lifts away and returns
    f, r = split(els)
    return f + [shift(r, 0.8, -0.8, 2)]


@icon
def separator(head, els):
    # the line breathes between its dots
    f, r = split(els)
    return f + [r[0], r[2], morph(['M7 12l10 0', 'M9.5 12l5 0'], 3)]


@icon
def share(head, els):
    # each node pulses in turn, left to right
    f, r = split(els)
    body = f + r[3:]
    for e, (cx, cy), a in zip(r[:3], [(6, 12), (18, 6), (18, 18)], [0, 0.3, 0.45]):
        body.append(about([e], cx, cy, win('transform', '1', ['0.8'], 2.5, a, a + 0.35, kind='scale')))
    return body


@icon
def shield(head, els):
    # no feature: pulse
    f, r = split(els)
    return f + [pulse(r, 12, 12, 0.07, 3)]


@icon
def shield_check(head, els):
    # the check writes itself again
    f, r = split(els)
    return f + [r[0], redraw(r[1], 3, 0, 0.7)]


@icon
def shield_heart(head, els):
    # the heart beats
    f, r = split(els)
    beat = win('transform', '1', ['1.15', '1', '1.15'], 2, 0, 0.5, kind='scale')
    return f + [r[0], about([r[1]], 18, 18.5, beat)]


@icon
def shield_plus(head, els):
    # the plus turns a quarter
    f, r = split(els)
    g = group(r[1:], anim('transform', rot([0, 90, 90], 19, 19), [0, 0.5, 1], 3, kind='rotate'))
    return f + [r[0], g]


@icon
def ship(head, els):
    f, r = split(els)
    boat = group(r[1:], osc('transform', rot([0, -3, 0, 3], 12, 17), 4, kind='rotate'))
    return f + water('ship') + [boat]


@icon
def shirt(head, els):
    # sways on its hanger
    f, r = split(els)
    return f + [group(r, osc('transform', rot([0, 4, 0, -4], 12, 4), 4, kind='rotate'))]


@icon
def shoe(head, els):
    # the toe taps twice
    f, r = split(els)
    a = anim('transform', rot([0, -5, 0, -5, 0, 0], 4, 18), [0, 0.15, 0.3, 0.45, 0.6, 1], 2.5, kind='rotate')
    return f + [group(r, a)]


@icon
def shooting_star(head, els):
    # the star streaks on, its trails stretch
    f, r = split(els)
    star = shift([r[0]], 0.8, 0.8, 2)
    body = f + [star]
    for e in r[1:]:
        d = d_of(e)
        m = re.match(r'M(\S+) (\S+)l(\S+) (\S+)', d)
        x, y, dx, dy = (float(v) for v in m.groups())
        body.append(morph([d, f'M{fmt(x)} {fmt(y)}l{fmt(dx + 0.8)} {fmt(dy + 0.8)}'], 2))
    return body


@icon
def shopping_bag(head, els):
    # swings from its handle
    f, r = split(els)
    return f + [group(r, osc('transform', rot([0, 4, 0, -4], 12, 3), 3, kind='rotate'))]


@icon
def shopping_cart(head, els):
    f, r = split(els)
    return f + r[:2] + [spoke(6, 19, 1.5), spoke(17, 19, 1.5), shift(r[2:], 0, -0.4, 1.5)]


@icon
def sign_out(head, els):
    # the arrow steps out
    f, r = split(els)
    return f + [r[0], shift(r[1:], -1, 0, 2)]


@icon
def signature(head, els):
    # the signature writes itself
    f, r = split(els)
    d = d_of(r[0])
    an = anim('stroke-dashoffset', ['0', '0', '-1', '-2'], [0, 0.2, 0.35, 1], 4)
    return f + [f'<path d="{d}" pathLength="1" stroke-dasharray="1 1">\n    {an}\n</path>']


@icon
def skateboard(head, els):
    f, r = split(els)
    return f + r[:2] + [spoke(7, 15, 1.5), spoke(17, 15, 1.5), shift([r[2]], 0, -0.4, 1.5)]


@icon
def ski_jumping(head, els):
    # the jumper floats
    f, r = split(els)
    return f + [shift(r, 0, -0.7, 3)]


@icon
def skull(head, els):
    # the eyes blink
    f, r = split(els)
    blink = lambda: win('transform', '1 1', ['1 0.1'], 3, 0.8, 0.9, kind='scale')
    return f + r[:3] + [about([r[3]], 9, 11, blink()), about([r[4]], 15, 11, blink())]


@icon
def snowflake(head, els):
    # turns slowly
    f, r = split(els)
    return f + [group(r, spin(12, 12, 12))]


@icon
def social(head, els):
    # the front head nods, the one behind steps aside
    # (the large file strokes white by inline style; masks only read alpha, so it is dropped here)
    f, r = split(els)
    r = [re.sub(r' style="[^"]*"', '', e) for e in r]
    return f + [r[1], shift([r[0]], 0, -0.5, 3), group(r[2:], win('transform', '0 0', ['0.6 0'], 3, 0.3, 0.9, kind='translate'))]


@icon
def soup(head, els):
    # the steam rises and fades, one wisp after another
    f, r = split(els)
    body = f + [r[0]]
    for e, a in zip([r[3], r[1], r[2]], [0, 0.2, 0.4]):
        t = [0, a, a + 0.45, a + 0.4501, a + 0.6, 1]
        tr = anim('transform', ['0 0', '0 0', '0 -1.5', '0 0', '0 0', '0 0'], t, 3, kind='translate', ease=False)
        op = anim('opacity', ['1', '1', '0', '0', '1', '1'], t, 3, ease=False)
        body.append(group([e], tr, op))
    return body


@icon
def spacing_vertical(head, els):
    # the space opens and closes
    f, r = split(els)
    return f + [r[2], shift([r[0]], 0, 0.8, 3), shift([r[1]], 0, -0.8, 3)]


@icon
def speakerphone(head, els):
    # the sound goes out
    f, r = split(els)
    wave = group([r[0]], osc('transform', ['0 0', '0.8 0'], 1.5, kind='translate'), osc('opacity', ['1', '0.3'], 1.5))
    return f + r[1:] + [wave]


@icon
def spray(head, els):
    # the droplets flicker
    f, r = split(els)
    body = f + r[:2] + [r[8]]
    for i, e in enumerate(r[2:8]):
        a = (i * 0.11) % 0.5
        t = [0, a, a + 0.3, a + 0.3001, a + 0.45, 1]
        tr = anim('transform', ['0 0', '0 0', '1 0', '0 0', '0 0', '0 0'], t, 2, kind='translate', ease=False)
        op = anim('opacity', ['1', '1', '0', '0', '1', '1'], t, 2, ease=False)
        body.append(group([e], tr, op))
    return body


@icon
def stairs_up(head, els):
    f, r = split(els)
    return f + [r[0], shift(r[1:], 0, -0.8, 2)]


@icon
def star(head, els):
    # twinkles
    f, r = split(els)
    return f + [pulse(r, 12, 12.3, -0.1, 3)]


@icon
def star_off(head, els):
    # the slash strikes again
    f, r = split(els)
    return f + [r[1], redraw(r[0], 3, 0, 0.6)]


@icon
def steering_wheel(head, els):
    f, r = split(els)
    return f + [group(r, osc('transform', rot([0, -25, 0, 25], 12, 12), 4, kind='rotate'))]


@icon
def stethoscope(head, els):
    # the chest piece hears a heartbeat
    f, r = split(els)
    beat = win('transform', '1', ['1.2', '1', '1.2'], 2, 0, 0.5, kind='scale')
    return f + r[:4] + [about([r[4]], 20, 10, beat)]


@icon
def storm(head, els):
    f, r = split(els)
    return f + [group(r, spin(12, 12, 6))]


@icon
def stretching(head, els):
    # reaches further, the head follows
    f, r = split(els)
    d0 = 'M18 20v-5h-5.5l2.5 -6.5l-5.5 1l1.5 2'
    d1 = 'M18 20v-5h-5.5l2.5 -6.5l-6.2 .6l1.6 2.2'
    return f + [r[1], morph([d0, d1], 3), shift([r[0]], -0.4, 0.2, 3)]


@icon
def sun(head, els):
    # the rays turn
    f, r = split(els)
    return f + [r[0], group(r[1:], spin(12, 12, 16))]


@icon
def sunglasses(head, els):
    # a glint passes over each lens
    f, r = split(els)
    return f + r[:5] + [group([r[5]], win('opacity', '1', ['0'], 3, 0, 0.3)), group([r[6]], win('opacity', '1', ['0'], 3, 0.2, 0.5))]


@icon
def swimming(head, els):
    # the swimmer bobs on the water
    f, r = split(els)
    return f + [shift(r[:2], 0, 0.5, 2), shift([r[2]], 0, -0.3, 2)]


@icon
def table(head, els):
    # the inner lines draw again
    f, r = split(els)
    return f + [r[0], redraw(r[1], 3, 0, 0.5), redraw(r[2], 3, 0.3, 0.8)]


@icon
def tag(head, els):
    # swings on its hole
    f, r = split(els)
    return f + [group(r, osc('transform', rot([0, 6, 0, -6], 7.5, 7.5), 3, kind='rotate'))]


@icon
def target(head, els):
    # a ripple from the bull's-eye outwards
    f, r = split(els)
    return f + [about([r[0]], 12, 12, win('transform', '1', ['1.4'], 2.4, 0, 0.35, kind='scale')),
                about([r[1]], 12, 12, win('transform', '1', ['1.08'], 2.4, 0.15, 0.5, kind='scale')),
                about([r[2]], 12, 12, win('transform', '1', ['1.04'], 2.4, 0.3, 0.65, kind='scale'))]


@icon
def telescope(head, els):
    # the tube tilts up and down
    f, r = split(els)
    return f + r[:2] + [group(r[2:], osc('transform', rot([0, -4, 0, 3], 12, 13), 4, kind='rotate'))]


@icon
def tent(head, els):
    # the door flaps
    f, r = split(els)
    d0 = 'M11 14l4 6h6l-9 -16l-9 16h6l4 -6'
    d1 = 'M11.6 14.4l3.4 5.6h6l-9 -16l-9 16h6l4.6 -5.6'
    return f + [morph([d0, d1], 2)]


if __name__ == '__main__':
    labels = sys.argv[1:] or [l.strip() for l in open('/home/starfox/projects/streetlight/tools/svg/lists/07.txt') if l.strip()]
    for l in labels:
        head, els = load(l)
        save(l, head, S[l](head, els))
    print('wrote', len(labels))

"""Sweep batch 04: eye-plus .. horse-toy."""
import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# ---- local helpers ----

def kf(attr, frames, dur, kind=None, ease=True, extra=''):
    """An animation through (time fraction, value) [frames], looping. Times run 0..1."""
    ts = ';'.join(fmt(t) for t, _ in frames)
    vs = ';'.join(v for _, v in frames)
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"'
    if kind:
        a += f' type="{kind}"'
    a += f' values="{vs}" keyTimes="{ts}" dur="{fmt(dur)}s" repeatCount="indefinite"'
    if ease:
        a += f' calcMode="spline" keySplines="{splines(len(frames) - 1)}"'
    return a + extra + '/>'


def about(children, cx, cy, anim):
    """Applies a scale [anim] to [children] about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], anim)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def rock(children, cx, cy, deg, dur):
    """Rocks [children] about (cx, cy) by +-[deg]."""
    c = f'{fmt(cx)} {fmt(cy)}'
    return group(children, osc('transform', [f'0 {c}', f'{fmt(deg)} {c}', f'0 {c}', f'{fmt(-deg)} {c}'], dur, kind='rotate'))


def beat(children, cx, cy, s=1.1, dur=1.6, start=0.0):
    """A lub-dub heartbeat of [children] about (cx, cy), starting at fraction [start]."""
    w = 0.12
    fr = [(0, '1')]
    t = start
    if t > 0:
        fr.append((t, '1'))
    fr += [(t + w, f'{fmt(s)}'), (t + 2 * w, '1'), (t + 3 * w, f'{fmt(1 + (s - 1) * 0.7)}'), (t + 4 * w, '1')]
    if fr[-1][0] < 1:
        fr.append((1, '1'))
    return about(children, cx, cy, kf('transform', fr, dur, kind='scale'))


def hop(children, dx, dy, dur, start, width=0.25):
    """Moves [children] by (dx, dy) and back within the window [start, start+width] of the loop."""
    fr = [(0, '0 0')]
    if start > 0:
        fr.append((start, '0 0'))
    fr += [(start + width / 2, f'{fmt(dx)} {fmt(dy)}'), (start + width, '0 0')]
    if fr[-1][0] < 1:
        fr.append((1, '0 0'))
    return group(children, kf('transform', fr, dur, kind='translate'))


def retrace(d, dur, hold=0.3, extra_attrs=''):
    """A path that holds, slides away along itself, then draws back in from its start."""
    a = kf('stroke-dashoffset', [(0, '0'), (hold, '0'), (hold + (1 - hold) / 2, '-1'), (1, '-2')], dur, ease=False)
    return f'<path d="{d}" pathLength="1" stroke-dasharray="1 1"{extra_attrs}>\n    {a}\n</path>'


def rising(x0, y0, dy, dur, start, width=0.5, r=0.5, dx=0.0):
    """A small dot that appears at (x0, y0), rises by [dy] while fading, inside the window from [start]."""
    end = start + width
    tr = [(0, '0 0'), (start, '0 0'), (end, f'{fmt(dx)} {fmt(-dy)}'), (1, f'{fmt(dx)} {fmt(-dy)}')]
    op = [(0, '0'), (start, '0'), (start + width * 0.2, '1'), (end, '0'), (1, '0')]
    if start == 0:
        tr = tr[:1] + tr[2:]
        op = op[:1] + op[2:]
    return (f'<circle cx="{fmt(x0)}" cy="{fmt(y0)}" r="{fmt(r)}" fill="currentColor" stroke="none" opacity="0">\n'
            f'    {kf("transform", tr, dur, kind="translate", ease=False)}\n'
            f'    {kf("opacity", op, dur, ease=False)}\n</circle>')


def path(d):
    return f'<path d="{d}"/>'


def split(els):
    frame = [e for e in els if is_frame(e)]
    rest = [e for e in els if not is_frame(e)]
    return frame, rest


def hubs(centers, y, dur=1.5):
    return [group([f'<path d="M{fmt(cx - 0.8)} {fmt(y)}h1.6"/>'], spin(cx, y, dur)) for cx in centers]


def blink(children, cy, dur=4, at=0.8, cx=12):
    """Closes [children] vertically about y=[cy] briefly near fraction [at]."""
    fr = [(0, '1 1'), (at, '1 1'), (at + 0.05, '1 0.1'), (at + 0.1, '1 1'), (1, '1 1')]
    return about(children, cx, cy, kf('transform', fr, dur, kind='scale'))


# ---- icons ----

def eye_plus():
    head, els = load('eye-plus')
    f, r = split(els)
    save('eye-plus', head, f + [blink(r[0:2], 12)] + [about(r[2:4], 19, 19, kf('transform', [(0, '1'), (0.3, '1'), (0.45, '1.25'), (0.6, '1'), (1, '1')], 4, kind='scale'))])


def eyeglass():
    head, els = load('eyeglass')
    f, r = split(els)
    save('eyeglass', head, f + [rock(r, 12, 16, 3, 3)])


def feather():
    head, els = load('feather')
    f, r = split(els)
    save('feather', head, f + [rock(r, 4, 20, 6, 3)])


def file():
    head, els = load('file')
    f, r = split(els)
    save('file', head, f + [shift(r, 0, -0.7, 3)])


def file_cv():
    head, els = load('file-cv')
    f, r = split(els)
    save('file-cv', head, f + r[:2] + [hop([r[2]], 0, -0.8, 2.4, 0.1, 0.3), hop([r[3]], 0, -0.8, 2.4, 0.4, 0.3)])


def file_export():
    head, els = load('file-export')
    f, r = split(els)
    outline = path('M11.5 21h-4.5a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2h7l5 5v5')
    arrow = path('M14 19h7m-3 -3l3 3l-3 3')
    save('file-export', head, f + [r[0], outline, shift([arrow], 1, 0, 2)])


def file_import():
    head, els = load('file-import')
    f, r = split(els)
    outline = path('M5 13v-8a2 2 0 0 1 2 -2h7l5 5v11a2 2 0 0 1 -2 2h-5.5')
    arrow = path('M2 19h7m-3 -3l3 3l-3 3')
    save('file-import', head, f + [r[0], outline, shift([arrow], 1, 0, 2)])


def file_music():
    head, els = load('file-music')
    f, r = split(els)
    note = group(r[2:4], kf('transform', [(0, '0 0'), (0.25, '0 -0.8'), (0.5, '0 0'), (1, '0 0')], 2, kind='translate'))
    save('file-music', head, f + r[:2] + [rock([note], 12, 16, 5, 2)])


def file_text():
    head, els = load('file-text')
    f, r = split(els)
    lines = []
    for el, (x, y, w), start in zip(r[2:], [(9, 9, 1), (9, 13, 6), (9, 17, 6)], (0.0, 0.25, 0.5)):
        short = max(0.01, w * 0.3)
        vals = [(0, f'M{x} {y}l{w} 0')]
        if start > 0:
            vals.append((start, f'M{x} {y}l{w} 0'))
        vals += [(start + 0.15, f'M{x} {y}l{fmt(short)} 0'), (start + 0.4, f'M{x} {y}l{w} 0')]
        vals.append((1, f'M{x} {y}l{w} 0'))
        lines.append(f'<path d="M{x} {y}l{w} 0">\n    {kf("d", vals, 4)}\n</path>')
    save('file-text', head, f + r[:2] + lines)


def file_type_pdf():
    head, els = load('file-type-pdf')
    f, r = split(els)
    p, fbar, fstem, d = r[2], r[3], r[4], r[5]
    save('file-type-pdf', head, f + r[:2] + [
        hop([p], 0, -0.8, 3, 0.05, 0.25), hop([d], 0, -0.8, 3, 0.3, 0.25), hop([fbar, fstem], 0, -0.8, 3, 0.55, 0.25)])


def filter_():
    head, els = load('filter')
    f, r = split(els)
    drop = rising(12, 21.3, -2, 2.4, 0.2, 0.6, r=0.75)
    save('filter', head, f + r + [drop])


def fire():
    head, els = load('fire')
    f, r = split(els)
    fr = [(0, '1 1'), (0.25, '1.03 0.94'), (0.5, '0.98 1.05'), (0.75, '1.02 0.97'), (1, '1 1')]
    save('fire', head, f + [about(r, 12, 20.7, kf('transform', fr, 1.6, kind='scale'))])


def firetruck():
    head, els = load('firetruck')
    f, r = split(els)
    ladder = r[5]
    c = '3 9'
    lift = group([ladder], kf('transform', [(0, f'0 {c}'), (0.4, f'-6 {c}'), (0.6, f'-6 {c}'), (1, f'0 {c}')], 3, kind='rotate'))
    save('firetruck', head, f + r[:5] + [lift, r[6]] + hubs([5, 17], 17))


def first_aid_kit():
    head, els = load('first-aid-kit')
    f, r = split(els)
    save('first-aid-kit', head, f + r[:2] + [beat(r[2:4], 12, 14, 1.25, 1.8)])


def fish():
    head, els = load('fish')
    f, r = split(els)
    swim = rock(r, 17, 12, 5, 2)
    save('fish', head, f + [shift([swim], -0.6, 0, 4)])


def flag():
    head, els = load('flag')
    f, r = split(els)

    def wave(u):
        u %= 14
        if u < 7:
            return -(math.sqrt(25 - (u - 3.5) ** 2) - math.sqrt(25 - 3.5 ** 2))
        return math.sqrt(25 - (u - 10.5) ** 2) - math.sqrt(25 - 3.5 ** 2)

    xs = [5 + i * 0.5 for i in range(29)]
    frames, poles = [], []
    steps = 24
    for k in range(steps + 1):
        ph = 14 * k / steps
        top = [(x, 5 + wave(x - 5 - ph)) for x in xs]
        bot = [(x, 14 + wave(x - 5 - ph)) for x in reversed(xs)]
        pts = top + bot
        d = 'M' + 'L'.join(f'{fmt(x)} {fmt(y)}' for x, y in pts) + 'Z'
        frames.append((k / steps, d))
        poles.append((k / steps, f'M5 21V{fmt(bot[-1][1])}'))
    body = f'<path d="{frames[0][1]}">\n    {kf("d", frames, 3, ease=False)}\n</path>'
    pole = f'<path d="{poles[0][1]}">\n    {kf("d", poles, 3, ease=False)}\n</path>'
    save('flag', head, f + [body, pole])


def flask():
    head, els = load('flask')
    f, r = split(els)
    bubbles = [rising(11, 18.5, 7, 3, 0.0, 0.6, r=0.8), rising(13.2, 18, 6, 3, 0.4, 0.55, r=0.65)]
    save('flask', head, f + r + bubbles)


def flip_horizontal():
    head, els = load('flip-horizontal')
    f, r = split(els)
    fr = [(0, '1 1'), (0.3, '1 1'), (0.5, '-1 1'), (0.8, '-1 1'), (1, '1 1')]
    save('flip-horizontal', head, f + [r[0], about(r[1:], 12, 12, kf('transform', fr, 4, kind='scale'))])


def flower():
    head, els = load('flower')
    f, r = split(els)
    save('flower', head, f + [r[0], group([r[1]], spin(12, 12, 6, deg=60))])


def focus():
    head, els = load('focus')
    f, r = split(els)
    dirs = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    ticks = [shift([e], 0.8 * dx, 0.8 * dy, 2) for e, (dx, dy) in zip(r[2:], dirs)]
    save('focus', head, f + r[:2] + ticks)


def forms():
    head, els = load('forms')
    f, r = split(els)
    d1 = group([r[5]], kf('opacity', [(0, '1'), (0.4, '1'), (0.5, '0'), (0.9, '0'), (1, '1')], 2.4, ease=False))
    d2 = group([r[4]], kf('opacity', [(0, '1'), (0.5, '1'), (0.6, '0'), (0.9, '0'), (1, '1')], 2.4, ease=False))
    cursor = group(r[:2], kf('opacity', [(0, '1'), (0.75, '1'), (0.85, '0.3'), (0.95, '1'), (1, '1')], 2.4))
    save('forms', head, f + [cursor] + r[2:4] + [d1, d2])


def frame():
    head, els = load('frame')
    f, r = split(els)
    dirs = [(0, -0.7), (0, 0.7), (-0.7, 0), (0.7, 0)]
    save('frame', head, f + [shift([e], dx, dy, 2.5) for e, (dx, dy) in zip(r, dirs)])


def frame_eye():
    head, els = load('frame-eye')
    f, r = split(els)
    fr = lambda a, b: [(0, a), (0.75, a), (0.8, b), (0.85, a), (1, a)]
    top = f'<path d="M7 12q 5 -7 10 0">\n    {kf("d", fr("M7 12q5 -7 10 0", "M7 12q5 0 10 0"), 4)}\n</path>'
    bot = f'<path d="M7 12q 5 7 10 0">\n    {kf("d", fr("M7 12q5 7 10 0", "M7 12q5 0 10 0"), 4)}\n</path>'
    pupil = group([r[2]], kf('opacity', fr('1', '0'), 4))
    save('frame-eye', head, f + [top, bot, pupil] + r[3:])


def fridge():
    head, els = load('fridge')
    f, r = split(els)
    save('fridge', head, f + r[:2] + [hop([r[3]], 0.6, 0, 3, 0.1, 0.25), hop([r[2]], 0.6, 0, 3, 0.45, 0.25)])


def friends():
    head, els = load('friends')
    f, r = split(els)
    a = group(r[:2], kf('transform', [(0, '0 0'), (0.25, '0 -0.7'), (0.5, '0 0'), (1, '0 0')], 2, kind='translate'))
    b = group(r[2:], kf('transform', [(0, '0 0'), (0.5, '0 0'), (0.75, '0 -0.7'), (1, '0 0')], 2, kind='translate'))
    save('friends', head, f + [a, b])


def gavel():
    head, els = load('gavel')
    f, r = split(els)
    c = '19.5 19.5'
    strike = kf('transform', [(0, f'0 {c}'), (0.6, f'14 {c}'), (0.75, f'0 {c}'), (1, f'0 {c}')], 2, kind='rotate')
    save('gavel', head, f + [r[3], group([r[0], r[1], r[2], r[4]], strike)])


def gear():
    head, els = load('gear')
    f, r = split(els)
    save('gear', head, f + [group(r, spin(12, 12, 8))])


def gender_female():
    head, els = load('gender-female')
    f, r = split(els)
    save('gender-female', head, f + [rock(r, 12, 9, 6, 3)])


def gender_male():
    head, els = load('gender-male')
    f, r = split(els)
    save('gender-male', head, f + [r[0], shift(r[1:], 0.8, -0.8, 2)])


def ghost():
    head, els = load('ghost')
    f, r = split(els)
    save('ghost', head, f + [shift([rock(r, 12, 12, 3, 4)], 0, -0.9, 2)])


def gift():
    head, els = load('gift')
    f, r = split(els)
    lid = group([r[0], r[3]], kf('transform', [(0, '0 0'), (0.5, '0 0'), (0.6, '0 -0.9'), (0.7, '0 0'), (0.8, '0 -0.6'), (0.9, '0 0'), (1, '0 0')], 3, kind='translate'))
    save('gift', head, f + [r[1], r[2], lid])


def git_merge():
    head, els = load('git-merge')
    f, r = split(els)
    dot = (f'<circle r="0.9" fill="currentColor" stroke="none" opacity="0">\n'
           f'    <animateMotion path="M7 8a4 4 0 0 0 4 4h4" dur="2.4s" repeatCount="indefinite" keyPoints="0;0;1;1" keyTimes="0;0.2;0.8;1" calcMode="linear"/>\n'
           f'    {kf("opacity", [(0, "0"), (0.2, "0"), (0.3, "1"), (0.7, "1"), (0.8, "0"), (1, "0")], 2.4, ease=False)}\n</circle>')
    save('git-merge', head, f + r + [dot])


def github():
    head, els = load('github')
    f, r = split(els)
    tail = path('M9 19c-4.3 1.4 -4.3 -2.5 -6 -3')
    rest = path(re.search(r'd="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5(.*)"', r[0]).group(1).join(['M15 21', '']))
    save('github', head, f + [rest, rock([tail], 9, 19, 10, 2)])


def glass():
    head, els = load('glass')
    f, r = split(els)
    save('glass', head, f + [rock(r, 12, 21, 4, 3)])


def glass_champagne():
    head, els = load('glass-champagne')
    f, r = split(els)
    bubbles = [rising(11.2, 13.5, 6, 2.4, 0.0, 0.6, r=0.6), rising(12.7, 12.5, 5, 2.4, 0.45, 0.5, r=0.5)]
    save('glass-champagne', head, f + r + bubbles)


def glass_cocktail():
    head, els = load('glass-cocktail')
    f, r = split(els)
    save('glass-cocktail', head, f + [rock(r, 12, 21, 4, 3)])


def glass_full():
    head, els = load('glass-full')
    f, r = split(els)
    a = 'M6 10c1.778 -1.333 4.222 -1.333 6 0c1.778 1.333 4.222 1.333 6 0'
    b = 'M6 10c1.778 1.333 4.222 1.333 6 0c1.778 -1.333 4.222 -1.333 6 0'
    surf = f'<path d="{a}">\n    {osc("d", [a, b], 3)}\n</path>'
    save('glass-full', head, f + r[:3] + [surf])


def golf():
    head, els = load('golf')
    f, r = split(els)
    vals = ['M12 18v-15l7 4l-7 4', 'M12 18v-15l6.6 4.8l-6.6 3.2', 'M12 18v-15l7 3.4l-7 4.6']
    fl = f'<path d="{vals[0]}">\n    {osc("d", vals, 2)}\n</path>'
    save('golf', head, f + [fl, r[1]])


def grave():
    head, els = load('grave')
    f, r = split(els)
    save('grave', head, f + [r[0], r[3], beat(r[1:3], 12, 9.5, 1.15, 3)])


def grill():
    head, els = load('grill')
    f, r = split(els)
    wisps = []
    for el, start in zip(r[5:8], (0.0, 0.3, 0.6)):
        tr = [(0, '0 0'), (start, '0 0'), (start + 0.3, '0 -1.5'), (start + 0.3001, '0 0'), (1, '0 0')] if start else \
             [(0, '0 0'), (0.3, '0 -1.5'), (0.3001, '0 0'), (1, '0 0')]
        op = [(0, '1'), (start, '1'), (start + 0.3, '0'), (start + 0.4, '1'), (1, '1')] if start else \
             [(0, '1'), (0.3, '0'), (0.4, '1'), (1, '1')]
        wisps.append(group([el], kf('transform', tr, 3, kind='translate', ease=False), kf('opacity', op, 3, ease=False)))
    save('grill', head, f + r[:5] + wisps)


def grip_vertical():
    head, els = load('grip-vertical')
    f, r = split(els)
    dots = []
    for i, el in enumerate(r):
        start = (i % 3) * 0.15 + (i // 3) * 0.08
        dots.append(hop([el], 0, -0.6, 2, start, 0.3))
    save('grip-vertical', head, f + dots)


def guitar_pick():
    head, els = load('guitar-pick')
    f, r = split(els)
    save('guitar-pick', head, f + [rock(r, 12, 12, 8, 2)])


def hammer():
    head, els = load('hammer')
    f, r = split(els)
    c = '5.5 18.9'
    strike = kf('transform', [(0, f'0 {c}'), (0.6, f'-14 {c}'), (0.75, f'0 {c}'), (1, f'0 {c}')], 2, kind='rotate')
    save('hammer', head, f + [group(r, strike)])


def hand_stop():
    head, els = load('hand-stop')
    f, r = split(els)
    save('hand-stop', head, f + [rock(r, 12, 21, 6, 2)])


def hanger():
    head, els = load('hanger')
    f, r = split(els)
    save('hanger', head, f + [rock(r, 12, 4, 6, 3)])


def heading():
    head, els = load('heading')
    f, r = split(els)
    bar = f'<path d="M7 12h10">\n    {osc("d", ["M7 12h10", "M7 11h10", "M7 12h10", "M7 13h10"], 3)}\n</path>'
    save('heading', head, f + [bar] + r[1:])


def headphones():
    head, els = load('headphones')
    f, r = split(els)
    left = beat([r[0]], 6.5, 16.5, 1.1, 1.6, 0.0)
    right = beat([r[1]], 17.5, 16.5, 1.1, 1.6, 0.4)
    save('headphones', head, f + [r[2], left, right])


def heart():
    head, els = load('heart')
    f, r = split(els)
    save('heart', head, f + [beat(r, 12, 13, 1.1, 1.6)])


def heart_broken():
    head, els = load('heart-broken')
    f, r = split(els)
    crack = retrace('M12 6l-2 4l4 3l-2 4v3', 3, 0.4)
    save('heart-broken', head, f + [r[0], crack])


def heart_dollar():
    head, els = load('heart-dollar')
    f, r = split(els)
    fr = [(0, '1 1'), (0.4, '1 1'), (0.55, '0.15 1'), (0.7, '1 1'), (1, '1 1')]
    save('heart-dollar', head, f + [r[0], about(r[1:], 19, 18, kf('transform', fr, 3, kind='scale'))])


def heart_handshake():
    head, els = load('heart-handshake')
    f, r = split(els)
    save('heart-handshake', head, f + [beat(r, 12, 13, 1.08, 1.6)])


def heart_off():
    head, els = load('heart-off')
    f, r = split(els)
    save('heart-off', head, f + [retrace('M3 3l18 18', 3, 0.4), r[1]])


def heart_plus():
    head, els = load('heart-plus')
    f, r = split(els)
    save('heart-plus', head, f + [beat([r[0]], 12, 13, 1.1, 1.6), about(r[1:], 19, 19, kf('transform', [(0, '1'), (0.55, '1'), (0.7, '1.25'), (0.85, '1'), (1, '1')], 1.6, kind='scale'))])


def heartbeat():
    head, els = load('heartbeat')
    f, r = split(els)
    save('heartbeat', head, f + [r[0], retrace('M3 13h2l2 3l2 -6l1 3h3', 2, 0.3)])


def hearts():
    head, els = load('hearts')
    f, r = split(els)
    save('hearts', head, f + [beat([r[0]], 11, 12, 1.08, 1.6, 0.0), beat([r[1]], 16.2, 15.5, 1.15, 1.6, 0.4)])


def helicopter():
    head, els = load('helicopter')
    f, r = split(els)
    rotor = about([r[3]], 12.5, 6, kf('transform', [(0, '1 1'), (0.5, '-1 1'), (1, '1 1')], 0.75, kind='scale', ease=False))
    body = r[:3] + [rotor] + r[4:]
    save('helicopter', head, f + [shift(body, 0, -0.6, 3)])


def home():
    head, els = load('home')
    f, r = split(els)
    fr = [(0, '1 1'), (0.4, '1 1'), (0.55, '0.35 1'), (0.75, '0.35 1'), (0.9, '1 1'), (1, '1 1')]
    save('home', head, f + r[:2] + [about([r[2]], 9, 17, kf('transform', fr, 4, kind='scale'))])


def home_exclamation():
    head, els = load('home-exclamation')
    f, r = split(els)
    save('home-exclamation', head, f + r[:2] + [hop(r[2:], 0, -0.8, 2, 0.1, 0.3)])


def home_heart():
    head, els = load('home-heart')
    f, r = split(els)
    save('home-heart', head, f + r[:2] + [beat([r[2]], 19, 18.8, 1.15, 1.6)])


def home_shield():
    head, els = load('home-shield')
    f, r = split(els)
    save('home-shield', head, f + r[:3] + [about([r[3]], 18.5, 18.2, osc('transform', ['1', '1.12'], 2.4, kind='scale'))])


def horse():
    head, els = load('horse')
    f, r = split(els)
    tail = rock([r[1]], 19, 9, 10, 1.2)
    save('horse', head, f + [shift([r[0], tail], 0, -0.5, 1.2)])


def horse_toy():
    head, els = load('horse-toy')
    f, r = split(els)
    save('horse-toy', head, f + [rock(r, 12, 20, 6, 3)])


ALL = [eye_plus, eyeglass, feather, file, file_cv, file_export, file_import, file_music, file_text, file_type_pdf,
       filter_, fire, firetruck, first_aid_kit, fish, flag, flask, flip_horizontal, flower, focus, forms, frame,
       frame_eye, fridge, friends, gavel, gear, gender_female, gender_male, ghost, gift, git_merge, github, glass,
       glass_champagne, glass_cocktail, glass_full, golf, grave, grill, grip_vertical, guitar_pick, hammer, hand_stop,
       hanger, heading, headphones, heart, heart_broken, heart_dollar, heart_handshake, heart_off, heart_plus,
       heartbeat, hearts, helicopter, home, home_exclamation, home_heart, home_shield, horse, horse_toy]

if __name__ == '__main__':
    only = set(sys.argv[1:])
    for fn in ALL:
        if not only or fn.__name__ in only:
            fn()
    print(len(ALL), 'functions')

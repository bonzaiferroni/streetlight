import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# local helpers

def anim(attr, vals, kts, dur, kind=None, spline=True, extra=''):
    """An animation through [vals] at keyTimes [kts]; eased unless [spline] is off."""
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"' + (f' type="{kind}"' if kind else '')
    a += f' values="{";".join(vals)}" keyTimes="{";".join(fmt(k) for k in kts)}" dur="{dur}s" repeatCount="indefinite"'
    if spline:
        a += f' calcMode="spline" keySplines="{splines(len(vals) - 1)}"'
    return a + extra + '/>'


def window(attr, rest, peak, dur, start, end, kind=None):
    """Rests at [rest], swings to [peak] and back between [start] and [end] (fractions of [dur])."""
    mid = (start + end) / 2
    vals, kts = [rest], [0]
    if start > 0:
        vals.append(rest); kts.append(start)
    vals.append(peak); kts.append(mid)
    vals.append(rest); kts.append(end)
    if end < 1:
        vals.append(rest); kts.append(1)
    return anim(attr, vals, kts, dur, kind)


def lin(attr, frm, to, dur, kind=None):
    """A linear run from [frm] to [to], restarting each loop."""
    tag = 'animateTransform' if kind else 'animate'
    return (f'<{tag} attributeName="{attr}"' + (f' type="{kind}"' if kind else '') +
            f' from="{frm}" to="{to}" dur="{dur}s" repeatCount="indefinite"/>')


def about(children, cx, cy, *anims):
    """Wraps [children] so scale/rotate-free [anims] apply about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], *anims)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def rock(children, cx, cy, deg, dur, start=0, end=1):
    """Rotates [children] about (cx, cy) by +deg then -deg and back."""
    c = f' {fmt(cx)} {fmt(cy)}'
    if start == 0 and end == 1:
        return group(children, osc('transform', ['0' + c, fmt(deg) + c, '0' + c, fmt(-deg) + c], dur, kind='rotate'))
    return group(children, window('transform', '0' + c, fmt(deg) + c, dur, start, end, kind='rotate'))


def tilt(children, cx, cy, deg, dur, start=0, end=1):
    """Rotates [children] about (cx, cy) to [deg] and back."""
    c = f' {fmt(cx)} {fmt(cy)}'
    return group(children, window('transform', '0' + c, fmt(deg) + c, dur, start, end, kind='rotate'))


def scale_win(children, cx, cy, s, dur, start=0, end=1):
    """Scales [children] about (cx, cy) to [s] (number or 'sx sy') and back within a window."""
    peak = s if isinstance(s, str) else fmt(s)
    rest = '1 1' if isinstance(s, str) else '1'
    return about(children, cx, cy, window('transform', rest, peak, 1 if False else dur, start, end, kind='scale'))


def move_win(children, dx, dy, dur, start=0, end=1):
    """Moves [children] by (dx, dy) and back within a window."""
    return group(children, window('transform', '0 0', f'{fmt(dx)} {fmt(dy)}', dur, start, end, kind='translate'))


def fade_win(children, low, dur, start=0, end=1):
    """Fades [children] to [low] opacity and back within a window."""
    return group(children, window('opacity', '1', fmt(low), dur, start, end))


def with_anim(el, *anims):
    """Puts [anims] inside the element [el]."""
    tag = re.match(r'<([a-z]+)', el).group(1)
    inner = '\n'.join('    ' + a for a in anims)
    return el[:-2] + '>\n' + inner + f'\n</{tag}>'


def redraw(el, length, dur, start=0.3, end=1):
    """Undraws and redraws the path [el] of [length] between [start] and [end]."""
    L = fmt(length)
    el = el.replace('<path ', f'<path stroke-dasharray="{L} {L}" ', 1)
    a, b = start, (start + end) / 2
    vals, kts = ['0'], [0]
    if a > 0:
        vals.append('0'); kts.append(a)
    vals += [L, L, '0']; kts += [b, b + 0.05 * (end - start), end]
    if end < 1:
        vals.append('0'); kts.append(1)
    return with_anim(el, anim('stroke-dashoffset', vals, kts, dur))


def ps(d, extra=''):
    return f'<path d="{d}"{extra}/>'


W = {}


def icon(f):
    W[f.__name__.replace('_', '-')] = f
    return f


@icon
def city():
    # windows light up one after another
    head, els = load('city')
    dots = els[4:8]
    body = els[:4] + [fade_win([d], 0.15, 3, i * 0.2, i * 0.2 + 0.35) for i, d in enumerate(dots)]
    return head, body


@icon
def clef():
    # sways to the beat
    head, els = load('clef')
    return head, [els[0], rock(els[1:], 12, 20, 5, 3)]


@icon
def click():
    # the pointer taps, the rays burst out
    head, els = load('click')
    rays = els[1:6]
    dirs = [(-1, 0), (0, -1), (-0.707, -0.707), (0.707, -0.707), (-0.707, 0.707)]
    body = [els[0]]
    body += [move_win([r], 0.8 * dx, 0.8 * dy, 2, 0.15, 0.6) for r, (dx, dy) in zip(rays, dirs)]
    body.append(move_win([els[6]], -0.6, -0.6, 2, 0, 0.3))
    return head, body


@icon
def clipboard():
    # the clip lifts and settles
    head, els = load('clipboard')
    return head, els[:2] + [move_win([els[2]], 0, -0.8, 2.5)]


@icon
def clipboard_check():
    # the check is drawn again
    head, els = load('clipboard-check')
    return head, els[:3] + [redraw(els[3], 8.5, 3)]


@icon
def clipboard_list():
    # each row is ticked in turn
    head, els = load('clipboard-list')
    rows = [[els[3], els[4]], [els[5], els[6]]]
    body = els[:3] + [fade_win(r, 0.2, 3, 0.1 + i * 0.4, 0.5 + i * 0.4) for i, r in enumerate(rows)]
    return head, body


@icon
def clipboard_text():
    # the lines are written in turn
    head, els = load('clipboard-text')
    return head, els[:3] + [redraw(els[3], 6, 4, 0.1, 0.6), redraw(els[4], 6, 4, 0.45, 0.95)]


@icon
def clock_pause():
    # the pause bars blink
    head, els = load('clock-pause')
    return head, els[:3] + [fade_win(els[3:5], 0.2, 2)]


@icon
def cloud():
    # drifts
    head, els = load('cloud')
    return head, [els[0], group(els[1:], osc('transform', ['0 0', '0.8 0', '0 0', '-0.8 0'], 5, kind='translate'))]


@icon
def cloud_rain():
    # the drops fall
    head, els = load('cloud-rain')
    drops = []
    for x, y, dur in ((11, 13, 1.2), (15, 15, 1.5)):
        drops.append(with_anim(f'<path d="M{x} {y}v7" stroke-dasharray="2 3"/>', lin('stroke-dashoffset', '0', '-5', dur)))
    return head, els[:2] + drops


@icon
def clover():
    # turns a quarter and rests
    head, els = load('clover')
    a = anim('transform', ['0 12 12', '0 12 12', '90 12 12'], [0, 0.4, 1], 4, kind='rotate')
    return head, [els[0], group(els[1:], a)]


@icon
def code():
    # the brackets breathe apart
    head, els = load('code')
    return head, [els[0], move_win([els[1]], -0.8, 0, 2), move_win([els[2]], 0.8, 0, 2), els[3]]


def rise(children, dur, dy=1.5):
    """Rises [dy] and fades, then comes up from below."""
    t = anim('transform', ['0 0', f'0 {fmt(-dy)}', f'0 {fmt(dy * 0.66)}', '0 0'], [0, 0.5, 0.5, 1], dur, kind='translate', spline=False)
    o = anim('opacity', ['1', '0', '0', '1'], [0, 0.5, 0.5, 1], dur, spline=False)
    return group(children, t, o)


@icon
def coffee():
    # steam rises
    head, els = load('coffee')
    return head, [els[0], els[1], rise([els[2]], 2), rise([els[3]], 2.5)] + els[4:]


@icon
def coin():
    # flips
    head, els = load('coin')
    return head, [els[0], about(els[1:], 12, 12, osc('transform', ['1 1', '0.15 1'], 3, kind='scale'))]


@icon
def columns():
    # text lines settle, column by column
    head, els = load('columns')
    body = [els[0]]
    for i, e in enumerate(els[1:]):
        m = re.search(r'd="M([\d.]+) ([\d.]+)l5.5 0"', e)
        x, y = m.group(1), m.group(2)
        row, col = i % 4, i // 4
        start = 0.05 + row * 0.12 + col * 0.25
        short = '3' if row % 2 else '4'
        body.append(with_anim(e, window('d', f'M{x} {y}l5.5 0', f'M{x} {y}l{short} 0', 4, start, start + 0.3)))
    return head, body


@icon
def confetti():
    # the bits twinkle and turn
    head, els = load('confetti')
    plus = [(els[1:3], 5, 5, 2), (els[4:6], 19, 5, 3), (els[8:10], 19, 19, 2)]
    body = [els[0]]
    for ps_, cx, cy, d in plus:
        body.append(group(ps_, anim('transform', [f'0 {cx} {cy}', f'0 {cx} {cy}', f'90 {cx} {cy}'], [0, 0.5, 1], d * 2, kind='rotate')))
    body.append(fade_win([els[3]], 0.2, 2, 0, 0.5))
    body.append(fade_win([els[6]], 0.2, 2, 0.3, 0.8))
    body.append(fade_win([els[7]], 0.2, 2, 0.5, 1))
    body.append(rock([els[10]], 4, 20, 3, 4))
    return head, body


@icon
def copy():
    # the copy slides off and back
    head, els = load('copy')
    return head, [els[0], move_win([els[1]], 0.7, 0.7, 2.5), els[2]]


@icon
def copy_plus():
    # the copy slides, the plus turns
    head, els = load('copy-plus')
    plus = group(els[3:5], osc('transform', ['0 14 14', '90 14 14'], 2.5, kind='rotate'))
    return head, [els[0], move_win([els[1], plus], 0.7, 0.7, 2.5), els[2]]


@icon
def credit_card():
    # swipes
    head, els = load('credit-card')
    return head, [els[0], group(els[1:], osc('transform', ['0 0', '1 0', '0 0', '-1 0'], 3, kind='translate'))]


@icon
def crop():
    # the corners open and close
    head, els = load('crop')
    return head, [els[0], move_win([els[1]], -0.7, 0.7, 2.5), move_win([els[2]], 0.7, -0.7, 2.5)]


@icon
def cross():
    # glows
    head, els = load('cross')
    return head, [els[0], pulse(els[1:], 12, 12, 0.07, 3)]


@icon
def crosshairs():
    # the sight tightens on the dot
    head, els = load('crosshairs')
    return head, [els[0], scale_win([els[1]], 12, 12, 1.6, 2), scale_win([els[2]], 12, 12, 0.92, 2)]


@icon
def crown():
    # tips this way and that
    head, els = load('crown')
    return head, [els[0], rock(els[1:], 12, 18, 4, 3)]


@icon
def cup():
    # the straw stirs
    head, els = load('cup')
    return head, els[:4] + [rock([els[4]], 15, 5, 20, 2)]


@icon
def current_location():
    # the ticks turn, the center pulses
    head, els = load('current-location')
    ticks = group(els[3:7], anim('transform', ['0 12 12', '0 12 12', '90 12 12'], [0, 0.5, 1], 4, kind='rotate'))
    return head, [els[0], scale_win([els[1]], 12, 12, 0.7, 2), els[2], ticks]


@icon
def cut():
    # the blades snip
    head, els = load('cut')
    px, py = 12, 11.36
    return head, [els[0], tilt([els[1], els[3]], px, py, -10, 1.6), tilt([els[2], els[4]], px, py, 10, 1.6)]


@icon
def dashboard():
    # each tile pulses in turn
    head, els = load('dashboard')
    centers = [(7, 8), (7, 18), (17, 16), (17, 6)]
    body = [els[0]]
    for i, (e, (cx, cy)) in enumerate(zip(els[1:], centers)):
        body.append(scale_win([e], cx, cy, 0.85, 4, i * 0.22, i * 0.22 + 0.3))
    return head, body


@icon
def database():
    # the lid lifts
    head, els = load('database')
    return head, [els[0], move_win([els[1]], 0, -0.9, 2.5)] + els[2:]


@icon
def database_arrow():
    # the arrow lifts
    head, els = load('database-arrow')
    return head, els[:4] + [move_win(els[4:6], 0, -1.2, 1.8)]


@icon
def database_minus():
    # the minus narrows
    head, els = load('database-minus')
    return head, els[:5] + [scale_win([els[5]], 19, 19, '0.5 1', 2)]


@icon
def database_plus():
    # the plus turns
    head, els = load('database-plus')
    return head, els[:5] + [group(els[5:7], osc('transform', ['0 19 19', '90 19 19'], 2.5, kind='rotate'))]


@icon
def dental():
    # wobbles
    head, els = load('dental')
    return head, [els[0], rock(els[1:], 12, 20, 4, 2.5)]


@icon
def device_desktop():
    # a cursor blinks on the screen
    head, els = load('device-desktop')
    cursor = with_anim('<path d="M7 7.5v3" opacity="0"/>', anim('opacity', ['0', '1', '0'], [0, 0.25, 0.75], 1.6, spline=False, extra=' calcMode="discrete"'))
    return head, els + [cursor]


@icon
def device_floppy():
    # the shutter slides
    head, els = load('device-floppy')
    return head, els[:3] + [move_win([els[3]], 1.5, 0, 2.5)]


@icon
def device_gamepad():
    # buttons get pressed
    head, els = load('device-gamepad')
    pad = scale_win(els[3:5], 8, 10, 0.75, 2, 0, 0.4)
    btn = move_win([els[5]], 0, 0.5, 2, 0.5, 0.8)
    return head, els[:3] + [pad, btn]


@icon
def device_laptop():
    # the lid tilts
    head, els = load('device-laptop')
    return head, [els[0], els[1], scale_win([els[2]], 12, 16, '1 0.85', 3)]


@icon
def device_projector():
    # the lens pulses
    head, els = load('device-projector')
    return head, [els[0], scale_win([els[1]], 13, 9, 0.85, 2.5)] + els[2:]


@icon
def device_speaker():
    # the cone thumps
    head, els = load('device-speaker')
    cone = about([els[2]], 12, 14, anim('transform', ['1', '1.15', '1', '1.1', '1', '1'], [0, 0.15, 0.35, 0.5, 0.7, 1], 1.6, kind='scale'))
    return head, [els[0], els[1], cone, els[3]]


@icon
def device_tv():
    # the antenna wiggles
    head, els = load('device-tv')
    return head, [els[0], els[1], rock([els[2]], 12, 7, 8, 2.5)]


@icon
def diamond():
    # bobs and glints
    head, els = load('diamond')
    return head, [els[0], move_win([els[1], fade_win([els[2]], 0.1, 3, 0.2, 0.6)], 0, -0.6, 3)]


@icon
def dice():
    # rolls a quarter turn
    head, els = load('dice')
    a = anim('transform', ['0 12 12', '0 12 12', '90 12 12'], [0, 0.5, 1], 2.5, kind='rotate')
    shrink = anim('transform', ['1', '1', '0.88', '1'], [0, 0.5, 0.75, 1], 2.5, kind='scale')
    return head, [els[0], group([about(els[1:], 12, 12, shrink)], a)]


@icon
def directions():
    # the signs swing
    head, els = load('directions')
    return head, els[:5] + [rock([els[5]], 12, 7, 3, 3), rock([els[6]], 12, 15, -3, 3)]


@icon
def disc():
    # spins
    head, els = load('disc')
    return head, els[:3] + [group(els[3:5], spin(12, 12, 4))]


@icon
def disc_golf():
    # the chains sway
    head, els = load('disc-golf')
    return head, [els[0], els[1], rock(els[2:6], 12, 5, 3, 3)] + els[6:]


@icon
def dog():
    # the ears flop
    head, els = load('dog')
    return head, els[:7] + [tilt([els[7]], 9, 4.6, -7, 2, 0, 0.5), tilt([els[8]], 15, 4.6, 7, 2, 0.4, 0.9)]


@icon
def door():
    # swings open a little
    head, els = load('door')
    return head, [els[0], els[2], scale_win([els[1], els[3]], 6, 12, '0.8 1', 3)]


@icon
def door_enter():
    # the arrow goes in
    head, els = load('door-enter')
    return head, els[:4] + [move_win([els[4]], -1, 0, 1.8)]


@icon
def door_exit():
    # the arrow goes out
    head, els = load('door-exit')
    return head, els[:4] + [move_win([els[4]], 1, 0, 1.8)]


@icon
def dots():
    # a wave runs along
    head, els = load('dots')
    return head, [els[0]] + [move_win([e], 0, -1, 1.8, i * 0.2, i * 0.2 + 0.4) for i, e in enumerate(els[1:])]


@icon
def dots_vertical():
    # each dot swells in turn
    head, els = load('dots-vertical')
    order = [(els[3], 5), (els[1], 12), (els[2], 19)]
    return head, [els[0]] + [scale_win([e], 12, y, 1.4, 1.8, i * 0.2, i * 0.2 + 0.4) for i, (e, y) in enumerate(order)]


@icon
def download():
    # the arrow drops
    head, els = load('download')
    return head, [els[0], els[1], move_win(els[2:4], 0, 1, 1.6)]


@icon
def droplet():
    # jiggles
    head, els = load('droplet')
    return head, [els[0], about(els[1:], 12, 21, osc('transform', ['1 1', '1.05 0.95', '0.96 1.04'], 2.5, kind='scale'))]


@icon
def droplet_heart():
    # the heart beats
    head, els = load('droplet-heart')
    beat = about([els[2]], 18, 18.5, anim('transform', ['1', '1.15', '1', '1.1', '1', '1'], [0, 0.15, 0.3, 0.45, 0.7, 1], 1.4, kind='scale'))
    return head, [els[0], els[1], beat]


@icon
def earth():
    # the lands turn
    head, els = load('earth')
    return head, [els[0], rock(els[1:4], 12, 12, 8, 5), els[4]]


@icon
def edit():
    # the pencil scribbles
    head, els = load('edit')
    pen = group(els[2:4], osc('transform', ['0 0', '-0.8 0.5', '0.3 0.8'], 1.6, kind='translate'))
    return head, [els[0], els[1], pen]


@icon
def eraser():
    # rubs back and forth
    head, els = load('eraser')
    return head, [els[0], group(els[1:], osc('transform', ['0 0', '-0.8 0', '0 0', '0.8 0'], 1.6, kind='translate'))]


@icon
def expand_below():
    # the chevron nudges down
    head, els = load('expand-below')
    return head, els[:3] + [move_win([els[3]], 0, 0.7, 1.6)]


@icon
def external_link():
    # the arrow points out
    head, els = load('external-link')
    return head, [els[0], els[1], move_win(els[2:4], 0.8, -0.8, 1.8)]


def squash_d(d, f):
    """Scales the relative y values of the eye outline path [d] by [f] about y=12."""
    out = []
    for cmd in re.findall(r'[MmCcLlAaHhVvSsQqZz][^MmCcLlAaHhVvSsQqZz]*', d):
        c, nums = cmd[0], [float(x) for x in re.findall(r'-?\d*\.?\d+', cmd[1:])]
        if c == 'M':
            nums = [nums[0], 12 + (nums[1] - 12) * f]
        elif c == 'c':
            nums = [v * f if i % 2 else v for i, v in enumerate(nums)]
        out.append(c + ' '.join(fmt(v) for v in nums))
    return ''.join(out)


@icon
def eye():
    # looks around and blinks
    head, els = load('eye')
    outline = re.search(r'd="([^"]+)"', els[2]).group(1)
    shut = squash_d(outline, 0.08)
    kts = [0, 0.8, 0.86, 0.92, 1]
    lid = with_anim(els[2], anim('d', [outline, outline, shut, outline, outline], kts, 4))
    ball = 'M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0'
    ball_shut = 'M10 12a2 0.16 0 1 0 4 0a2 0.16 0 0 0 -4 0'
    pupil_el = with_anim(els[1], anim('d', [ball, ball, ball_shut, ball, ball], kts, 4))
    pupil = group([pupil_el], anim('transform', ['0 0', '0 0', '-1.2 0', '-1.2 0', '1.2 0', '1.2 0', '0 0', '0 0'],
                                   [0, 0.1, 0.2, 0.35, 0.45, 0.6, 0.7, 1], 4, kind='translate'))
    return head, [els[0], pupil, lid]


@icon
def eye_closed():
    # sleeps, breathing
    head, els = load('eye-closed')
    return head, [els[0], group(els[1:], osc('transform', ['0 0', '0 0.5'], 4, kind='translate'))]


@icon
def eye_minus():
    # looks about, the minus narrows
    head, els = load('eye-minus')
    pupil = group([els[1]], osc('transform', ['0 0', '-1 0', '0 0', '1 0'], 4, kind='translate'))
    return head, [els[0], pupil, els[2], scale_win([els[3]], 19, 19, '0.5 1', 2)]


@icon
def eye_off():
    # the slash is drawn again
    head, els = load('eye-off')
    return head, els[:3] + [redraw(els[3], 25.5, 3)]


def check(name):
    src = open(f'{SVG_DIR}/{name}-animated.svg').read()
    for a in re.findall(r'<animate[^>]*>', src):
        kt = re.search(r'keyTimes="([^"]+)"', a)
        vals = re.search(r'values="([^"]+)"', a)
        if kt:
            k = [float(x) for x in kt.group(1).split(';')]
            ok = k[0] == 0 and all(b >= a_ for a_, b in zip(k, k[1:])) and k[-1] <= 1
            if 'discrete' not in a:
                ok = ok and k[-1] == 1
            if vals and len(vals.group(1).split(';')) != len(k):
                ok = False
            ks = re.search(r'keySplines="([^"]+)"', a)
            if ks and len(ks.group(1).split(';')) != len(k) - 1:
                ok = False
            if not ok:
                print('BAD', name, a[:160])


if __name__ == '__main__':
    only = sys.argv[1:]
    for name, f in W.items():
        if only and name not in only:
            continue
        head, body = f()
        save(name, head, body)
        check(name)
    print('wrote', len(only) or len(W))

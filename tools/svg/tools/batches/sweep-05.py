import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# --- local helpers ---

def kanim(attr, values, keytimes, dur, kind=None, ease=True):
    """An animate/animateTransform with explicit keyTimes."""
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"'
    if kind:
        a += f' type="{kind}"'
    a += f' values="{";".join(values)}" keyTimes="{";".join(fmt(k) for k in keytimes)}" dur="{dur}s" repeatCount="indefinite"'
    if ease:
        a += f' calcMode="spline" keySplines="{splines(len(values) - 1)}"'
    return a + '/>'


def blip(base, peak, start, width):
    """Values and keyTimes that hold [base], swing to [peak] around [start] for [width] of the loop, and return."""
    s, m, e = start, start + width / 2, start + width
    vals, kts = [base], [0]
    if s > 0:
        vals.append(base); kts.append(s)
    vals += [peak, base]; kts += [m, e]
    if e < 1:
        vals.append(base); kts.append(1)
    return vals, kts


def rock(children, cx, cy, degs, dur):
    """Rotates [children] about (cx, cy) through the angles [degs] and back."""
    return group(children, osc('transform', [f'{fmt(d)} {fmt(cx)} {fmt(cy)}' for d in [0] + degs], dur, kind='rotate'))


def scale_about(children, cx, cy, values, dur, keytimes=None):
    """Scales [children] about (cx, cy) through [values] (scale strings)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    if keytimes:
        an = kanim('transform', values, keytimes, dur, kind='scale')
    else:
        an = osc('transform', values, dur, kind='scale')
    mid = group([inner], an)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def apath(ds, dur, keytimes=None, extra=''):
    """A path whose d moves through [ds] and back."""
    an = kanim('d', ds, keytimes, dur) if keytimes else osc('d', ds, dur)
    return f'<path d="{ds[0]}"{extra}>\n    {an}\n</path>'


def blink(children, cx, cy, dur, at=0.8):
    """Squashes [children] vertically about (cx, cy) once per loop, as a blink."""
    vals, kts = blip('1 1', '1 0.15', at, 0.08)
    return scale_about(children, cx, cy, vals, dur, keytimes=kts)


def bob(children, dx, dy, start, width, dur):
    """Moves [children] by (dx, dy) and back once per loop, starting at [start]."""
    vals, kts = blip('0 0', f'{fmt(dx)} {fmt(dy)}', start, width)
    return group(children, kanim('transform', vals, kts, dur, kind='translate'))


def split(els):
    frame = [e for e in els if is_frame(e)]
    rest = [e for e in els if not is_frame(e)]
    return frame, rest


# --- icons ---

def hourglass():
    # flips over, then rests while the sand runs
    head, els = load('hourglass')
    f, r = split(els)
    an = kanim('transform', ['0 12 12', '0 12 12', '180 12 12'], [0, 0.75, 1], 4, kind='rotate')
    save('hourglass', head, f + [group(r, an)])


def ice_cream():
    # tilts on its stick
    head, els = load('ice-cream')
    f, r = split(els)
    save('ice-cream', head, f + [rock(r, 12, 21.5, [-6, 6], 3)])


def ice_skating():
    # glides back and forth
    head, els = load('ice-skating')
    f, r = split(els)
    save('ice-skating', head, f + [group(r, osc('transform', ['0 0', '1.2 0', '-0.8 0'], 3, kind='translate'))])


def id_card():
    # the text lines write out in turn
    head, els = load('id')
    f, r = split(els)
    card, face = r[0], r[1]
    lines = [(15, 8, 2), (15, 12, 2), (7, 16, 10)]
    body = f + [card, face]
    for i, (x, y, w) in enumerate(lines):
        full = f'M{x} {y}l{w} 0'
        short = f'M{x} {y}l{fmt(w * 0.25)} 0'
        vals, kts = blip(full, short, 0.1 + i * 0.25, 0.35)
        body.append(apath([v for v in vals], 4, keytimes=kts))
    save('id', head, body)


def info():
    # the dot hops
    head, els = load('info')
    f, r = split(els)
    save('info', head, f + [r[0], bob([r[1]], 0, -1.5, 0.2, 0.4, 2), r[2]])


def italic():
    # leans further and back
    head, els = load('italic')
    f, r = split(els)
    save('italic', head, f + [r[0], r[1], apath(['M14 5l-4 14', 'M15 5l-6 14', 'M13 5l-2 14'], 3)])


def jacket():
    # the zip runs down and up
    head, els = load('jacket')
    f, r = split(els)
    body = f + r[:4] + [apath(['M12 19v-11', 'M12 19v-3'], 3)]
    save('jacket', head, body)


def jewish_star():
    # a gentle swell
    head, els = load('jewish-star')
    f, r = split(els)
    save('jewish-star', head, f + [pulse(r, amount=0.07, dur=3)])


def jump_rope():
    # the handles lift in turn and the rope follows
    head, els = load('jump-rope')
    f, r = split(els)
    rope_ds = []
    offs = [(0, 0), (-1, 1), (0, 0), (1, -1)]
    for a, b in offs:
        rope_ds.append(f'M6 {fmt(14 + a)}v{fmt(-6 - a)}a3 3 0 1 1 6 0v8a3 3 0 0 0 6 0v{fmt(-6 + b)}')
    right = group([r[1]], osc('transform', [f'0 {fmt(b)}' for a, b in offs], 2.4, kind='translate'))
    left = group([r[2]], osc('transform', [f'0 {fmt(a)}' for a, b in offs], 2.4, kind='translate'))
    save('jump-rope', head, f + [apath(rope_ds, 2.4), right, left])


def karate():
    # the kicking leg snaps up and back
    head, els = load('karate')
    f, r = split(els)
    leg = r[0]
    vals, kts = blip('0 10.5 12.5', '12 10.5 12.5', 0.15, 0.5)
    save('karate', head, f + [group([leg], kanim('transform', vals, kts, 2, kind='rotate'))] + r[1:])


def kayak():
    # the paddle strokes side to side
    head, els = load('kayak')
    f, r = split(els)
    paddle = r[0:3]
    boat = r[3:]
    save('kayak', head, f + boat + [rock(paddle, 12, 12, [12, -12], 3)])


def key():
    # turns a little in the lock
    head, els = load('key')
    f, r = split(els)
    save('key', head, f + [rock(r, 16.8, 8.2, [-10, 4], 2.5)])


def keyboard():
    # keys light in turn
    head, els = load('keyboard')
    f, r = split(els)
    frame, keys = r[0], r[1:]
    order = [0, 4, 1, 6, 2, 5, 3]
    body = f + [frame]
    for i, k in enumerate(keys):
        vals, kts = blip('1', '0.2', 0.04 + order.index(i) * 0.12, 0.14)
        body.append(group([k], kanim('opacity', vals, kts, 3)))
    save('keyboard', head, body)


def label():
    # sways on its point
    head, els = load('label')
    f, r = split(els)
    save('label', head, f + [rock(r, 4, 12, [4, -3], 3)])


def language():
    # the two glyphs bob in turn
    head, els = load('language')
    f, r = split(els)
    left = [r[0], r[1], r[2], r[5]]
    right = [r[3], r[4]]
    save('language', head, f + [bob(left, 0, -0.8, 0, 0.5, 3), bob(right, 0, -0.8, 0.5, 0.5, 3)])


def layout_bottombar():
    # the bar rises and settles
    head, els = load('layout-bottombar')
    f, r = split(els)
    save('layout-bottombar', head, f + [r[0], apath(['M4 15l16 0', 'M4 13l16 0'], 3)])


def layout_grid():
    # the tiles swell in turn
    head, els = load('layout-grid')
    f, r = split(els)
    centers = [(7, 7), (17, 7), (17, 17), (7, 17)]
    order = [0, 1, 3, 2]
    body = list(f)
    for i, (el, (cx, cy)) in enumerate(zip(r, centers)):
        vals, kts = blip('1', '0.8', order[i] * 0.25, 0.25)
        body.append(scale_about([el], cx, cy, vals, 4, keytimes=kts))
    save('layout-grid', head, body)


def layout_minimal():
    # the short lines stretch in turn
    head, els = load('layout-minimal')
    f, r = split(els)
    v1, k1 = blip('M13 9h5', 'M13 9h8', 0, 0.5)
    v2, k2 = blip('M13 19h5', 'M13 19h8', 0.5, 0.5)
    save('layout-minimal', head, f + [r[0], apath(v1, 3, keytimes=k1), r[2], apath(v2, 3, keytimes=k2), r[4], r[5]])


def layout_navbar():
    # the bar lowers and settles
    head, els = load('layout-navbar')
    f, r = split(els)
    save('layout-navbar', head, f + [r[0], apath(['M4 9l16 0', 'M4 11l16 0'], 3)])


def layout_row():
    # the rows draw apart and back
    head, els = load('layout-row')
    f, r = split(els)
    save('layout-row', head, f + [shift([r[0]], 0, -0.7, 2.5), shift([r[1]], 0, 0.7, 2.5)])


def leaf():
    # sways on its stem
    head, els = load('leaf')
    f, r = split(els)
    save('leaf', head, f + [rock(r, 5, 21, [-4, 3], 3.5)])


def letter_t():
    # the bar narrows and widens
    head, els = load('letter-t')
    f, r = split(els)
    save('letter-t', head, f + [apath(['M6 4l12 0', 'M8 4l8 0'], 2.5), r[1]])


def library_photo():
    # the back print slides out and the sun rises
    head, els = load('library-photo')
    f, r = split(els)
    front, back, sun, m1, m2 = r
    save('library-photo', head, f + [front, shift([back], -0.8, 0.8, 3), shift([sun], 0, -0.8, 3), m1, m2])


def lifebuoy():
    # turns slowly in the water
    head, els = load('lifebuoy')
    f, r = split(els)
    save('lifebuoy', head, f + r[:2] + [group(r[2:], spin(12, 12, 8))])


def light():
    # the flame flickers from its base
    head, els = load('light')
    f, r = split(els)
    save('light', head, f + [scale_about(r, 12, 20.4, ['1 1', '1.04 0.95', '0.97 1.05'], 1.8)])


def link():
    # the links draw apart and close
    head, els = load('link')
    f, r = split(els)
    save('link', head, f + [r[0], shift([r[1]], 0.7, -0.7, 2), shift([r[2]], -0.7, 0.7, 2)])


def list_icon():
    # the lines shorten in turn
    head, els = load('list')
    f, r = split(els)
    body = list(f)
    for i, y in enumerate((6, 12, 18)):
        vals, kts = blip(f'M9 {y}l11 0', f'M9 {y}l6 0', 0.05 + i * 0.25, 0.4)
        body.append(apath(vals, 3, keytimes=kts))
    save('list', head, body + r[3:])


def list_numbers():
    # the lines shorten in turn
    head, els = load('list-numbers')
    f, r = split(els)
    body = list(f)
    for i, (x, y, w) in enumerate(((11, 6, 9), (11, 12, 9), (12, 18, 8))):
        vals, kts = blip(f'M{x} {y}h{w}', f'M{x} {y}h{w - 5}', 0.05 + i * 0.25, 0.4)
        body.append(apath(vals, 3, keytimes=kts))
    save('list-numbers', head, body + r[3:])


def loader():
    # turns
    head, els = load('loader')
    f, r = split(els)
    save('loader', head, f + [group(r, spin(12, 12, 4))])


def lock():
    # the shackle tugs up and drops back
    head, els = load('lock')
    f, r = split(els)
    save('lock', head, f + r[:2] + [bob([r[2]], 0, -1, 0.1, 0.4, 2.5)])


def lock_open():
    # the shackle bobs above the body
    head, els = load('lock-open')
    f, r = split(els)
    save('lock-open', head, f + r[:2] + [shift([r[2]], 0, -0.8, 2.5)])


def locomotive():
    # chugs along the rail
    head, els = load('locomotive')
    f, r = split(els)
    save('locomotive', head, f + [shift(r[:6], 0, -0.5, 1), r[6]])


def login():
    # the arrow pushes in
    head, els = load('login')
    f, r = split(els)
    save('login', head, f + [r[0], shift(r[1:], -1.2, 0, 2)])


def logout():
    # the arrow pushes out
    head, els = load('logout')
    f, r = split(els)
    save('logout', head, f + [r[0], shift(r[1:], 1.2, 0, 2)])


def magic():
    # the stars twinkle in turn
    head, els = load('magic')
    f, r = split(els)
    small = 'a2 2 0 0 1 2 2a2 2 0 0 1 2 -2a2 2 0 0 1 -2 -2a2 2 0 0 1 -2 2'
    s1 = f'<path d="M16 18{small}"/>'
    s2 = f'<path d="M16 6{small}"/>'
    big = '<path d="M9 18a6 6 0 0 1 6 -6a6 6 0 0 1 -6 -6a6 6 0 0 1 -6 6a6 6 0 0 1 6 6"/>'
    v1, k1 = blip('1', '0.5', 0.1, 0.3)
    v2, k2 = blip('1', '0.5', 0.55, 0.3)
    body = f + [
        scale_about([big], 9, 12, ['1', '1.06'], 3),
        scale_about([s1], 18, 18, v1, 3, keytimes=k1),
        scale_about([s2], 18, 6, v2, 3, keytimes=k2),
    ]
    save('magic', head, body)


def mail():
    # the flap lifts and settles
    head, els = load('mail')
    f, r = split(els)
    save('mail', head, f + [r[0], apath(['M3 7l9 6l9 -6', 'M3 7l9 3l9 -3'], 3)])


def mail_plus():
    # the plus swells
    head, els = load('mail-plus')
    f, r = split(els)
    save('mail-plus', head, f + [r[0], scale_about(r[1:3], 19, 19, ['1', '1.25'], 2), r[3]])


def map_icon():
    # the pin hops on the map
    head, els = load('map')
    f, r = split(els)
    save('map', head, f + r[:3] + [bob(r[3:], 0, -1.2, 0.1, 0.45, 2.5)])


def map_pin():
    # hops
    head, els = load('map-pin')
    f, r = split(els)
    save('map-pin', head, f + [bob(r, 0, -1.2, 0.1, 0.45, 2.5)])


def map_pin_share():
    # the arrow pushes out
    head, els = load('map-pin-share')
    f, r = split(els)
    save('map-pin-share', head, f + r[:2] + [shift(r[2:], 0.8, -0.8, 2)])


def masks_theater():
    # the masks rock against each other
    head, els = load('masks-theater')
    f, r = split(els)
    save('masks-theater', head, f + [rock(r[:4], 16.5, 15, [5, -3], 3), rock(r[4:], 8, 10, [-5, 3], 3)])


def massage():
    # the hands press
    head, els = load('massage')
    f, r = split(els)
    save('massage', head, f + [r[0], r[2], r[3], shift([r[1], r[4]], 0, 0.7, 1.6)])


def meat():
    # sways on the bone
    head, els = load('meat')
    f, r = split(els)
    save('meat', head, f + [rock(r, 16, 8, [-5, 4], 3)])


def medal():
    # the star swells on its ribbon
    head, els = load('medal')
    f, r = split(els)
    save('medal', head, f + [r[0], scale_about([r[1]], 12, 15.6, ['1', '1.15'], 2.5)])


def medicine_syrup():
    # the cross swells
    head, els = load('medicine-syrup')
    f, r = split(els)
    save('medicine-syrup', head, f + [r[0], scale_about(r[1:3], 12, 14, ['1', '1.3'], 2), r[3]])


def menu():
    # the bars slide in turn
    head, els = load('menu')
    f, r = split(els)
    body = list(f)
    for i, el in enumerate(r):
        vals, kts = blip('20', '15', 0.05 + i * 0.25, 0.4)
        body.append(el.replace('/>', f'>\n    {kanim("x2", vals, kts, 3)}\n</line>'))
    save('menu', head, body)


def message():
    # the text lines write in turn
    head, els = load('message')
    f, r = split(els)
    v1, k1 = blip('M8 9h8', 'M8 9h2', 0.05, 0.4)
    v2, k2 = blip('M8 13h6', 'M8 13h1.5', 0.35, 0.4)
    save('message', head, f + [apath(v1, 3, keytimes=k1), apath(v2, 3, keytimes=k2), r[2]])


def message_circle():
    # rocks on its tail
    head, els = load('message-circle')
    f, r = split(els)
    save('message-circle', head, f + [rock(r, 3, 20, [-3, 2], 3)])


def message_dots():
    # the dots hop in turn
    head, els = load('message-dots')
    f, r = split(els)
    order = {1: 0, 0: 1, 2: 2}  # left, middle, right
    body = list(f)
    for i in range(3):
        body.append(bob([r[i]], 0, -1.2, 0.05 + order[i] * 0.2, 0.3, 1.8))
    save('message-dots', head, body + [r[3]])


def message_heart():
    # the heart beats
    head, els = load('message-heart')
    f, r = split(els)
    beat = scale_about([r[3]], 18, 18.5, ['1', '1.15', '1', '1.1', '1', '1'], 1.6,
                       keytimes=[0, 0.12, 0.25, 0.37, 0.5, 1])
    save('message-heart', head, f + r[:3] + [beat])


def message_minus():
    # the minus shortens and grows
    head, els = load('message-minus')
    f, r = split(els)
    save('message-minus', head, f + r[:3] + [apath(['M16 19h6', 'M17.5 19h3'], 2)])


def message_plus():
    # the plus swells
    head, els = load('message-plus')
    f, r = split(els)
    save('message-plus', head, f + r[:3] + [scale_about(r[3:], 19, 19, ['1', '1.25'], 2)])


def message_reply():
    # the arrow pushes back
    head, els = load('message-reply')
    f, r = split(els)
    save('message-reply', head, f + [r[0], shift(r[1:], -1, 0, 2)])


def messages():
    # the bubbles bob in turn
    head, els = load('messages')
    f, r = split(els)
    save('messages', head, f + [bob([r[0]], 0, -0.7, 0, 0.5, 3), bob([r[1]], 0, -0.7, 0.5, 0.5, 3)])


def metronome():
    # the arm swings across
    head, els = load('metronome')
    f, r = split(els)
    body_d = re.search(r'd="([^"]+)"', r[0]).group(1).split('m-4.935')[0]
    case = f'<path d="{body_d}"/>'
    arm = '<path d="M11 18l9 -13"/>'
    swing = group([arm, r[1]], osc('transform', ['0 11 18', '-50 11 18'], 1.6, kind='rotate'))
    save('metronome', head, f + [case, swing])


def microphone():
    # the head bobs in its cradle
    head, els = load('microphone')
    f, r = split(els)
    save('microphone', head, f + [shift([r[0]], 0, -0.7, 2), r[1], r[2], r[3]])


def microphone_handheld():
    # sways in the hand
    head, els = load('microphone-handheld')
    f, r = split(els)
    save('microphone-handheld', head, f + [rock(r, 4.9, 19, [-6, 4], 3)])


def minimize():
    # the corners draw in
    head, els = load('minimize')
    f, r = split(els)
    dirs = [(-1, -1), (-1, 1), (1, -1), (1, 1)]
    save('minimize', head, f + [shift([e], 0.8 * dx, 0.8 * dy, 2) for e, (dx, dy) in zip(r, dirs)])


def minus():
    # shortens and grows
    head, els = load('minus')
    f, r = split(els)
    save('minus', head, f + [apath(['M5 12l14 0', 'M7.5 12l9 0'], 2)])


def mood_crazy_happy():
    # the crossed eyes spin
    head, els = load('mood-crazy-happy')
    f, r = split(els)
    save('mood-crazy-happy', head, f + [r[0], group(r[1:3], spin(8.5, 10, 2)),
                                        group(r[3:5], spin(15.5, 10, 2, reverse=True)), r[5]])


def mood_happy():
    # blinks
    head, els = load('mood-happy')
    f, r = split(els)
    save('mood-happy', head, f + [r[0], blink(r[1:3], 12, 9, 4), r[3]])


def mood_kid():
    # blinks and the curl bounces
    head, els = load('mood-kid')
    f, r = split(els)
    save('mood-kid', head, f + [r[0], blink(r[1:3], 12, 10, 4), r[3], rock([r[4]], 12, 7, [-12, 8], 2)])


ALL = [hourglass, ice_cream, ice_skating, id_card, info, italic, jacket, jewish_star, jump_rope, karate, kayak, key,
       keyboard, label, language, layout_bottombar, layout_grid, layout_minimal, layout_navbar, layout_row, leaf,
       letter_t, library_photo, lifebuoy, light, link, list_icon, list_numbers, loader, lock, lock_open, locomotive,
       login, logout, magic, mail, mail_plus, map_icon, map_pin, map_pin_share, masks_theater, massage, meat, medal,
       medicine_syrup, menu, message, message_circle, message_dots, message_heart, message_minus, message_plus,
       message_reply, messages, metronome, microphone, microphone_handheld, minimize, minus, mood_crazy_happy,
       mood_happy, mood_kid]

if __name__ == '__main__':
    only = set(sys.argv[1:])
    for fn in ALL:
        if not only or fn.__name__ in only:
            fn()
    print(len(ALL), 'icons')

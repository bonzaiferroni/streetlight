import math
import os
import re
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# local helpers

def anim(attr, vals, keys, dur, kind=None, ease=True, extra=''):
    """An animate/animateTransform through [vals] at [keys], looping."""
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"'
    if kind:
        a += f' type="{kind}"'
    a += f' values="{";".join(vals)}" keyTimes="{";".join(fmt(k) for k in keys)}" dur="{dur}s" repeatCount="indefinite"'
    if ease:
        a += f' calcMode="spline" keySplines="{splines(len(vals) - 1)}"'
    return a + extra + '/>'


def window(base, alt, i, n):
    """Values and keyTimes that hold [base] except a dip to [alt] in the i-th of n windows."""
    a, b = i / n, (i + 1) / n
    pts = [(0, base), (a, base), ((a + b) / 2, alt), (b, base), (1, base)]
    out = []
    for k, v in pts:
        if out and abs(out[-1][0] - k) < 1e-9:
            continue
        out.append((k, v))
    return [v for _, v in out], [k for k, _ in out]


def stagger(attr, base, alt, i, n, dur, kind=None):
    vals, keys = window(base, alt, i, n)
    return anim(attr, vals, keys, dur, kind=kind)


def scale_at(children, cx, cy, anim_tag):
    """Wraps [children] so [anim_tag] (a scale animateTransform) scales about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], anim_tag)
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def rock(children, cx, cy, deg, dur, start_neg=False):
    """Rotates [children] back and forth by [deg] about (cx, cy)."""
    a, b = (-deg, deg) if start_neg else (deg, -deg)
    vals = [f'0 {fmt(cx)} {fmt(cy)}', f'{fmt(a)} {fmt(cx)} {fmt(cy)}', f'0 {fmt(cx)} {fmt(cy)}', f'{fmt(b)} {fmt(cx)} {fmt(cy)}']
    return group(children, osc('transform', vals, dur, kind='rotate'))


def path_anim(d0, ds, dur, keys=None, extra_attrs=''):
    """A path whose d runs through [ds] (first equal to d0), back to d0."""
    vals = list(ds)
    if vals[-1] != vals[0]:
        vals.append(vals[0])
    if keys:
        a = anim('d', vals, keys, dur)
    else:
        a = osc('d', vals, dur)
    return f'<path d="{d0}"{extra_attrs}>\n    {a}\n</path>'


def d_of(el):
    return re.search(r'd="([^"]+)"', el).group(1)


def split(label):
    head, els = load(label)
    frame = [e for e in els if is_frame(e)]
    rest = [e for e in els if not is_frame(e)]
    return head, frame, rest


def hub(cx, cy, r, dur, reverse=False):
    return group([f'<path d="M{fmt(cx - r)} {fmt(cy)}h{fmt(2 * r)}"/>'], spin(cx, cy, dur, reverse=reverse))


# icons

def mood_smile():
    # the smile widens and settles
    head, f, e = split('mood-smile')
    mouth = path_anim(d_of(e[3]), [d_of(e[3]), 'M9 14.6a4 4 0 0 0 6 0'], 3)
    save('mood-smile', head, f + e[:3] + [mouth])


def mood_tongue():
    # the tongue wags out and back
    head, f, e = split('mood-tongue')
    d0 = d_of(e[3])
    tongue = path_anim(d0, [d0, 'M10 14v3a2 2 0 0 0 4 0v-3m1.5 0h-7'], 2)
    save('mood-tongue', head, f + e[:3] + [tongue])


def mood_wink():
    # the face tilts playfully while winking
    head, f, e = split('mood-wink')
    save('mood-wink', head, f + [e[0], rock(e[1:], 12, 12, 8, 3)])


def moon():
    # the star and the sparkle twinkle in turn
    head, f, e = split('moon')
    star = scale_at([e[1]], 17, 6, anim('transform', ['1', '0.6', '1', '1'], [0, 0.25, 0.5, 1], 3, kind='scale'))
    spark = scale_at([e[2]], 20, 11, anim('transform', ['1', '1', '0.4', '1'], [0, 0.5, 0.75, 1], 3, kind='scale'))
    save('moon', head, f + [e[0], star, spark])


def motorbike():
    # hubs turn in the wheels, the frame bobs
    head, f, e = split('motorbike')
    hubs = [hub(5, 16, 1.2, 1.5), hub(19, 16, 1.2, 1.5)]
    save('motorbike', head, f + e[:2] + hubs + [shift(e[2:], 0, -0.5, 1.2)])


def mountain():
    # the snowline ripples
    head, f, e = split('mountain')
    d0 = d_of(e[1])
    d1 = 'M7.5 11.5l2 1.5l2.5 -1.5l2 2l2.5 -1'
    d2 = 'M7.5 10.5l2 3.5l2.5 -3.5l2 4l2.5 -3'
    save('mountain', head, f + [e[0], path_anim(d0, [d0, d1, d0, d2], 4)])


def movie():
    # the film perforations scroll down
    head, f, e = split('movie')
    rect = d_of(e[0])
    clip = f'<clipPath id="movie-clip"><path d="{rect}"/></clipPath>'
    ticks = ''.join(f'M4 {y}h4M16 {y}h4' for y in (4, 8, 12, 16))
    strip = (f'<g clip-path="url(#movie-clip)">\n    <path d="{ticks}">\n'
             f'        <animateTransform attributeName="transform" type="translate" from="0 0" to="0 4" dur="2s" repeatCount="indefinite"/>\n'
             f'    </path>\n</g>')
    save('movie', head, f + [f'<defs>{clip}</defs>', e[0], e[1], e[2], e[5], strip])


def mushroom():
    # the cap lifts on a stretching stem
    head, f, e = split('mushroom')
    cap = shift([e[0]], 0, -0.7, 2.5)
    d0 = d_of(e[1])
    stem = path_anim(d0, [d0, 'M10 11.3v7.7a2 2 0 1 0 4 0v-7.7'], 2.5)
    save('mushroom', head, f + [cap, stem])


def music():
    # the notes sway to the beat
    head, f, e = split('music')
    save('music', head, f + [rock(e, 11, 20, 6, 2)])


def music_bolt():
    # the bolt sparks
    head, f, e = split('music-bolt')
    bolt = scale_at([e[3]], 19, 19, anim('transform', ['1', '1.25', '1', '1'], [0, 0.15, 0.3, 1], 1.6, kind='scale'))
    save('music-bolt', head, f + e[:3] + [bolt])


def music_heart():
    # the heart beats twice
    head, f, e = split('music-heart')
    beat = anim('transform', ['1', '1.15', '1', '1.15', '1', '1'], [0, 0.12, 0.24, 0.36, 0.48, 1], 2, kind='scale')
    save('music-heart', head, f + e[:3] + [scale_at([e[3]], 18, 18.5, beat)])


def music_star():
    # the star turns
    head, f, e = split('music-star')
    cx, cy = 17.8, 18.35
    save('music-star', head, f + e[:3] + [group([e[3]], spin(cx, cy, 6))])


def needle():
    # the needle stitches along its length
    head, f, e = split('needle')
    save('needle', head, f + [shift(e, 0.7, -0.7, 1.6)])


def needle_thread():
    # the needle and thread stitch along the needle's length
    head, f, e = split('needle-thread')
    save('needle-thread', head, f + [shift(e, 0.7, -0.7, 1.6)])


def news():
    # the text lines are written in turn
    head, f, e = split('news')
    lines = []
    for i, y in enumerate((8, 12, 16)):
        lines.append(f'<path d="M8 {y}l4 0">\n    {stagger("d", f"M8 {y}l4 0", f"M8 {y}l1 0", i, 3, 3)}\n</path>')
    save('news', head, f + [e[0]] + lines)


def old():
    # the cane taps as the walker bobs
    head, f, e = split('old')
    cane = rock([e[4]], 16, 21, 6, 2)
    walker = shift(e[:4], 0, -0.4, 2)
    save('old', head, f + [walker, cane])


def om():
    # the crescent and dot float
    head, f, e = split('om')
    save('om', head, f + e[:3] + [shift(e[3:], 0, -0.8, 3)])


def package():
    # the package hops
    head, f, e = split('package')
    save('package', head, f + [shift(e, 0, -0.9, 2)])


def palette():
    # the paint dots swell in turn
    head, f, e = split('palette')
    dots = []
    for i, (cx, cy) in enumerate(((8.5, 10.5), (12.5, 7.5), (16.5, 10.5))):
        dots.append(scale_at([e[1 + i]], cx, cy, stagger('transform', '1', '1.3', i, 3, 3, kind='scale')))
    save('palette', head, f + [e[0]] + dots)


def panel_left():
    # the divider slides open and back
    head, f, e = split('panel-left')
    save('panel-left', head, f + [e[0], path_anim('M9 4l0 16', ['M9 4l0 16', 'M11 4l0 16'], 3)])


def panel_right():
    head, f, e = split('panel-right')
    save('panel-right', head, f + [e[0], path_anim('M15 4l0 16', ['M15 4l0 16', 'M13 4l0 16'], 3)])


def paper_bag():
    # the bag swings
    head, f, e = split('paper-bag')
    save('paper-bag', head, f + [rock(e, 12, 21, 4, 2.5)])


def paperclip():
    # the clip wiggles
    head, f, e = split('paperclip')
    save('paperclip', head, f + [rock(e, 12, 12, 8, 2.5)])


def parking():
    # the P swells
    head, f, e = split('parking')
    save('parking', head, f + [e[0], scale_at([e[1]], 12, 12, osc('transform', ['1', '1.12'], 2.5, kind='scale'))])


def paw():
    # the toes wiggle in turn
    head, f, e = split('paw')
    toes = [group([e[k]], stagger('transform', '0 0', '0 -0.6', i, 4, 2.4, kind='translate'))
            for i, k in enumerate((4, 2, 3, 1))]
    save('paw', head, f + [e[0]] + toes)


def pencil():
    # the pencil scribbles
    head, f, e = split('pencil')
    motion = '<animateMotion path="M0 0c.6 -.5 1.2 -.5 1.2 0s-.6 .5 -1.2 0" dur="1.5s" repeatCount="indefinite"/>'
    save('pencil', head, f + [group(e, motion)])


def pepper():
    # the stem sways
    head, f, e = split('pepper')
    save('pepper', head, f + [e[0], rock([e[1]], 16, 8, 10, 2.5)])


def ring(children, cx, cy, dur):
    vals = [f'{a} {fmt(cx)} {fmt(cy)}' for a in (0, 6, -6, 6, -6, 0, 0)]
    keys = [0, 0.08, 0.16, 0.24, 0.32, 0.4, 1]
    return group(children, anim('transform', vals, keys, dur, kind='rotate', ease=False))


def phone():
    # the handset rings
    head, f, e = split('phone')
    save('phone', head, f + [ring(e, 12, 12, 2)])


def phone_call():
    # the call waves pulse outward
    head, f, e = split('phone-call')
    waves = [f'<path d="{d_of(e[1 + i])}">\n    {stagger("opacity", "1", "0.15", i, 2, 2)}\n</path>' for i in range(2)]
    save('phone-call', head, f + [e[0]] + waves)


def photo():
    # the sun rises and sets over the hills
    head, f, e = split('photo')
    save('photo', head, f + e[1:] + [shift([e[0]], 0, -1.2, 3)])


def photo_up():
    # the arrow lifts
    head, f, e = split('photo-up')
    save('photo-up', head, f + e[:4] + [shift(e[4:], 0, -1, 1.6)])


def piano():
    # the black keys press in turn
    head, f, e = split('piano')
    keys = []
    for i, x in enumerate((8, 14)):
        base, down = f'M{x} 5v8h2v-8', f'M{x} 5v8.8h2v-8.8'
        keys.append(f'<path d="{base}">\n    {stagger("d", base, down, i, 2, 2)}\n</path>')
    save('piano', head, f + [e[0], e[1], e[3]] + keys)


def pig_money():
    # the coin bobs over the slot
    head, f, e = split('pig-money')
    save('pig-money', head, f + [e[0], e[2], shift([e[1]], 0, -1, 2)])


def pill():
    # the pill turns
    head, f, e = split('pill')
    save('pill', head, f + [group(e, spin(12, 12, 4, deg=180))])


def pin():
    # the pin wobbles on its point
    head, f, e = split('pin')
    save('pin', head, f + [rock(e, 4.5, 19.5, 6, 2.5)])


def ping_pong():
    # the ball bounces off the paddle
    head, f, e = split('ping-pong')
    save('ping-pong', head, f + [e[0], e[2], shift([e[1]], 0.6, 1, 1.2)])


def pinned_off():
    # the slash undraws and draws again
    head, f, e = split('pinned-off')
    n = 25.456
    dash = anim('stroke-dashoffset', ['0', '0', fmt(n), '0'], [0, 0.5, 0.75, 1], 4)
    slash = f'<path d="M3 3l18 18" stroke-dasharray="{fmt(n)}">\n    {dash}\n</path>'
    save('pinned-off', head, f + e[1:] + [slash])


def pizza():
    # the slice tips
    head, f, e = split('pizza')
    save('pizza', head, f + [rock(e, 12, 20, 6, 3)])


def plane():
    # the plane rides the air
    head, f, e = split('plane')
    pitch = rock(e, 12, 12, 3, 3)
    save('plane', head, f + [shift([pitch], 0, -0.6, 3)])


def planet():
    # the ring sways
    head, f, e = split('planet')
    save('planet', head, f + [rock([e[0]], 12, 12, 8, 4), e[1]])


def plant_2():
    # the lotus sways on its base
    head, f, e = split('plant-2')
    save('plant-2', head, f + [rock(e, 12, 19, 4, 3)])


def bar(x, top, h):
    return f'M{fmt(x)} {fmt(top)}a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v{fmt(h)}a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 {fmt(-h)}'


def player_pause():
    # the bars breathe in turn
    head, f, e = split('player-pause')
    bars = []
    for i, x in enumerate((6, 14)):
        b0, b1 = bar(x, 6, 12), bar(x, 7.5, 9)
        bars.append(f'<path d="{b0}">\n    {stagger("d", b0, b1, i, 2, 2.4)}\n</path>')
    save('player-pause', head, f + bars)


def player_play():
    # the triangle nudges forward
    head, f, e = split('player-play')
    save('player-play', head, f + [shift(e, 0.8, 0, 1.6)])


def player_record():
    # the dot breathes
    head, f, e = split('player-record')
    save('player-record', head, f + [scale_at(e, 12, 12, osc('transform', ['1', '0.85'], 2, kind='scale'))])


def player_skip_forward():
    # the triangle nudges toward the bar
    head, f, e = split('player-skip-forward')
    save('player-skip-forward', head, f + [shift([e[0]], 1, 0, 1.6), e[1]])


def quarter(children, dur):
    """A quarter turn about the center that eases, then holds."""
    vals = ['0 12 12', '90 12 12', '90 12 12']
    return group(children, anim('transform', vals, [0, 0.4, 1], dur, kind='rotate'))


def player_stop():
    # the square makes a quarter turn
    head, f, e = split('player-stop')
    save('player-stop', head, f + [quarter(e, 3)])


def plus():
    # the plus makes a quarter turn
    head, f, e = split('plus')
    save('plus', head, f + [quarter(e, 3)])


def plus_above():
    # the chevron above lifts
    head, f, e = split('plus-above')
    save('plus-above', head, f + e[:2] + [shift([e[2]], 0, -0.8, 2)])


def pool():
    # the waves rise and fall in turn
    head, f, e = split('pool')
    w1 = group([e[0]], osc('transform', ['0 0', '0 -0.5', '0 0', '0 0.5'], 3, kind='translate'))
    w2 = group([e[1]], osc('transform', ['0 0', '0 0.5', '0 0', '0 -0.5'], 3, kind='translate'))
    save('pool', head, f + [w1, w2] + e[2:])


def potted_plant():
    # the plant sways in its pot
    head, f, e = split('potted-plant')
    save('potted-plant', head, f + [e[0], rock(e[1:], 12, 15, 6, 3)])


def pray():
    # the head bows
    head, f, e = split('pray')
    save('pray', head, f + [shift([e[0]], 0.3, 0.5, 3), e[1]])


def prescription():
    # the crossing stroke draws in again
    head, f, e = split('prescription')
    n = 8.485
    dash = anim('stroke-dashoffset', ['0', '0', fmt(n), '0'], [0, 0.55, 0.7, 1], 3)
    save('prescription', head, f + e[:2] + [f'<path d="M13 21l6 -6" stroke-dasharray="{fmt(n)}">\n    {dash}\n</path>'])


def presentation():
    # the chart line shifts
    head, f, e = split('presentation')
    d0 = d_of(e[4])
    save('presentation', head, f + e[:4] + [path_anim(d0, [d0, 'M8 11l3 -1l2 -2l3 -2'], 3)])


def printer():
    # the paper feeds out
    head, f, e = split('printer')
    save('printer', head, f + e[:2] + [shift([e[2]], 0, 1, 2)])


def puzzle():
    # the piece wiggles
    head, f, e = split('puzzle')
    save('puzzle', head, f + [rock(e, 12, 12, 5, 2.5)])


def qrcode():
    # the data modules blink in turn
    head, f, e = split('qrcode')
    finders = [e[0], e[2], e[4]]
    groups = [[e[1], e[3], e[5]], [e[6], e[8], e[9]], [e[7], e[10], e[11]]]
    data = [group(g, stagger('opacity', '1', '0.2', i, 3, 2.4)) for i, g in enumerate(groups)]
    save('qrcode', head, f + finders + data)


def question():
    # the question mark tilts
    head, f, e = split('question')
    save('question', head, f + [e[0], rock(e[1:], 12, 15, 8, 2.5)])


def quote():
    # the quote marks bob in turn
    head, f, e = split('quote')
    marks = [group([e[i]], stagger('transform', '0 0', '0 -0.7', i, 2, 2.4, kind='translate')) for i in range(2)]
    save('quote', head, f + marks)


def rainbow():
    # the bands glow from inside out
    head, f, e = split('rainbow')
    bands = [f'<path d="{d_of(e[2 - i])}">\n    {stagger("opacity", "1", "0.25", i, 3, 3)}\n</path>' for i in range(3)]
    save('rainbow', head, f + bands)


def rays():
    # the rays stream in turn
    head, f, e = split('rays')
    head = head.replace('stroke="white"', 'stroke="currentColor"')
    lines = [group([e[i]], stagger('transform', '0 0', '-1 0', i, 3, 3, kind='translate')) for i in range(3)]
    save('rays', head, f + lines)


def receipt():
    # the receipt rises from the printer
    head, f, e = split('receipt')
    save('receipt', head, f + [shift(e, 0, -0.8, 2.5)])


def receipt_tax():
    # the percent sign makes a half turn
    head, f, e = split('receipt-tax')
    vals = ['0 12 11', '180 12 11', '180 12 11']
    pct = group(e[:3], anim('transform', vals, [0, 0.4, 1], 3, kind='rotate'))
    save('receipt-tax', head, f + [pct, e[3]])


ALL = [mood_smile, mood_tongue, mood_wink, moon, motorbike, mountain, movie, mushroom, music, music_bolt,
       music_heart, music_star, needle, needle_thread, news, old, om, package, palette, panel_left, panel_right,
       paper_bag, paperclip, parking, paw, pencil, pepper, phone, phone_call, photo, photo_up, piano, pig_money,
       pill, pin, ping_pong, pinned_off, pizza, plane, planet, plant_2, player_pause, player_play, player_record,
       player_skip_forward, player_stop, plus, plus_above, pool, potted_plant, pray, prescription, presentation,
       printer, puzzle, qrcode, question, quote, rainbow, rays, receipt, receipt_tax]

if __name__ == '__main__':
    names = sys.argv[1:]
    for fn in ALL:
        if not names or fn.__name__ in names:
            fn()
    print(len(ALL), 'icons')

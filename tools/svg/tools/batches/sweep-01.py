import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *


# local helpers

def win(attr, rest, inner, dur, start=0.0, length=1.0, kind=None):
    """An animation resting at [rest], moving through [inner] during the window [start, start+length] of the loop."""
    vals = [rest] + list(inner) + [rest]
    ts = [start + length * i / (len(vals) - 1) for i in range(len(vals))]
    if start > 0:
        vals = [rest] + vals; ts = [0] + ts
    if start + length < 1:
        vals = vals + [rest]; ts = ts + [1]
    tag = 'animateTransform' if kind else 'animate'
    t = f' type="{kind}"' if kind else ''
    return (f'<{tag} attributeName="{attr}"{t} values="{";".join(vals)}" keyTimes="{";".join(fmt(x) for x in ts)}" '
            f'calcMode="spline" keySplines="{splines(len(vals) - 1)}" dur="{dur}s" repeatCount="indefinite"/>')


def wmove(children, dx, dy, dur, start=0.0, length=1.0):
    return group(children, win('transform', '0 0', [f'{fmt(dx)} {fmt(dy)}'], dur, start, length, kind='translate'))


def wgrow(children, cx, cy, amount, dur, start=0.0, length=1.0):
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], win('transform', '1', [fmt(1 + amount)], dur, start, length, kind='scale'))
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def wmorph(rest, peak, dur, start=0.0, length=1.0):
    return f'<path d="{rest}">\n    {win("d", rest, [peak], dur, start, length)}\n</path>'


def wrise(el, dx, dy, dur, start=0.0, length=1.0):
    """[el] drifts by (dx, dy) while fading in and out during its window, hidden at frame 0."""
    def times(vals_n):
        return [start + length * i / (vals_n - 1) for i in range(vals_n)]
    def pad(vals, ts, rest):
        if start > 0: vals = [rest] + vals; ts = [0] + ts
        if start + length < 1: vals = vals + [vals[-1]]; ts = ts + [1]
        return vals, ts
    ov, ot = pad(['0', '1', '0'], times(3), '0')
    mv, mt = pad(['0 0', f'{fmt(dx)} {fmt(dy)}'], times(2), '0 0')
    return (f'<g opacity="0">\n'
            f'    <animate attributeName="opacity" values="{";".join(ov)}" keyTimes="{";".join(fmt(x) for x in ot)}" dur="{dur}s" repeatCount="indefinite"/>\n'
            f'    <animateTransform attributeName="transform" type="translate" values="{";".join(mv)}" keyTimes="{";".join(fmt(x) for x in mt)}" dur="{dur}s" repeatCount="indefinite"/>\n'
            f'    {el}\n</g>')


def rock(children, cx, cy, deg, dur, begin=None, one_way=False):
    """Swings [children] about (cx, cy) by [deg] either side (or one way only)."""
    c = f'{fmt(cx)} {fmt(cy)}'
    vals = [f'0 {c}', f'{fmt(deg)} {c}'] if one_way else [f'0 {c}', f'{fmt(deg)} {c}', f'0 {c}', f'{fmt(-deg)} {c}']
    return group(children, osc('transform', vals, dur, kind='rotate', begin=begin))


def move(children, dx, dy, dur, begin=None):
    """Moves [children] by (dx, dy) and back."""
    return group(children, osc('transform', ['0 0', f'{fmt(dx)} {fmt(dy)}'], dur, kind='translate', begin=begin))


def grow(children, cx, cy, amount, dur, begin=None):
    """Grows and shrinks [children] about (cx, cy)."""
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], osc('transform', ['1', fmt(1 + amount)], dur, kind='scale', begin=begin))
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def turn(children, cx, cy, dur, reverse=False):
    return group(children, spin(cx, cy, dur, reverse=reverse))


def morph(d_values, dur, begin=None):
    """A path whose d moves through [d_values] and back."""
    return f'<path d="{d_values[0]}">\n    {osc("d", d_values, dur, begin=begin)}\n</path>'


def hub(cx, cy, r, dur, reverse=False):
    """A spoke turning inside a wheel."""
    return turn([f'<path d="M{fmt(cx - r)} {fmt(cy)}h{fmt(2 * r)}"/>'], cx, cy, dur, reverse)


def fade(el, dur, begin=None, peak=0.3):
    """Shows [el] for a moment each loop, hidden at frame 0."""
    b = f' begin="{begin}"' if begin else ''
    return el.replace('/>', f' opacity="0">\n    <animate attributeName="opacity" values="0;1;1;0" keyTimes="0;{fmt(peak)};{fmt(1 - peak)};1" dur="{dur}s"{b} repeatCount="indefinite"/>\n</' + el.split()[0][1:] + '>', 1)


def rise(el, dx, dy, dur, begin=None):
    """[el] drifts by (dx, dy) while fading in and out, hidden at frame 0."""
    b = f' begin="{begin}"' if begin else ''
    return (f'<g opacity="0">\n'
            f'    <animate attributeName="opacity" values="0;1;0" keyTimes="0;0.4;1" dur="{dur}s"{b} repeatCount="indefinite"/>\n'
            f'    <animateTransform attributeName="transform" type="translate" values="0 0;{fmt(dx)} {fmt(dy)}" dur="{dur}s"{b} repeatCount="indefinite"/>\n'
            f'    {el}\n</g>')


def wfade(el, dur, start, length):
    """Shows [el] only during its window; hidden at frame 0."""
    ts = [start, start + length * 0.25, start + length * 0.75, start + length]
    vals = ['0', '1', '1', '0']
    if start > 0: vals = ['0'] + vals; ts = [0] + ts
    if start + length < 1: vals = vals + ['0']; ts = ts + [1]
    tag = el.split()[0][1:]
    return el.replace('/>', f' opacity="0">\n    <animate attributeName="opacity" values="{";".join(vals)}" keyTimes="{";".join(fmt(x) for x in ts)}" dur="{dur}s" repeatCount="indefinite"/>\n</{tag}>', 1)


def frame_and(els):
    return [e for e in els if is_frame(e)]


def parts(els):
    return [e for e in els if not is_frame(e)]


def build(label, fn):
    head, els = load(label)
    fr = frame_and(els)
    p = parts(els)
    save(label, head, fr + fn(p))


# icons; p is the list of visible elements in source order

def address_book(p):
    tabs = [wmove([p[3 + i]], -0.8, 0, 2.4, start=0.15 * i, length=0.55) for i in range(3)]
    return [p[0], move(p[1:3], 0, -0.6, 2.4)] + tabs


def affiliate(p):
    return [p[0], p[1],
            wgrow([p[2]], 5.5, 5.5, 0.25, 2.4, 0, 0.4),
            wgrow([p[3]], 18.5, 5.5, 0.25, 2.4, 0.3, 0.4),
            wgrow([p[4]], 18.5, 18.5, 0.25, 2.4, 0.6, 0.4),
            wgrow([p[5]], 8.5, 15.5, 0.06, 2.4)]


def alert_triangle(p):
    return [p[1], grow([p[0], p[2]], 12, 12.5, 0.18, 1.6)]


def alphabet_latin(p):
    return [wmove([p[0]], 0, -0.8, 2, 0, 0.5), wmove(p[1:3], 0, -0.8, 2, 0.5, 0.5)]


def anchor(p):
    return [rock(p, 12, 6, 8, 3)]


def app_window(p):
    return [p[0], wgrow([p[1]], 6, 8, 0.8, 1.6, 0, 0.5), wgrow([p[2]], 9, 8, 0.8, 1.6, 0.5, 0.5)]


def apple(p):
    return [p[0], p[1], rock([p[2]], 12, 7, 10, 3)]


def archive(p):
    return [move([p[0]], 0, -1, 2.4), p[1], p[2]]


def armchair(p):
    return [rock(p, 12, 21, 3, 3)]


def armchair_2(p):
    return [rock(p, 12, 21, 3, 3)]


def arrow_back_up(p):
    offs = [0, -1]
    head = move([p[0]], -1, 0, 2)
    shaft = morph([f'M{fmt(5 + o)} 10h{fmt(11 - o)}a4 4 0 1 1 0 8h-1' for o in offs], 2)
    return [head, shaft]


def arrow_forward_up(p):
    offs = [0, 1]
    head = move([p[0]], 1, 0, 2)
    shaft = morph([f'M{fmt(19 + o)} 10h{fmt(-11 - o)}a4 4 0 1 0 0 8h1' for o in offs], 2)
    return [head, shaft]


def arrow_left(p):
    return [move(p, -1, 0, 2)]


def arrow_right(p):
    return [move(p, 1, 0, 2)]


def arrow_up(p):
    return [move(p, 0, -1, 2)]


def arrows_maximize(p):
    dirs = [(1, -1), (-1, 1), (1, 1), (-1, -1)]
    return [move(p[2 * i:2 * i + 2], 0.8 * dx, 0.8 * dy, 2) for i, (dx, dy) in enumerate(dirs)]


def arrows_minimize(p):
    dirs = [(1, 1), (1, -1), (-1, 1), (-1, -1)]
    return [move(p[2 * i:2 * i + 2], 0.8 * dx, 0.8 * dy, 2) for i, (dx, dy) in enumerate(dirs)]


def arrows_move(p):
    dirs = [(1, 0), (-1, 0), (0, 1), (0, -1)]
    return [move(p[2 * i:2 * i + 2], 0.8 * dx, 0.8 * dy, 2) for i, (dx, dy) in enumerate(dirs)]


def arrows_shuffle(p):
    o = [0, 0.8]
    a = morph([f'M3 7h3a5 5 0 0 1 5 5a5 5 0 0 0 5 5h{fmt(5 + x)}' for x in o], 2)
    b = morph([f'M{fmt(21 + x)} 7h{fmt(-5 - x)}a4.978 4.978 0 0 0 -3 1m-4 8a4.984 4.984 0 0 1 -3 1h-3' for x in o], 2)
    return [move([p[0]], 0.8, 0, 2), move([p[1]], 0.8, 0, 2), a, b]


def arrows_sort(p):
    return [wmove([p[0]], 0, -1, 2, 0, 0.5), wmove([p[1]], 0, 1, 2, 0.5, 0.5)]


def arrows_split(p):
    o = [0, 0.8]
    a = morph([f'M{fmt(21 + x)} 17h{fmt(-8 - x)}l-3.5 -5h-6.5' for x in o], 2)
    b = morph([f'M{fmt(21 + x)} 7h{fmt(-8 - x)}l-3.495 5' for x in o], 2)
    return [a, b, move([p[2]], 0.8, 0, 2), move([p[3]], 0.8, 0, 2)]


def arrows_up_down(p):
    return [wmove(p[0:2], 0, -1, 2, 0, 0.5), wmove(p[2:4], 0, 1, 2, 0.5, 0.5)]


def article(p):
    lines = [wmorph(f'M7 {y}h10', f'M7 {y}h5', 3, 0.2 * i, 0.6) for i, y in enumerate((8, 12, 16))]
    return [p[0]] + lines


def award(p):
    return [rock(p, 12, 3, 6, 3)]


def axe(p):
    c = '19 18.5'
    return [group(p, osc('transform', [f'0 {c}', f'-12 {c}'], 1.8, kind='rotate'))]


def baby_bottle(p):
    return [rock(p, 12, 22, 6, 3)]


def baby_carriage(p):
    return [p[0], p[1], hub(8, 19, 0.8, 1.5), hub(18, 19, 0.8, 1.5), move(p[2:6], 0, -0.4, 1.5)]


def backpack(p):
    return [move(p, 0, -0.8, 1.6)]


def backspace(p):
    return [p[0], turn([p[1]], 14, 12, 4)]


def ball_spin(dur):
    return lambda p: [turn(p, 12, 12, dur)]


def balloon(p):
    inner = rock(p, 12, 8, 4, 4)
    return [move([inner], 0, -0.8, 2)]


def ban(p):
    return [grow(p, 12, 12, 0.06, 3)]


def barbell(p):
    return [move(p, 0, -1.2, 2)]


def basket(p):
    return [rock(p, 12, 20, 4, 3)]


def bath(p):
    drop = wrise('<path stroke-width="1.8" d="M11 6.5v.01"/>', 0, 4.5, 1.5, 0, 1)
    return p + [drop]


def battery_charging(p):
    return [p[0], p[1], grow([p[2]], 11.5, 12, 0.15, 1.6)]


def beach(p):
    return [p[0], rock(p[1:4], 12, 14.196, 5, 3), move([p[4]], 1, 0, 2)]


def bed(p):
    return [grow([p[0]], 7, 9, 0.12, 3)] + p[1:]


def beer(p):
    bubbles = [wrise(f'<circle cx="{x}" cy="17" r="0.6"/>', 0, -7, 2, b, 0.5) for x, b in ((10.5, 0), (13.5, 0.5))]
    return p + bubbles


def bell(p):
    return [rock([p[0]], 12, 3, 10, 2), rock([p[1]], 12, 3, 16, 2)]


def bell_off(p):
    return [rock(p[0:2], 12, 3, 6, 3), p[2]]


def bell_ringing(p):
    return [rock(p[0:2], 12, 3, 10, 2), wmove([p[2]], 0.5, -0.5, 2, 0, 0.5), wmove([p[3]], -0.5, -0.5, 2, 0.5, 0.5)]


def bike(p):
    return [p[0], p[1], hub(5, 18, 1.8, 1.5), hub(19, 18, 1.8, 1.5), p[2], move([p[3]], 0, -0.4, 1.5)]


def binoculars(p):
    return [move(p, 0.8, 0, 3)]


def bold(p):
    return [group(p, '<animate attributeName="stroke-width" values="1;1.6;1" keyTimes="0;0.5;1" calcMode="spline" '
                     f'keySplines="{EASE};{EASE}" dur="2s" repeatCount="indefinite"/>', attrs='stroke-width="1"')]


def bolt(p):
    return [group(p, '<animate attributeName="opacity" values="1;0.35;1;1" keyTimes="0;0.08;0.16;1" dur="2.5s" repeatCount="indefinite"/>')]


def bomb(p):
    spark = fade('<path stroke-width="1.6" d="M21 3v.01"/>', 0.8, peak=0.4)
    return [rock(p, 10, 14, 3, 1.6), spark]


def bone(p):
    return [rock(p, 12, 12, 10, 2)]


def book(p):
    page = '<path d="M12 6a9 9 0 0 1 9 0v13a9 9 0 0 0 -9 0"/>'
    inner = group([page], attrs='transform="translate(-12 0)"')
    flip = group([inner], f'<animateTransform attributeName="transform" type="scale" values="1 1;-1 1;-1 1" keyTimes="0;0.5;1" '
                          f'calcMode="spline" keySplines="{EASE};0 0 1 1" dur="2.4s" repeatCount="indefinite"/>')
    show = '<animate attributeName="opacity" values="0;1;1;0;0" keyTimes="0;0.02;0.48;0.5;1" dur="2.4s" repeatCount="indefinite"/>'
    return p + [group([group([flip], attrs='transform="translate(12 0)"')], show, attrs='opacity="0"')]


def book_2(p):
    return [rock(p, 12, 20, 4, 3)]


def bookmark(p):
    return [morph(['M18 7v14l-6 -4l-6 4v-14a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4',
                   'M18 7v14l-6 -2.5l-6 2.5v-14a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4'], 2)]


def books(p):
    return p[0:4] + [rock(p[4:7], 18.5, 20, -6, 3, one_way=True)]


def boost(p):
    dim = lambda el, start: group([el], win('opacity', '1', ['0.2'], 1.6, start, 0.5))
    return [move([p[0]], 0, -0.8, 1.6), dim(p[1], 0), dim(p[2], 0.5)]


def bottle(p):
    return [rock(p, 12, 21, 5, 3)]


def bowl(p):
    steam = [wrise(f'<path d="M{x} 6c-.5 -1 .5 -2 0 -3"/>', 0, -1.5, 2.4, b, 0.5) for x, b in ((10, 0), (14, 0.5))]
    return p + steam


def bowling(p):
    return [rock(p[0:3], 11, 14, 20, 3), p[3], rock(p[4:6], 18, 22, 6, 2)]


def box(p):
    return [move(p, 0, -0.8, 1.6)]


ICONS = {
    'address-book': address_book, 'affiliate': affiliate, 'alert-triangle': alert_triangle,
    'alphabet-latin': alphabet_latin, 'anchor': anchor, 'app-window': app_window, 'apple': apple,
    'archive': archive, 'armchair': armchair, 'armchair-2': armchair_2, 'arrow-back-up': arrow_back_up,
    'arrow-forward-up': arrow_forward_up, 'arrow-left': arrow_left, 'arrow-right': arrow_right,
    'arrow-up': arrow_up, 'arrows-maximize': arrows_maximize, 'arrows-minimize': arrows_minimize,
    'arrows-move': arrows_move, 'arrows-shuffle': arrows_shuffle, 'arrows-sort': arrows_sort,
    'arrows-split': arrows_split, 'arrows-up-down': arrows_up_down, 'article': article, 'award': award,
    'axe': axe, 'baby-bottle': baby_bottle, 'baby-carriage': baby_carriage, 'backpack': backpack,
    'backspace': backspace, 'ball-baseball': ball_spin(8), 'ball-basketball': ball_spin(8),
    'ball-bowling': ball_spin(4), 'ball-football': ball_spin(8), 'ball-tennis': ball_spin(6),
    'ball-volleyball': ball_spin(8), 'balloon': balloon, 'ban': ban, 'barbell': barbell, 'basket': basket,
    'bath': bath, 'battery-charging': battery_charging, 'beach': beach, 'bed': bed, 'beer': beer,
    'bell': bell, 'bell-off': bell_off, 'bell-ringing': bell_ringing, 'bike': bike, 'binoculars': binoculars,
    'bold': bold, 'bolt': bolt, 'bomb': bomb, 'bone': bone, 'book': book, 'book-2': book_2,
    'bookmark': bookmark, 'books': books, 'boost': boost, 'bottle': bottle, 'bowl': bowl,
    'bowling': bowling, 'box': box,
}

if __name__ == '__main__':
    only = sys.argv[1:]
    for label, fn in ICONS.items():
        if not only or label in only:
            build(label, fn)
    print('wrote', len(only) if only else len(ICONS))

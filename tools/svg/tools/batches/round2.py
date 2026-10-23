"""New icons, round 2: values across the enums whose icon stood in for something else (notes.md, New Icons).

Usage: python3 round2.py [labels...]   (default: all of these). Tabler style on the 24 grid. A figure's head is the
flame; its static element is marked `data-head`, which the small variant turns into the thinner head stroke.
"""
import math
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import SVG_DIR, EASE, fmt, group, osc, flame_head
from forge import header, write, keys, p, FRAME
from rig import curve, morph, slide

PACKS = {}
SMALL_HEAD = 1.5


def pack(label):
    def deco(fn):
        PACKS[label] = fn
        return fn
    return deco


def build(label):
    static, animated = PACKS[label]()
    for v, w in (('small', 2), ('large', 1), ('very-large', 0.5)):
        head = f'<path stroke-width="{SMALL_HEAD}" ' if v == 'small' else '<path '
        els = [e.replace('<path data-head ', head).replace('<path data-thin ', f'<path stroke-width="{fmt(w / 2)}" ') for e in static]
        write(f'{SVG_DIR}/{label}-{v}.svg', header(label, w), [FRAME] + els)
    els = [e.replace('<path data-head ', '<path ').replace('<path data-thin ', '<path stroke-width="0.5" ') for e in animated]
    write(f'{SVG_DIR}/{label}-animated.svg', header(label, 1, 'animated'), [FRAME] + els)


def head_el(d):
    return f'<path data-head d="{d}"/>'


def spline_keys(attr, pairs, dur, splines, kind=None):
    """An animation through (time, value) [pairs] with a keySpline per segment."""
    tag = f'animateTransform attributeName="transform" type="{kind}"' if kind else f'animate attributeName="{attr}"'
    return (f'<{tag} values="{";".join(v for _, v in pairs)}" keyTimes="{";".join(fmt(t) for t, _ in pairs)}" '
            f'dur="{fmt(dur)}s" repeatCount="indefinite" calcMode="spline" keySplines="{";".join(splines)}"/>')


def animated_path(d, anim):
    return f'<path d="{d}">\n    {anim}\n</path>'


# ---------- party-hat: a striped cone with a pompom; it hops and wiggles as confetti spins ----------
HAT = 'M6.5 19L12 4.6L17.5 19'
HAT_BRIM = 'M6.5 19Q12 21.4 17.5 19'
HAT_BANDS = 'M10.1 9.6Q12 10.4 13.9 9.6M8.4 14.2Q12 15.7 15.6 14.2'
POMPOM = 'M10.7 3.4a1.3 1.3 0 1 0 2.6 0a1.3 1.3 0 1 0 -2.6 0'
CONFETTI = [(4, 6.5, 30), (19.8, 5.5, -40), (3.5, 13, -20), (20.5, 12.5, 50)]


def bit(x, y, deg):
    a = math.radians(deg)
    dx, dy = 0.8 * math.cos(a), 0.8 * math.sin(a)
    return f'M{fmt(x - dx)} {fmt(y - dy)}L{fmt(x + dx)} {fmt(y + dy)}'


@pack('party-hat')
def party_hat():
    hat = [p(HAT), p(HAT_BRIM), p(HAT_BANDS), p(POMPOM)]
    bits = [p(bit(*c)) for c in CONFETTI]
    hop = keys('transform', [(0, '0 0'), (0.15, '0 -1'), (0.3, '0 0'), (0.45, '0 -0.6'), (0.6, '0 0'), (1, '0 0')], 2.4, kind='translate')
    wiggle = keys('transform', [(0, '0 12 19'), (0.15, '-5 12 19'), (0.3, '0 12 19'), (0.45, '4 12 19'), (0.6, '0 12 19'), (1, '0 12 19')], 2.4, kind='rotate')
    spun = []
    for i, (x, y, deg) in enumerate(CONFETTI):
        c = f'{fmt(x)} {fmt(y)}'
        a, b = (0, 360) if i % 2 == 0 else (360, 0)
        spun.append(group([p(bit(x, y, deg))], f'<animateTransform attributeName="transform" type="rotate" from="{a} {c}" to="{b} {c}" dur="{2.4 if i < 2 else 1.2}s" repeatCount="indefinite"/>'))
    return hat + bits, [group([group(hat, wiggle)], hop)] + spun


# ---------- billiards: a cue lined up behind the cue ball, the eight ball beyond, on the rail ----------
AIM = (0.8, 0.6)
BALL_R = 2.8
CUE_BALL = (9.6, 11.6)
EIGHT_BALL = (CUE_BALL[0] + AIM[0] * (2 * BALL_R + 2.2), CUE_BALL[1] + AIM[1] * (2 * BALL_R + 2.2))
TIP = (CUE_BALL[0] - AIM[0] * (BALL_R + 0.8), CUE_BALL[1] - AIM[1] * (BALL_R + 0.8))
RAIL = ''


def ball(c):
    return f'M{fmt(c[0] - BALL_R)} {fmt(c[1])}a{BALL_R} {BALL_R} 0 1 0 {fmt(2 * BALL_R)} 0a{BALL_R} {BALL_R} 0 1 0 {fmt(-2 * BALL_R)} 0'


def along(d):
    """An offset of [d] along the aim, as 'dx dy'."""
    return f'{fmt(AIM[0] * d)} {fmt(AIM[1] * d)}'


EIGHT_SPOT = f'M{fmt(EIGHT_BALL[0] - 1.6)} {fmt(EIGHT_BALL[1] - 0.6)}a1 1 0 1 0 2 0a1 1 0 1 0 -2 0'
CUE = f'M{fmt(TIP[0] - AIM[0] * 4.6)} {fmt(TIP[1] - AIM[1] * 4.6)}L{fmt(TIP[0])} {fmt(TIP[1])}'


@pack('billiards')
def billiards():
    static = [p(CUE), p(ball(CUE_BALL)), p(ball(EIGHT_BALL)), p(EIGHT_SPOT)]
    dur = 3.2
    # two practice strokes, then the strike: the cue ball kisses the eight, which rolls on a little
    cue = keys('transform', [(0, '0 0'), (0.1, along(-1.1)), (0.2, '0 0'), (0.3, along(-1.1)), (0.4, '0 0'), (0.5, along(-1.8)),
                             (0.56, along(0.6)), (0.66, '0 0'), (1, '0 0')], dur, kind='translate')
    roll = keys('transform', [(0, '0 0'), (0.56, '0 0'), (0.64, along(2.2)), (0.84, along(2.2)), (0.86, '0 0'), (1, '0 0')], dur, kind='translate')
    roll8 = keys('transform', [(0, '0 0'), (0.64, '0 0'), (0.76, along(0.9)), (0.84, along(0.9)), (0.86, '0 0'), (1, '0 0')], dur, kind='translate')
    spin8 = keys('transform', [(0, f'0 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}'), (0.64, f'0 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}'),
                               (0.76, f'40 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}'), (0.84, f'40 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}'),
                               (0.86, f'0 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}'), (1, f'0 {fmt(EIGHT_BALL[0])} {fmt(EIGHT_BALL[1])}')], dur, kind='rotate')
    # the balls are set back while out of sight: they fade out, return, and fade in
    fade = '<animate attributeName="opacity" values="1;1;0;0;1;1" keyTimes="0;0.8;0.84;0.88;0.94;1" dur="3.2s" repeatCount="indefinite"/>'
    balls = group([group([p(ball(CUE_BALL))], roll), group([group([p(ball(EIGHT_BALL)), p(EIGHT_SPOT)], spin8)], roll8)], fade)
    return static, [group([p(CUE)], cue), balls]


# ---------- shower: a head on a curved arm; water falls in dashes ----------
SHOWER_ARM = 'M5 21V7.5a4 4 0 0 1 4 -4h2.5a4 4 0 0 1 4 4v.5'
SHOWER_HEAD = 'M11.5 11a4 4 0 0 1 8 0z'
STREAMS = 'M12.5 13.5v7.5M15.5 13.5v7.5M18.5 13.5v7.5'


@pack('shower')
def shower():
    water = '<path d="{}" stroke-dasharray="1.5 2"{}/>'
    static = [p(SHOWER_ARM), p(SHOWER_HEAD), water.format(STREAMS, '')]
    flow = '<animate attributeName="stroke-dashoffset" from="0" to="-3.5" dur="0.6s" repeatCount="indefinite"/>'
    return static, [p(SHOWER_ARM), p(SHOWER_HEAD), f'<path d="{STREAMS}" stroke-dasharray="1.5 2">\n    {flow}\n</path>']


# ---------- taxi: Tabler's car with a roof sign; wheels turn and the body rides ----------
CAR_WHEELS = ['M5 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0', 'M15 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0']
CAR_BODY = 'M5 17h-2v-6l2 -5h9l4 5h1a2 2 0 0 1 2 2v4h-2m-4 0h-6m-6 -6h15m-6 0v-5'
TAXI_SIGN = 'M7.5 6v-1.8h4v1.8'


@pack('taxi')
def taxi():
    static = [p(d) for d in CAR_WHEELS] + [p(CAR_BODY), p(TAXI_SIGN)]
    hubs = [group([p(f'M{x - 0.8} 17h1.6')], f'<animateTransform attributeName="transform" type="rotate" from="0 {x} 17" to="360 {x} 17" dur="1.5s" repeatCount="indefinite"/>') for x in (7, 17)]
    ride = osc('transform', ['0 0', '0 -0.5'], 1.2, kind='translate')
    return static, [p(d) for d in CAR_WHEELS] + hubs + [group([p(CAR_BODY), p(TAXI_SIGN)], ride)]


# ---------- subway: a train front in the tunnel mouth; it sways on the rails, headlights glowing ----------
TUNNEL = 'M3 21v-9a9 9 0 0 1 18 0v9'
TRAIN = 'M7.5 10.5a2 2 0 0 1 2 -2h5a2 2 0 0 1 2 2v7.5a1 1 0 0 1 -1 1h-7a1 1 0 0 1 -1 -1z'
WINDSHIELD = 'M9 10.5h6v3.5h-6z'
LIGHTS = [(9.6, 16.3), (14.4, 16.3)]
RAILS = 'M9 19l-2 2m8 -2l2 2'


def light(x, y, r=0.6):
    return f'M{fmt(x - r)} {fmt(y)}a{fmt(r)} {fmt(r)} 0 1 0 {fmt(2 * r)} 0a{fmt(r)} {fmt(r)} 0 1 0 {fmt(-2 * r)} 0'


@pack('subway')
def subway():
    front = [p(TRAIN), p(WINDSHIELD)]
    static = [p(TUNNEL), p(RAILS)] + front + [p(light(*l)) for l in LIGHTS]
    glow = [f'<path d="{light(*l)}">\n    {osc("d", [light(*l), light(*l, 0.95)], 1.6)}\n</path>' for l in LIGHTS]
    sway = osc('transform', ['0 12 19', '1.5 12 19', '0 12 19', '-1.5 12 19'], 1.6, kind='rotate')
    return static, [p(TUNNEL), p(RAILS), group(front + glow, sway)]


# ---------- pier: a deck on pilings over the water; the water drifts beneath ----------
PIER_DECK = 'M2 8.5h17v2h-17'
PIER_POSTS = 'M5 10.5v8m5 -8v8m5 -8v8'
PIER_RAIL = 'M17.5 8.5v-4.5m-1 0h2l-.3 -1.4h-1.4z'
WAVE_LEN = 5


def waves(x0, x1, y):
    d = f'M{fmt(x0)} {fmt(y)}'
    x = x0
    while x < x1 - 1e-6:
        d += f'q1.25 -1 2.5 0t2.5 0'
        x += WAVE_LEN
    return d


@pack('pier')
def pier():
    water = [waves(2, 22, 15), waves(2, 22, 19)]
    static = [p(PIER_RAIL), p(PIER_POSTS), p(PIER_DECK)] + [p(w) for w in water]
    clip = '<clipPath id="pier-water">\n    <path d="M2 12h20v10h-20z"/>\n</clipPath>'
    # each row is drawn a wavelength long and slid by one wavelength, so frame 0 matches the static waves
    drifts = [group([p(waves(2 - WAVE_LEN, 22, 15))], f'<animateTransform attributeName="transform" type="translate" values="0 0;{WAVE_LEN} 0" dur="3s" repeatCount="indefinite"/>'),
              group([p(waves(2, 22 + WAVE_LEN, 19))], f'<animateTransform attributeName="transform" type="translate" values="0 0;{-WAVE_LEN} 0" dur="4s" repeatCount="indefinite"/>')]
    water_group = f'{clip}\n<g clip-path="url(#pier-water)">\n' + '\n'.join('    ' + l for d in drifts for l in d.split('\n')) + '\n</g>'
    return static, [p(PIER_RAIL), p(PIER_POSTS), p(PIER_DECK), water_group]


# ---------- drive-in: a big screen on its legs, a car parked before it; on screen the sun goes down over the hills ----------
SCREEN = 'M2.5 4a1 1 0 0 1 1 -1h17a1 1 0 0 1 1 1v8a1 1 0 0 1 -1 1h-17a1 1 0 0 1 -1 -1z'
SCREEN_LEGS = 'M6 13v2.5m12 -2.5v2.5'
HILLS = 'M3.5 11.5l4 -3.5l3 2.5l3.5 -4l5.5 5'
CAR_BACK = 'M6.5 21v-3a1 1 0 0 1 1 -1h9a1 1 0 0 1 1 1v3m-11 0h11m-9.5 -4l1.2 -2.5h5.6l1.2 2.5m-8 2h1.5m5 0h1.5'


def sun(y):
    return f'M16.3 {fmt(y)}a1.3 1.3 0 1 0 2.6 0a1.3 1.3 0 1 0 -2.6 0'


@pack('drive-in')
def drive_in():
    static = [p(SCREEN), p(SCREEN_LEGS), p(HILLS), p(sun(6)), p(CAR_BACK)]
    clip = '<clipPath id="drive-in-screen">\n    <path d="M3 3.5h18v8l-2 0l-5.5 -5l-3.5 4l-3 -2.5l-4 3.5h-0z"/>\n</clipPath>'
    setting = keys('transform', [(0, '0 0'), (0.45, '0 4.5'), (0.55, '0 4.5'), (1, '0 0')], 6, kind='translate')
    film = f'{clip}\n<g clip-path="url(#drive-in-screen)">\n' + '\n'.join('    ' + l for l in group([p(sun(6))], setting).split('\n')) + '\n</g>'
    return static, [p(SCREEN), p(SCREEN_LEGS), film, p(HILLS), p(CAR_BACK)]


# ---------- figures: frames of joint positions, drawn as smooth chains, the flame head riding with a translate ----------
def figure_frames(frames, chains, head, dur, times=None, splines=None, linear=False, thin=()):
    """Static and animated elements for [frames] (each a dict of joint -> (x, y), plus 'head' for the head's anchor).
    [chains] are (joint names, softness); [head] is the flame's (cx, cy, r) in frame 0."""
    draw = lambda f: [curve([f[j] for j in names], soft) for names, soft in chains]
    paths = [draw(f) for f in frames]
    hx0, hy0 = frames[0]['head']
    offsets = [(f['head'][0] - hx0, f['head'][1] - hy0) for f in frames]
    head_d = flame_head(*head)
    tag = lambda i: '<path data-thin ' if i in thin else '<path '
    static = [f'{tag(i)}d="{d}"/>' for i, d in enumerate(paths[0])] + [head_el(head_d)]

    def anim(values, kind=None):
        if times is None:
            return morph(values, dur, linear=linear) if kind is None else slide(values, dur, linear=linear)
        vals = list(values)
        tag = f'animateTransform attributeName="transform" type="translate"' if kind else 'animate attributeName="d"'
        vs = ';'.join(f'{fmt(x)} {fmt(y)}' for x, y in vals) if kind else ';'.join(vals)
        return (f'<{tag} values="{vs}" keyTimes="{";".join(fmt(t) for t in times)}" dur="{fmt(dur)}s" repeatCount="indefinite" '
                f'calcMode="spline" keySplines="{";".join(splines)}"/>')
    animated = [animated_path(d, anim([fr[i] for fr in paths])).replace('<path ', tag(i), 1) for i, d in enumerate(paths[0])]
    animated.append(group([p(head_d)], anim(offsets, kind='translate')))
    return static, animated


def lerp(a, b, t):
    return a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t


def shift(pose, dy):
    return {k: (v[0], v[1] + dy) for k, v in pose.items()}


# ---------- trampoline: a jumper at the top of the bounce; falls, sinks the mat, springs back up ----------
FALL, RISE, EASE_S = '0.33 0 0.67 0.33', '0.33 0.67 0.67 1', EASE
MAT_LEGS = 'M6 18.1V21M18 18.1V21'


def mat(sag):
    return f'M4 17.2C{fmt(7)} {fmt(16 + sag * 2.6)} {fmt(17)} {fmt(16 + sag * 2.6)} 20 17.2C20 18.4 4 18.4 4 17.2'


def jumper(knee, arms, dy):
    """A frontal jumper with feet on the mat line plus [dy]; [knee] 0 straight to 1 bent, [arms] 1 up to 0 down."""
    feet_y = 17.2 + dy
    hip = (12, 13.2 + dy + knee * 1.1)
    pose = {'footL': (10.6, feet_y), 'footR': (13.4, feet_y), 'hip': hip,
            'kneeL': (11.2 - knee * 1.2, (feet_y + hip[1]) / 2), 'kneeR': (12.8 + knee * 1.2, (feet_y + hip[1]) / 2)}
    neck = (12, hip[1] - 3.6)
    pose.update({'mid': (12, hip[1] - 1.8), 'neck': neck})
    up, down = ((7.4, neck[1] - 0.9), (16.6, neck[1] - 0.9)), ((8.8, neck[1] + 3.2), (15.2, neck[1] + 3.2))
    pose['handL'], pose['handR'] = lerp(down[0], up[0], arms), lerp(down[1], up[1], arms)
    pose['elbowL'] = lerp(neck, pose['handL'], 0.5)
    pose['elbowL'] = (pose['elbowL'][0] - 0.3, pose['elbowL'][1] + 0.4)
    pose['elbowR'] = lerp(neck, pose['handR'], 0.5)
    pose['elbowR'] = (pose['elbowR'][0] + 0.3, pose['elbowR'][1] + 0.4)
    pose['head'] = neck
    return pose


@pack('trampoline')
def trampoline():
    apex, touch, low = -2.4, 0, 0.8
    frames = [jumper(0, 1, apex), jumper(0.1, 0.5, touch), jumper(1, 0, low), jumper(0.1, 0.5, touch), jumper(0, 1, apex)]
    times = [0, 0.42, 0.5, 0.58, 1]
    splines = [FALL, EASE_S, EASE_S, RISE]
    chains = [(['footL', 'kneeL', 'hip', 'kneeR', 'footR'], 0.25), (['hip', 'mid', 'neck'], 0.3),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], 0.3)]
    neck0 = frames[0]['neck']
    head = (12, neck0[1] - 2.0, 1.7)
    static, animated = figure_frames(frames, chains, head, 1.6, times, splines)
    sags = [0, 0, 1, 0, 0]
    mat_anim = (f'<animate attributeName="d" values="{";".join(mat(s) for s in sags)}" keyTimes="{";".join(fmt(t) for t in times)}" '
                f'dur="1.6s" repeatCount="indefinite" calcMode="spline" keySplines="{";".join(splines)}"/>')
    return [p(mat(0)), p(MAT_LEGS)] + static, [animated_path(mat(0), mat_anim), p(MAT_LEGS)] + animated


# ---------- marionette: a control bar over a puppet on three strings; the bar rocks and the puppet dances ----------
BAR_C, BAR_HALF = (12, 2.8), 3.6


def puppet(tilt):
    """Joints for the bar tilted [tilt] degrees: each hand hangs from a bar end and the head from the middle; the leg
    under the rising hand kicks up."""
    a = math.radians(tilt)
    endL = (BAR_C[0] - BAR_HALF * math.cos(a), BAR_C[1] - BAR_HALF * math.sin(a))
    endR = (BAR_C[0] + BAR_HALF * math.cos(a), BAR_C[1] + BAR_HALF * math.sin(a))
    neck, hip = (12, 13.2), (12, 16.8)
    handL, handR = (endL[0] - 1.2, endL[1] + 8.6), (endR[0] + 1.2, endR[1] + 8.6)
    liftL, liftR = max(0, endR[1] - endL[1]) / 2, max(0, endL[1] - endR[1]) / 2
    kneeL, kneeR = (11.2 - liftL * 0.8, 19.1 - liftL), (12.8 + liftR * 0.8, 19.1 - liftR)
    pose = {'neck': neck, 'hip': hip, 'mid': (12, 15), 'handL': handL, 'handR': handR, 'kneeL': kneeL, 'kneeR': kneeR,
            'footL': (kneeL[0] - 0.2 + liftL * 0.3, kneeL[1] + 2.4), 'footR': (kneeR[0] + 0.2 - liftR * 0.3, kneeR[1] + 2.4),
            'endL': endL, 'endR': endR, 'top': BAR_C, 'string': (12, 8.6), 'head': neck,
            'crossT': (BAR_C[0] + 1.6 * math.sin(a), BAR_C[1] - 1.6 * math.cos(a)), 'crossB': (BAR_C[0] - 1.2 * math.sin(a), BAR_C[1] + 1.2 * math.cos(a))}
    pose['elbowL'] = (lerp(neck, handL, 0.5)[0] - 0.9, max(handL[1], neck[1]) + 1.2)
    pose['elbowR'] = (lerp(neck, handR, 0.5)[0] + 0.9, max(handR[1], neck[1]) + 1.2)
    return pose


@pack('marionette')
def marionette():
    n, dur = 8, 2.4
    frames = [puppet(13 * math.sin(2 * math.pi * k / n)) for k in range(n)]
    chains = [(['hip', 'mid', 'neck'], 0.3), (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], 0.3),
              (['footL', 'kneeL', 'hip', 'kneeR', 'footR'], 0.25),
              (['endL', 'endR'], 0), (['crossT', 'crossB'], 0), (['endL', 'handL'], 0), (['endR', 'handR'], 0), (['top', 'string'], 0)]
    head = (12, 11.4, 1.5)
    return figure_frames(frames, chains, head, dur, linear=True, thin=(5, 6, 7))


# ---------- tightrope: a walker on a sagging rope with a long pole; the body sways and the pole answers ----------
ROPE = 'M3 13.8Q12 17.4 21 13.8M3 21V13.8M21 21V13.8'
FEET = (12, 15.6)


def walker(theta):
    """Joints with the body leaning [theta] degrees about the feet and the pole tilted against it."""
    a = math.radians(theta)
    def rot(x, y, c=FEET, ang=a):
        dx, dy = x - c[0], y - c[1]
        return c[0] + dx * math.cos(ang) - dy * math.sin(ang), c[1] + dx * math.sin(ang) + dy * math.cos(ang)
    pose = {k: rot(*v) for k, v in {'footL': (11.3, 15.6), 'footR': (12.7, 15.6), 'kneeL': (11.5, 13.3), 'kneeR': (12.5, 13.3),
                                    'hip': (12, 11.1), 'mid': (12, 9.3), 'neck': (12, 7.4)}.items()}
    grip = rot(12, 9.6)
    b = math.radians(-1.8 * theta)
    hands = [(grip[0] + d * math.cos(b), grip[1] + d * math.sin(b)) for d in (-3.6, 3.6)]
    pole = [(grip[0] + d * math.cos(b), grip[1] + d * math.sin(b)) for d in (-9.5, 0, 9.5)]
    droop = [(pole[0][0], pole[0][1] + 0.6), pole[1], (pole[2][0], pole[2][1] + 0.6)]
    pose.update({'handL': hands[0], 'handR': hands[1], 'poleL': droop[0], 'poleM': droop[1], 'poleR': droop[2],
                 'elbowL': lerp(pose['neck'], hands[0], 0.55), 'elbowR': lerp(pose['neck'], hands[1], 0.55)})
    pose['elbowL'] = (pose['elbowL'][0], pose['elbowL'][1] + 0.9)
    pose['elbowR'] = (pose['elbowR'][0], pose['elbowR'][1] + 0.9)
    pose['head'] = pose['neck']
    return pose


@pack('tightrope')
def tightrope():
    n, dur = 8, 3.2
    frames = [walker(5 * math.sin(2 * math.pi * k / n)) for k in range(n)]
    chains = [(['footL', 'kneeL', 'hip', 'kneeR', 'footR'], 0.2), (['hip', 'mid', 'neck'], 0.3),
              (['handL', 'elbowL', 'neck', 'elbowR', 'handR'], 0.3), (['poleL', 'poleM', 'poleR'], 0.5)]
    head = (12, 5.4, 1.7)
    static, animated = figure_frames(frames, chains, head, dur, linear=True)
    return [p(ROPE)] + static, [p(ROPE)] + animated


if __name__ == '__main__':
    for label in sys.argv[1:] or PACKS:
        build(label)
        print('built', label)

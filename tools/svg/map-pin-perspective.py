"""Generates map-pin-animated.svg: each shape turns half way around the vertical axis at x=12, projected with perspective."""
import math
import sys

CAMERA = 24.0          # distance from the camera to the axis, in viewBox units; smaller is stronger perspective
AXIS_X, CENTER_Y = 12.0, 12.0
FRAMES = 24            # keyframes per half turn
DUR = 3.75
OUTER_SPAN = (0.0667, 0.2333)
INNER_SPAN = (0.3, 0.4667)


def arc_beziers(cx, cy, r, start, end, segments):
    """Cubic segments of the arc from angle start to end (radians, y down), as lists of 3 control points."""
    out = []
    step = (end - start) / segments
    k = 4 / 3 * math.tan(step / 4)
    for i in range(segments):
        a0, a1 = start + i * step, start + (i + 1) * step
        p0 = (cx + r * math.cos(a0), cy + r * math.sin(a0))
        p3 = (cx + r * math.cos(a1), cy + r * math.sin(a1))
        c1 = (p0[0] - k * r * math.sin(a0), p0[1] + k * r * math.cos(a0))
        c2 = (p3[0] + k * r * math.sin(a1), p3[1] - k * r * math.cos(a1))
        out.append([c1, c2, p3])
    return out


d45 = math.radians(45)
# outer pin: start at the right shoulder, line to the tip, round tip, line up, big arc over the top back to the start
tip = (12.0, 19.486)
outer_start = (12 + 8 * math.cos(d45), 11 + 8 * math.sin(d45))
outer = [("M", [outer_start]),
         ("L", [(tip[0] + 2 * math.cos(d45), tip[1] + 2 * math.sin(d45))])]
outer += [("C", seg) for seg in arc_beziers(tip[0], tip[1], 2, d45, 3 * d45, 2)]
outer += [("L", [(12 - 8 * math.cos(d45), 11 + 8 * math.sin(d45))])]
outer += [("C", seg) for seg in arc_beziers(12, 11, 8, 3 * d45, 9 * d45, 6)]

inner_start = (15.0, 11.0)
inner = [("M", [inner_start])] + [("C", seg) for seg in arc_beziers(12, 11, 3, 0, 2 * math.pi, 4)] + [("Z", [])]


def project(point, theta):
    x, y = point
    dx = x - AXIS_X
    z = dx * math.sin(theta)
    scale = CAMERA / (CAMERA + z)
    return AXIS_X + dx * math.cos(theta) * scale, CENTER_Y + (y - CENTER_Y) * scale


def path_at(commands, theta):
    parts = []
    for cmd, points in commands:
        coords = " ".join(f"{px:.3f} {py:.3f}" for px, py in (project(p, theta) for p in points))
        parts.append(f"{cmd}{coords}")
    return "".join(parts).replace("-0.000", "0.000")


def animate(commands, span):
    start, end = span
    thetas = [math.pi * (1 - math.cos(math.pi * i / FRAMES)) / 2 for i in range(FRAMES + 1)]
    frames = [path_at(commands, t) for t in thetas]
    values = [frames[0]] + frames + [frames[-1]]
    times = [0.0] + [start + (end - start) * i / FRAMES for i in range(FRAMES + 1)] + [1.0]
    return (f'<animate attributeName="d" dur="{DUR}s" repeatCount="indefinite"\n'
            f'                     keyTimes="{";".join(f"{t:.4f}" for t in times)}"\n'
            f'                     values="{";".join(values)}"/>'), frames[0]


outer_anim, outer_d = animate(outer, OUTER_SPAN)
inner_anim, inner_d = animate(inner, INNER_SPAN)

sys.stdout.write(f'''<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
     stroke-width="1" stroke-linecap="round" stroke-linejoin="round"
     class="icon icon-tabler icons-tabler-animated icon-tabler-map-pin">
    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
    <path d="{outer_d}">
        {outer_anim}
    </path>
    <path d="{inner_d}">
        {inner_anim}
    </path>
</svg>
''')

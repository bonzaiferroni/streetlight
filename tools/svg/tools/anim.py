"""Helpers for writing animated SVG variants.

Usage in a per-icon script:

    from anim import *
    head, els = load('clock')          # head: the <svg ...> open tag of the -large file; els: child element strings
    body = [els[0], ...]               # build the new body
    save('clock', head, body)

Conventions: SMIL only, frame 0 equals the -large icon, loops indefinite and seamless.
"""
import math
import os
import re

SVG_DIR = '/home/starfox/projects/streetlight/www/icon'
EASE = '0.42 0 0.58 1'  # ease-in-out cubic


def load(label):
    """Returns the open tag and the child elements of the -large file for [label]."""
    src = open(f'{SVG_DIR}/{label}-large.svg').read()
    m = re.search(r'(<svg\b[^>]*>)(.*)</svg>', src, re.S)
    head = m.group(1)
    els = re.findall(r'<[a-z]+\b[^>]*/>', m.group(2))
    return head, els


def save(label, head, body):
    """Writes [label]-animated.svg from the open tag and body lines (strings, may be nested markup)."""
    head = head.replace('icons-tabler-outline', 'icons-tabler-animated')
    lines = []
    for b in body:
        for ln in b.split('\n'):
            lines.append('    ' + ln)
    open(f'{SVG_DIR}/{label}-animated.svg', 'w').write(head + '\n' + '\n'.join(lines) + '\n</svg>\n')


def fmt(x):
    """A compact number."""
    s = f'{x:.3f}'.rstrip('0').rstrip('.')
    return '0' if s in ('-0', '') else s


def splines(n):
    """keySplines for n segments, all ease-in-out."""
    return ';'.join([EASE] * n)


def times(n):
    """Evenly spaced keyTimes for n values."""
    return ';'.join(fmt(i / (n - 1)) for i in range(n))


def osc(attr, values, dur, kind=None, ease=True, begin=None, extra=''):
    """An animate/animateTransform through [values] (list of strings), back to the first, looping.

    [kind] is a transform type (rotate, translate, scale) or None for a plain attribute.
    """
    vals = list(values)
    if vals[-1] != vals[0]:
        vals.append(vals[0])
    n = len(vals)
    tag = 'animateTransform' if kind else 'animate'
    a = f'<{tag} attributeName="{attr}"'
    if kind:
        a += f' type="{kind}"'
    a += f' values="{";".join(vals)}" dur="{dur}s" repeatCount="indefinite"'
    if ease:
        a += f' keyTimes="{times(n)}" calcMode="spline" keySplines="{splines(n - 1)}"'
    if begin:
        a += f' begin="{begin}"'
    return a + extra + '/>'


def spin(cx, cy, dur, deg=360, reverse=False):
    """A continuous rotation about (cx, cy)."""
    a, b = (deg, 0) if reverse else (0, deg)
    return (f'<animateTransform attributeName="transform" type="rotate" '
            f'from="{a} {fmt(cx)} {fmt(cy)}" to="{b} {fmt(cx)} {fmt(cy)}" dur="{dur}s" repeatCount="indefinite"/>')


def group(children, *anims, attrs=''):
    """A <g> holding [children] with animations [anims]."""
    inner = '\n'.join('    ' + c for ch in children for c in ch.split('\n'))
    an = '\n'.join('    ' + x for x in anims)
    parts = [f'<g{(" " + attrs) if attrs else ""}>']
    if an:
        parts.append(an)
    parts.append(inner)
    parts.append('</g>')
    return '\n'.join(parts)


def pulse(children, cx=12, cy=12, amount=0.06, dur=3):
    """Grows and shrinks [children] about (cx, cy) by [amount] (fraction of size)."""
    s = fmt(1 + amount)
    inner = group(children, attrs=f'transform="translate({fmt(-cx)} {fmt(-cy)})"')
    mid = group([inner], osc('transform', ['1', s], dur, kind='scale'))
    return group([mid], attrs=f'transform="translate({fmt(cx)} {fmt(cy)})"')


def shift(children, dx, dy, dur, extra_values=None):
    """Moves [children] by (dx, dy) and back."""
    vals = ['0 0', f'{fmt(dx)} {fmt(dy)}'] + (extra_values or [])
    return group(children, osc('transform', vals, dur, kind='translate'))


def is_frame(el):
    """Whether [el] is Tabler's invisible 24x24 frame path."""
    return 'd="M0 0h24v24H0z"' in el



NUM = re.compile(r'-?\d*\.?\d+')

# The logo flame (www/svg/flame.svg, absolute commands). Its round body is centered at (12, 14.294) with radius 7.
FLAME = ('M12 10.941C14.333 7.633 12.167 3.118 11 2C11 5.395 8.765 7.299 7.333 8.706C5.903 10.114 5 12.327 5 14.294'
         'C5 17.998 8.134 21 12 21C15.866 21 19 17.998 19 14.294C19 12.582 17.768 9.891 16.667 8.706'
         'C14.583 12.059 13.41 12.059 12 10.941')
FLAME_TIPS = {5: 'top', 7: 'top', 9: 'top', 35: 'right', 37: 'right', 39: 'right'}  # number index -> tip it moves


def flame_head(cx, cy, r, top=0.0, right=0.0):
    """A stick figure's head: the logo flame with its round body centered at (cx, cy) and radius [r].

    [top] and [right] raise the top and right tips, in logo units (body radius 7).
    """
    lift = {'top': top, 'right': right}
    s = r / 7
    out = iter([fmt((v - 12) * s + cx) if i % 2 == 0 else fmt((v - lift.get(FLAME_TIPS.get(i), 0) - 14.294) * s + cy)
                for i, v in enumerate(float(x) for x in NUM.findall(FLAME))])
    return NUM.sub(lambda m: next(out), FLAME)


def affine(d, s=1.0, dx=0.0, dy=0.0, cx=12.0, cy=12.0):
    """Path [d] (absolute M, L, Q, C only) scaled by [s] about (cx, cy), then moved by (dx, dy)."""
    assert not re.search(r'[a-z]', d), d
    out = iter([fmt((float(v) - cx) * s + cx + dx) if i % 2 == 0 else fmt((float(v) - cy) * s + cy + dy)
                for i, v in enumerate(NUM.findall(d))])
    return NUM.sub(lambda m: next(out), d)

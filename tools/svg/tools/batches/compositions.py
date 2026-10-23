"""Composed icons: a main idea with a small sub icon in the lower right corner (notes.md, Strategies: Composition).

Usage: python3 compositions.py [labels...]   (default: all). Built with round2's packer: static small, large, very-large
and animated.
"""
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, os.path.dirname(__file__))
from anim import group
from forge import keys, p
import round2
from round2 import pack, build

round2.PACKS.clear()

# Tabler's badge corner: the sub icon sits in x 14..22, y 14..22, and the main idea stops short of it.
EDIT_BADGE = 'M18.42 15.61a2.1 2.1 0 0 1 2.97 2.97l-3.39 3.42h-3v-3l3.42 -3.39'  # Tabler's -edit pencil


# ---------- songwriting: a detailed pencil, the main idea, with a note in the corner; the pencil writes, the note hops ----------
# The pencil is drawn lying along x with its point at the left, then turned -45° about PENCIL_AT, its axis on x + y = 23
# so its lower edge clears the corner by 1.5.
PENCIL_AT = (11.5, 11.5)
PENCIL_BODY = 'M-7 -2H5V2H-7Z'
PENCIL_POINT = 'M-7 -2L-10.5 0L-7 2'
PENCIL_LEAD = 'M-9.2 -.75V.75'
PENCIL_FERRULE = 'M6.5 -2V2'
PENCIL_ERASER = 'M6.5 -2H7.5A2 2 0 0 1 7.5 2H6.5'
NOTE_HEAD = 'M15 19.6a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0'
NOTE_STEM = 'M18.2 19.6V14.6'
NOTE_FLAG = 'M18.2 14.6c1.4 .4 2.6 1.4 2.6 3.2'


def pencil(*anims):
    parts = [p(PENCIL_BODY), p(PENCIL_POINT), p(PENCIL_LEAD), p(PENCIL_FERRULE), p(PENCIL_ERASER)]
    turned = group(parts, attrs='transform="rotate(-45)"')
    return group([group([turned], *anims)], attrs=f'transform="translate({PENCIL_AT[0]} {PENCIL_AT[1]})"')


@pack('songwriting')
def songwriting():
    note = [p(NOTE_HEAD), p(NOTE_STEM), p(NOTE_FLAG)]
    static = [pencil()] + note
    dur = 2.4
    write = keys('transform', [(0, '0 0'), (0.08, '-.7 .3'), (0.16, '.2 .5'), (0.24, '-.5 .1'), (0.32, '.1 .4'), (0.42, '0 0'), (1, '0 0')], dur, kind='translate')
    hop = keys('transform', [(0, '0 0'), (0.5, '0 0'), (0.6, '0 -1'), (0.7, '0 0'), (0.8, '0 -.6'), (0.9, '0 0'), (1, '0 0')], dur, kind='translate')
    return static, [pencil(write), group(note, hop)]


if __name__ == '__main__':
    for label in sys.argv[1:] or round2.PACKS:
        build(label)
        print('built', label)

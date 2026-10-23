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


# ---------- songwriting: Tabler's music with its right note cut back for the -edit pencil; the pencil writes ----------
NOTE_HEAD = 'M3 17a3 3 0 1 0 6 0a3 3 0 0 0 -6 0'
NOTE_STEMS = 'M9 17v-13h10v7'
NOTE_BEAM = 'M9 8h10'


@pack('songwriting')
def songwriting():
    note = [p(NOTE_HEAD), p(NOTE_STEMS), p(NOTE_BEAM)]
    static = note + [p(EDIT_BADGE)]
    dur = 2.4
    write = keys('transform', [(0, '0 0'), (0.1, '-.6 .5'), (0.2, '.3 -.2'), (0.3, '-.5 .4'), (0.4, '0 0'), (1, '0 0')], dur, kind='translate')
    bob = keys('transform', [(0, '0 0'), (0.5, '0 0'), (0.6, '0 -.6'), (0.7, '0 0'), (0.8, '0 -.4'), (0.9, '0 0'), (1, '0 0')], dur, kind='translate')
    return static, [group(note, bob), group([p(EDIT_BADGE)], write)]


if __name__ == '__main__':
    for label in sys.argv[1:] or round2.PACKS:
        build(label)
        print('built', label)

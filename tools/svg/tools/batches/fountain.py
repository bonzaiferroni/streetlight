import os, re, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *
sys.argv = ['fill.py']
src = open('/tmp/claude-1000/-home-starfox-projects-streetlight/4b7f68ea-34dc-47f5-9941-5a2349350eb5/scratchpad/fill.py').read()
exec(src[src.index('def fmt'):src.index('def with_width')].replace('def fmt', 'def fmt_svg'))
pkg = os.path.expanduser('~/apps/tabler-icons/package/icons')
out = fmt_svg(open(f'{pkg}/outline/fountain.svg').read())
for v, w in {'small': '2', 'large': '1', 'very-large': '0.5'}.items():
    open(f'{SVG_DIR}/fountain-{v}.svg', 'w').write(re.sub(r'stroke-width="[0-9.]+"', f'stroke-width="{w}"', out))
open(f'{SVG_DIR}/fountain-filled.svg', 'w').write(fmt_svg(open(f'{pkg}/filled/fountain.svg').read()))

# each plume spurts: its end retreats and extends, the four out of step
head, els = load('fountain')
plumes = {
    'M12 16v-10a3 3 0 0 1 6 0': 0.0,
    'M12 6a3 3 0 0 0 -6 0': 0.25,
    'M9 16v-5a2 2 0 1 0 -4 0': 0.12,
    'M15 16v-5a2 2 0 1 1 4 0': 0.38,
}
def spurt(phase):
    # rest full, a quick retreat, then a slower extension back to full; all inside the loop
    pts = [(0, 1)] + ([(phase, 1)] if phase else []) + [(phase + 0.18, 0.6), (phase + 0.5, 1)]
    return pts + ([(1, 1)] if phase + 0.5 < 1 else [])
out = [els[0]]
for d, ph in plumes.items():
    pts = spurt(ph)
    vals = ';'.join(f'{fmt(v)} 2' for _, v in pts)
    kt = ';'.join(fmt(t) for t, _ in pts)
    out.append(f'<path d="{d}" pathLength="1" stroke-dasharray="{fmt(pts[0][1])} 2">\n'
               f'    <animate attributeName="stroke-dasharray" values="{vals}" keyTimes="{kt}" dur="1.6s" repeatCount="indefinite" '
               f'calcMode="spline" keySplines="{";".join([EASE] * (len(pts) - 1))}"/>\n</path>')
out.append(els[5])
save('fountain', head, out)

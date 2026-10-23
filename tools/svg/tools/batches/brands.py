import os, re, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from anim import *

# brand icons only grow and shrink
labels = re.findall(r'svgPackOf\("(brand-[^"]+)"\)', open('/home/starfox/projects/streetlight/koala/src/commonMain/kotlin/koala/SvgFile.kt').read())
for l in labels:
    head, els = load(l)
    frame = [e for e in els if is_frame(e)]
    rest = [e for e in els if not is_frame(e)]
    save(l, head, frame + [pulse(rest, amount=0.06, dur=3)])
print(len(labels), 'brands')

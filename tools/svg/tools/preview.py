# Writes tools/svg/preview.html: animated icons grouped by the enum that uses them, then the rest.
import base64, glob, os, re
root = '/home/starfox/projects/streetlight'
kt = open(f'{root}/koala/src/commonMain/kotlin/koala/SvgFile.kt').read()
label_of = {v: l for v, l in re.findall(r'val (\w+) = svgPackOf\("([^"]+)"\)', kt)}
sections, used = [], set()
paths = sorted(glob.glob(f'{root}/model/src/commonMain/kotlin/streetlight/model/*/*.kt'))
for p in paths:
    src = open(p).read()
    m = re.search(r'enum class (\w+)\([^)]*val svg: SvgPack', src)
    if not m:
        continue
    items = [(name, label_of[v]) for name, v in re.findall(r'^\s+(\w+)\((?:\d+, )?SvgFile\.(\w+)', src, re.M) if v in label_of]
    sections.append((m.group(1), items))
    used.update(l for _, l in items)
order = ['EventTag', 'LocationTag', 'ButtonSvg', 'ResourceSvg', 'SkillTag', 'ExtraLinkSvg', 'InstrumentSvg', 'GenreSvg',
         'EventStatusSvg', 'BlockSvg', 'VehicleSvg', 'MediaSvg', 'PostSvg', 'EarthLayerSvg']
sections.sort(key=lambda s: order.index(s[0]) if s[0] in order else len(order))
rest = sorted(l for l in label_of.values() if l not in used)
sections.append(('SvgFile (no enum)', [(l, l) for l in rest]))
def cell(name, label):
    return f'<figure title="{label}"><img src="../../www/icon/{label}-animated.svg" loading="lazy"><figcaption>{name}</figcaption></figure>'
forged = ['lectern', 'model-pose', 'caricature', 'bouquet', 'clown', 'top-hat', 'pen-nib', 'beatbox', 'french-horn', 'clarinet', 'organ', 'water-slide', 'speakeasy', 'race-flag', 'party-hat', 'billiards', 'trampoline', 'marionette', 'tightrope', 'taxi', 'subway', 'pier', 'drive-in', 'shower', 'family', 'flame', 'fireworks', 'dance', 'playground', 'tram', 'accordion', 'harp', 'electric-guitar', 'synthesizer', 'barber-pole', 'ferris-wheel', 'toy-blocks', 'drum', 'violin', 'trumpet', 'saxophone', 'flute', 'acoustic-guitar', 'boxing-glove', 'disco-ball', 'juggling', 'hot-spring', 'fountain']
pair = ''.join(f'<figure><img src="../../www/icon/{l}-large.svg"><img src="../../www/icon/{l}-animated.svg"><figcaption>{l}</figcaption></figure>' for l in forged)
# under review: shown as the site shows flair, a mask over the event color at low opacity
review = [('Lecture', 'presentation'), ('Crafting', 'needle-thread'), ('FoodAndDrink', 'tools-kitchen'), ('LGBTQ', 'rainbow'), ('Church', 'building-church'), ('Dance', 'dance')]
def masked(name, label):
    uri = 'data:image/svg+xml;base64,' + base64.b64encode(open(f'{root}/www/icon/{label}-animated.svg', 'rb').read()).decode()
    return f'<figure><div class="flair" style="--mask: url({uri})"></div><figcaption>{name}</figcaption></figure>'
reviewed = ''.join(masked(*r) for r in review)
# edited this round: static large and animated at 10rem
edited = [(l, l) for l in ('songwriting',)]
latest = ''.join(f'<figure><img src="../../www/icon/{l}-large.svg"><img src="../../www/icon/{l}-animated.svg"><figcaption>{l}</figcaption></figure>' for n, l in edited)
nav = '<a href="#Edited">Edited</a> · <a href="#Review">Review</a> · <a href="#New">New</a> · ' + ' · '.join(f'<a href="#{n.split()[0]}">{n.split()[0]}</a>' for n, _ in sections)
body = ''.join(f'<h2 id="{n.split()[0]}">{n} <small>{len(items)}</small></h2><main>{"".join(cell(*i) for i in items)}</main>' for n, items in sections)
open(f'{root}/tools/svg/preview.html', 'w').write(f'''<!doctype html><meta charset="utf-8"><title>Animated Icons</title>
<style>
body {{ font: 12px monospace; margin: 16px; background: #fff; color: #111; }}
a {{ color: inherit; }}
@media (prefers-color-scheme: dark) {{ body {{ background: #111; color: #eee; }} img {{ filter: invert(1); }} }}
main {{ display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 12px; }}
figure {{ margin: 0; text-align: center; overflow-wrap: anywhere; }}
img {{ width: 48px; height: 48px; }}
h2 {{ margin-top: 32px; }}
main.pairs {{ grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); }}
main.pairs img {{ margin: 0 4px; }}
#Edited + main {{ grid-template-columns: repeat(auto-fill, minmax(22rem, 1fr)); }}
#Edited + main img {{ width: 10rem; height: 10rem; margin: 0 1rem; }}
#Review + main {{ grid-template-columns: repeat(auto-fill, 20rem); background: rgb(18, 26, 26); color: rgb(238, 230, 230); padding: 16px; }}
.flair {{ width: 20rem; aspect-ratio: 1; background-color: color-mix(in srgb, #EC2E6A 75%, rgb(238, 230, 230)); opacity: .25;
    mask: var(--mask) center / 100% 100% no-repeat; -webkit-mask: var(--mask) center / 100% 100% no-repeat; }}
</style>
<nav>{nav}</nav><h2 id="Edited">Edited <small>static · animated</small></h2><main>{latest}</main><h2 id="Review">Under review <small>as flair: event color, low opacity</small></h2><main>{reviewed}</main><h2 id="New">New icons <small>static · animated</small></h2><main class="pairs">{pair}</main>{body}''')
print(len(sections), 'sections;', sum(len(i) for _, i in sections), 'cells;', len(rest), 'outside enums')

"""Makes swings pass smoothly through their rest position.

An oscillation keyed rest; +a; rest; -a; rest eases to a stop at every key, so it pauses in the
middle. For every key at the rest value whose neighbors deviate in opposite directions (including
the loop seam), the adjoining segments are re-splined to keep full speed through the rest key.
Usage: python3 smooth.py [files...]  (default: all www/icon/*-animated.svg). Prints changed files.
"""
import glob, re, sys

NUM = re.compile(r'-?\d*\.?\d+(?:e-?\d+)?')

def spline(s0, s1):
    # cubic bezier with normalized start slope s0 and end slope s1
    return f'0.33 {0.33 * s0:.2f} 0.67 {1 - 0.33 * s1:.2f}'

def fix_anim(tag):
    if 'calcMode="spline"' not in tag:
        return tag
    vals = re.search(r'\bvalues="([^"]+)"', tag)
    ks = re.search(r'\bkeySplines="([^"]+)"', tag)
    if not vals or not ks:
        return tag
    keys = [[float(x) for x in NUM.findall(v)] for v in vals.group(1).split(';')]
    n = len(keys)
    if n < 3 or any(len(k) != len(keys[0]) for k in keys):
        return tag
    rest = keys[0]
    dev = [[a - b for a, b in zip(k, rest)] for k in keys]
    at_rest = [all(abs(x) < 1e-9 for x in d) for d in dev]
    dot = lambda a, b: sum(x * y for x, y in zip(a, b))
    cross = [False] * n
    for i in range(1, n - 1):
        if at_rest[i] and not at_rest[i - 1] and not at_rest[i + 1] and dot(dev[i - 1], dev[i + 1]) < 0:
            cross[i] = True
    if at_rest[0] and at_rest[-1] and not at_rest[1] and not at_rest[-2] and dot(dev[1], dev[-2]) < 0:
        cross[0] = cross[-1] = True
    if not any(cross):
        return tag
    segs = [s.strip() for s in ks.group(1).split(';')]
    for k in range(n - 1):
        if cross[k] or cross[k + 1]:
            segs[k] = spline(2 if cross[k] else 0, 2 if cross[k + 1] else 0)
    return tag[:ks.start(1)] + ';'.join(segs) + tag[ks.end(1):]

files = sys.argv[1:] or sorted(glob.glob('/home/starfox/projects/streetlight/www/icon/*-animated.svg'))
for f in files:
    s = open(f).read()
    t = re.sub(r'<animate(?:Transform)?\b[^>]*>', lambda m: fix_anim(m.group(0)), s)
    if t != s:
        open(f, 'w').write(t)
        print(f.rsplit('/', 1)[1][:-13])

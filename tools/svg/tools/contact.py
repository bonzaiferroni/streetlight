"""Writes a contact sheet of static icons: large at 160px, small at 48px beneath it, five to a row.

Usage: python3 contact.py out.png label...   (rsvg-convert renders; the sheet shows how each reads at both sizes)
"""
import os, subprocess, sys, tempfile
from PIL import Image
D = '/home/starfox/projects/streetlight/www/icon'
out, labels = sys.argv[1], sys.argv[2:]
tmp = tempfile.mkdtemp()
im = Image.new('RGB', (170 * 5, 230 * ((len(labels) + 4) // 5)), 'white')
for i, l in enumerate(labels):
    for v, w in (('large', 160), ('small', 48)):
        subprocess.run(['rsvg-convert', '-w', str(w), '-b', 'white', '-o', f'{tmp}/{v}-{l}.png', f'{D}/{l}-{v}.svg'], check=True)
    x, y = (i % 5) * 170, (i // 5) * 230
    im.paste(Image.open(f'{tmp}/large-{l}.png'), (x, y))
    im.paste(Image.open(f'{tmp}/small-{l}.png'), (x + 56, y + 172))
im.save(out)

"""Usage: python3 review.py out.png label...  Renders small (24px, upscaled), large and very-large (220px) per label."""
import subprocess, sys
from PIL import Image
out, labels = sys.argv[1], sys.argv[2:]
D = '/home/starfox/projects/streetlight/www/icon'
c = Image.new('RGB', (900, 230 * len(labels)), 'white')
def render(path, px):
    svg = open(path).read().replace('currentColor', '#000')
    return Image.open(__import__('io').BytesIO(subprocess.run(['rsvg-convert', '-w', str(px), '-b', 'white'], input=svg.encode(), capture_output=True).stdout))
for i, l in enumerate(labels):
    c.paste(render(f'{D}/{l}-small.svg', 24).resize((96, 96), Image.NEAREST), (0, i * 230 + 60))
    c.paste(render(f'{D}/{l}-small.svg', 220), (110, i * 230))
    c.paste(render(f'{D}/{l}-large.svg', 220), (340, i * 230))
    c.paste(render(f'{D}/{l}-very-large.svg', 220), (570, i * 230))
c.save(out)

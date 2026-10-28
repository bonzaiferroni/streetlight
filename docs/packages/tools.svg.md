# Module tools.svg

## Introduction

The workshop for the site's icons. Python batch scripts write the variant files of each pack in `www/icon`, and Node scripts render and check them. The packs are declared in `SvgFile`, as specified in `koala.md`. `tools/svg/notes.md` records each round of work and what every script built.

## Dependencies

| Package | Provides |
|---|---|
| Playwright at `tools/benchmark/node_modules/playwright` | Chromium for frame sheets and loop scans |
| `rsvg-convert` (librsvg2-bin) | Static rasters, the first frame only |
| `xmllint` (libxml2-utils) | The well-formedness check after a scripted edit |
| `ffmpeg` | Frames to GIF or WebM, for reviewing an animation |
| `python3` with PIL | Contact sheets; `~/.venvs/tools/bin/python` takes anything installed with pip |
| Tabler outline icons at `~/apps/tabler-icons/package/icons/outline` | The starting geometry of most packs |

## Naming

| Thing | Name |
|---|---|
| A pack's files | `www/icon/foo-small.svg`, `foo-large.svg`, `foo-very-large.svg`, `foo-animated.svg`, for the label `foo` |
| A label | Kebab case, named for the idea (`songwriting`, `lectern`), not its parts; checked against `www/icon` before use |
| A batch script | `tools/batches/<name>.py`, run as `python3 <name>.py [labels...]`; no labels builds all of its packs |
| Shared helpers | `tools/anim.py` (timing, groups, `flame_head`, `affine`), `tools/forge.py` (headers, `keys`), `tools/rig.py` (bones), `tools/batches/round2.py` (`pack`, `build`, `figure_frames`) |
| A clipPath or mask id | Prefixed with the label, as `drive-in-screen` |

`preview.html` and `__pycache__` are not committed.

## Variants

| Variant | Stroke | Content |
|---|---|---|
| `small` | 2 | The static icon; a flame head carries `stroke-width="1.5"` |
| `large` | 1 | The static icon |
| `very-large` | 0.5 | The static icon |
| `animated` | 1 | The large icon in motion; its frame 0 equals `large` |

A pack built with `round2.build` gets all four from one function that returns its static and animated elements.

Every file's root `<svg>` carries a `width` and `height` equal to the size of its `viewBox`, as the tabler packs' `24` do.

## Animation

- SMIL only: `<animate>`, `<animateTransform>` and `<animateMotion>`. No CSS and no script in the file.
- Every animation loops with `repeatCount="indefinite"`, and the end of the loop meets its start.
- Motion is gentle: loops of about 1 to 4 seconds and about 0.5 to 1.5 units of travel on the 24 grid, slower where the motion needs it.
- The animation moves a feature of the icon, as a clock's hands or a wheel's hub. A pack with no feature to move grows and shrinks by a few percent about its center. A `brand-*` pack only grows and shrinks.
- Reduced motion is handled by the UI, not the file.
- The durations in one file keep a small least common multiple, since `sheet.js` samples one full period.
- `animateMotion` that keeps time with another animation sets `calcMode="linear"` or `spline`. The default, `paced`, ignores `keyPoints` and `keyTimes`.
- keyTimes run from 0 to 1. A staggered beat ends by 1, or the browser drops the whole animation.
- A dash that hides a line fully uses `pathLength="1"` with `stroke-dasharray="1 1.1"`, so no round cap is left behind.
- A comet, a line whose tail follows its head along it, is one dash shorter than the line, swept with `stroke-dashoffset` from just before the line to just past it, so no offset leaves a dash of no length at an end. The fireworks' launch trails use `0.667 2`, swept from 0.7 to -1.05.
- A reveal, such as paint or a page, is a clipPath whose edge carries the same `animateTransform` values as the tool that makes it.
- A layer is swapped for an identical copy only under a fully opaque cover: the cover returns first, then the layer beneath hides. Two identical layers at frame 0 are not drawn at once; the copy beneath stays hidden until it is needed.

## Strategies

An icon's shape comes from the first strategy that carries its idea. Strategies combine only when one alone cannot.

| Strategy | Use |
|---|---|
| Tabler | First. A close Tabler icon is copied into a batch script and animated |
| Forged | When Tabler has nothing close: our own drawing in the Tabler style |
| Composition | When one shape cannot carry the idea and two simple ones can |
| Stick figure | A person in a pose or in motion |

### Tabler

Tabler geometry also serves the other strategies: as the main idea or sub icon of a composition (Tabler's `-edit` pencil as a sub icon), or as a prop beside a forged figure (lectern is Tabler's podium).

### Forged

A forged icon follows Tabler's drawing: the 24 grid, a live area of 2 to 22, round caps and joins. Details closer than about 2.5 units merge at stroke 2, so the small variant is checked at 48px.

### Composition

A main idea with a sub icon in the lower right corner, as Tabler's `music-plus` or `user-edit`.

- The sub icon sits in x 14 to 22 and y 14 to 22.
- The main idea is cut back or placed so nothing comes within about 1.5 units of the sub icon: parts that reach into the corner are dropped or shortened, or the whole shape moves away from it.
- The main idea is the subject and can carry detail; the sub icon is a simple shape that qualifies it. Songwriting is a pencil with a note in the corner, the pencil drawn with its point, lead, ferrule and eraser.
- A sub icon that is a Tabler badge shape (`-edit`, `-plus`, `-heart`, `-check`) is kept as a constant in `tools/batches/compositions.py`.
- In the animation one of the two acts and the other answers, as the pencil writes and the note hops.

### Stick Figures

A figure is a flame head on a jointed body. It is posed with bones, never by editing its paths.

#### Bones

A rig (`Rig` in `tools/rig.py`) is a list of joints, `(name, parent, (x, y))` at their rest positions, parents first, the first being the root. Each bone is the vector from its parent to its joint at rest and keeps its length.

```python
MODEL = Rig([
    ('hip', None, (12, 12.6)), ('chest', 'hip', (12, 10.3)), ('neck', 'chest', (12, 8)),
    ('elbowL', 'neck', (10.9, 11)), ('handL', 'elbowL', (10.5, 14)),
    ('kneeL', 'hip', (11.6, 16.8)), ('footL', 'kneeL', (11.2, 21)),
])
pose = MODEL.pose(rot={'chest': -3}, move=(-0.6, 0.15), ik={'handL': ((10.9, 12.4), 1), 'footL': ((10.4, 21), 1)})
```

| Step | Math |
|---|---|
| Forward | A joint's angle is its parent's angle plus `rot[joint]`, in degrees clockwise on the y-down grid. Its position is its parent's position plus its rest vector turned by that angle. A rotation carries every joint below it. `move` shifts the root |
| Inverse | After the forward pass, `ik={end: (target, bend)}` pins the two-bone chain root → mid → end to the target. With bone lengths l1 and l2, the root at a and the target at c: d = \|c − a\|, clamped to l1 + l2; along = (l1² − l2² + d²) / 2d; h = √(l1² − along²). The mid joint is a + u·along + n·h·bend, with u the unit vector from a to c and n its perpendicular. `bend`, 1 or −1, picks the side. A target out of reach straightens the limb toward it |
| Drawing | `curve(points, soft)` draws a chain of joints as one cubic path. At an interior joint the tangent runs parallel to next − previous, and each handle is `soft` times its segment's length |

| Joint | `soft` |
|---|---|
| A corner | 0 |
| An acute joint | About 0.1; more makes the curve loop |
| A smooth limb | 0.2 to 0.35 |

- Feet are pinned to the ground and hands to whatever they hold or rest on. Every other joint comes from rotations.
- A pose is a dict of joint positions. Every frame draws the same chains, so each frame's path has the same commands and `animate d` morphs between them.
- `figure_frames(frames, chains, head, dur)` builds the static and animated elements. The head is a static flame translated with the frame's `head` joint, usually the neck, so it stays upright.
- Frames are eased evenly. A repeated frame holds a pose: `[p0, p0, p1, p1]` holds each of two stances. Cyclic motion, such as a gait, samples one cycle into 8 to 16 frames played linearly.
- In a figure seen from the side, the near leg and arm join the body and the far ones start about 45% down the thigh and upper arm. The near and far limbs never swap, so the gap marks the far side.

#### Figure Style

| Part | Rule |
|---|---|
| Head | The logo flame from `flame_head(cx, cy, r)`, its round body where a circle head would be and its tips above. r is 2 or more, never under 1.8 |
| Fit | The figure is scaled about (12, 12) and shifted with `affine` so the flame's tips stay inside 2 to 22, shrunk only as far as the tips need |
| Head in motion | Rides with the body, never flickers |
| Limbs through a joint that does not bend | One smooth curve: the arms through the shoulders are one line, each arm bowed up a little so the hands fall away |
| The seat | A rounded fillet where the leg meets the torso, not a corner |
| The back | A gentle bend, about 0.5 off the straight line |
| Knees and elbows | Bends, softened where the pose allows |

Busts and people who are not figures, such as `user` or `juggling`, keep round heads.

## Reading at Icon Size

The shapes are checked for what they say before the idea is attached.

| Shape | Reads as |
|---|---|
| Round hills under a round sun | A person in bed |
| A roller over an outlined wall | A phone |
| Four straight rays | A plus sign; a four-point sparkle reads as a flash |
| A circle over a small cross | The female symbol; shoulders as an arc read as a person |
| A bell attached radially to a circle | A mirror or magnifier; a tube leaving along the tangent reads as a horn |

## Motion

- A motion is one the thing really makes: a wand pivots at its handle so its tip taps, a drawing stays still while the pencil draws it, a page flips whole.
- Fewer, clearer beats: rest, act, rest, or two poses in turn.
- A part attached at both ends scales outward from its roots and never rotates, since rotation swings one end into what holds it.
- Moving parts stay clear of fixed ones through the whole loop, not just at frame 0.

## Workflows

### Making an Icon

1. Pick the strategy and a label.
2. Write its pack in a batch script and run the script with the label.
3. Check it:
   - `python3 tools/contact.py out.png foo` shows the large and small statics.
   - `node tools/sheet.js out.png foo` shows frames across one period and flags `FRAME0!=STATIC`, `SEAM` and `NO-MOTION`. A frame 0 difference is expected when the animation adds a feature, or shows in turn parts the static icon shows together.
   - `node tools/blip.js foo` steps through the loop and prints sudden jumps between frames.
   - `xmllint --noout` on each file.
4. Register the pack in `SvgFile` in alphabetical order and repoint the enum value. The old pack stays.
5. Add the label to `edited` in `tools/preview.py` and run it, then open `preview.html`. Once the icon is approved, its label moves to the front of `forged`.
6. Record the round in `tools/svg/notes.md`: the script, each icon and the icon it replaced, and any rejected design with the reason.

### Regenerating

The script that last built a pack owns it, and `notes.md` names it. Running an older script for that label overwrites later work. `tools/smooth.py` runs again after any regeneration of a swing it smoothed.

# Module tools.promo

## Introduction

The capture pipeline for promo videos. A script films one shot of the app and encodes it to a looping AV1 webm. The content a shot shows is specified in `streetlight.web.promo.md`.

## Dependencies

| Package | Provides |
|---|---|
| `playwright` | Chromium's headless shell and its CDP sessions |
| `esbuild` | The bundle of the promo compilation |
| `sharp` | The camera's crop and scale |
| `ffmpeg` at `~/.local/bin/ffmpeg` | The encode, with `libsvtav1` |

## Naming

| Thing | Name |
|---|---|
| A shot's script | `foo.mjs`, for the shot `fooShot` in `web/src/jsPromo` |
| Its frames | `frames/foo-<mode>-<segment>/0000.png` |
| Its takes | `out/foo-<n>.webm`, `out/foo-fast-<n>.webm`; each run writes the next number |

`node_modules`, `bundle`, `frames` and `out` are not committed. A take that is used is copied into `www/video`.

## Running

```
node theme.mjs [--fast] [--build] [--no-build] [--encode-only] [--fps=60] [--crf=32] [--preset=4] [--svt=...]
```

| Flag | Effect |
|---|---|
| `--fast` | Renders at the output size with a quick encode, for composition |
| `--build` | Compiles and bundles the shots even when the bundle is fresh |
| `--no-build` | Skips compiling and bundling the shots |
| `--encode-only` | Encodes the frames of the last capture |

Full mode renders at device scale 2 and the encode downscales with lanczos.

`buildShots` compiles only when a source under `web`, `model`, `koala`, `kabinet` or `kampfire` is newer than the bundle (`kabinet` from `kabinet/library`). A run logs the seconds each phase took, through `lap`: build, capture, assemble and encode.

## Studio

`openStudio` films a page in Chrome's headless shell with begin frame control and virtual time, so the page runs as it does in a browser and every frame lands on time. A script never freezes, seeks or overrides the page's clock.

| Phase | Time | Frames |
|---|---|---|
| Before `start()` | Real | `load` and `until` draw one per interval while they wait |
| After `start()` | Virtual | `frame()` advances time by one interval, then draws |

Specifications the studio depends on:

- Begin frame control is taken only by a target created with `Target.createTarget({ enableBeginFrameControl: true })`, so the studio opens its page that way in a persistent context.
- `beginFrame` draws only rendering. JS timers, `performance.now` and coroutines follow virtual time, which `Emulation.setVirtualTimePolicy` advances one interval before each draw. Virtual time waits while a fetch is pending.
- A frame is drawn on the window's own surface. Its size and scale come from `--window-size` and `--force-device-scale-factor`, and Playwright's emulated viewport and device scale do not reach it.
- Under begin frame control an input event waits for the next frame. `frame()` sends its mouse input through `Input.dispatchMouseEvent` without waiting, draws, then settles the input. A Playwright `page.mouse` call during capture never returns.
- `Page.captureScreenshot` is not used in the studio. The frame's image comes from `beginFrame`.
- An element that enters with an `@starting-style` transition after `start()` stays painted partway through it, though its computed style has finished. A still shot turns off the `FocusInSlow` transition, and a shot that mounts such elements during capture turns theirs off.
- The page closes on `window.scrollTo`. The studio's `scrollTo` sets `scrollTop` on the scrolling element.

`settle()` draws frames until the page's images are drawn and its finite animations have finished, checking every few frames, and returns one frame more. The frame drawn as a transition ends still shows it unfinished.

A shot marked `still` pauses every animation, so a frame drawn at any time is the same. A still shot can film a held moment as one frame.

## Camera

The page renders its whole viewport. A camera is a rectangle in page pixels with the shot's aspect, and `frameWriter` crops it from the frame and scales it to the output size. The viewport is sized for the widest frame and the device scale for the closest, so a close frame takes at least one device pixel per output pixel.

A zoom eases the camera's width on a log scale, with its center moving in proportion.

The drawn pointer is a page element placed by `frame({ pointer })`. Its scale is divided by the camera's zoom, so it keeps its size on screen.

## Loops

A shot loops without a seam:

- Every value the pointer changes ends where it began: a light circles back to its place, a slider runs out and back.
- An infinite animation completes whole cycles within the loop. A shot paces one that does not, as `theme.mjs` sets the color flux to the loop length.
- A transition started late in the loop finishes before the loop ends.

## Assembly

`assemble` lays out a shot's frames from an edit list of segments: a still held for some seconds, the frames of a directory, or a transition between two stills, which blurs the first as it fades into the second as that sharpens. A held or played frame is a link to its image, so a hold costs no frames on disk.

## Layout Showcase

`layout-showcase.mjs` holds FizzBuzz's page in each header layout, the hero, the marquee and the card, each blurring and fading into the next. It ends on the hero at the foot of the page, scrolling up to its top, where it began, so it loops. The script sets the layout through `promoHeader(name)`, which the shot adds to the page.

The shot is `still` and its lights `Static`, so each layout is filmed as one still and only the scroll as frames, and `assemble` draws the transitions between them, since the page itself can only blur from one layout to the next. The hero's still is filmed in the same render as its scroll, so a paused animation stands at the same point where the loop meets.

## Fizzbuzz

`fizzbuzz.mjs` films the earth map against the dev server's data, and predates the studio. It freezes `performance.now`, seeks every animation, and swaps animated icons each frame in a regular browser with the GPU. Its zoom blends `sub` samples per frame into motion blur, which hides the flicker of MapLibre's buildings. A new shot uses the studio.

# Package streetlight.compose.svg

## Introduction

The renderer of the site's animated icons: an XML reader, the SVG document and its SMIL animations, and their drawing. A browser plays an icon's SMIL; Skia, which draws the static icons, does not.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.ui.graphics` | Paths, `PathParser` and drawing |

## Web Counterparts

| Compose | Web |
|---|---|
| `drawSvg` | The browser's SVG and SMIL, behind the web's icon mask |

## Rendering

`readSvg` reads a document once; `drawSvg` draws it at a time in seconds. Every shape is drawn in the one color given, as the web's mask uses an icon's alpha alone.

The renderer covers what the site's icons use: `g`, `path`, `circle`, `line` and `rect`; `transform` lists; `clipPath` with `clip-rule`, and `fill-rule`; stroke and fill presentation with `pathLength` dashes, which it cuts itself so a dash of no length draws nothing, as in a browser, where Skia's dash effect would draw a dot; and `animate`, `animateTransform` and `animateMotion` with `values`, `keyTimes`, spline `keySplines`, `from`/`to` and `begin`, repeating indefinitely. An animated value interpolates the numbers of its keyframes in place, so path data, transforms and dash arrays share one interpolation. A `mask` is not applied, and a group's `opacity` multiplies into its shapes rather than compositing the group.

An icon the renderer does not draw as the browser does is brought in line in its file, by the script that owns it in `tools/svg`, rather than taught to the renderer.

`DrawSvgTest` draws every animated icon at several times, as the renderer is a parser over the whole set.

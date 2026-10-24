# Module koala

## Introduction

The base set of components and functionality for web apps. It declares `View`, a unit of UI within the browser, and the tools views are built from.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire` | Wire and state types |
| `kotlinx.html` | The HTML DSL |
| `kotlinx.coroutines` | Coroutines behind views and state |

`koala` depends on `kampfire`. `kampfire` does not depend on `koala`.

## Packages

| Package | Holds |
|---|---|
| `koala.component` | Components, such as `switch`, `column` and `icon` |
| `koala.layout` | Layouts, such as `tabs` and `messageBox` |
| `koala.model` | Classes and interfaces not bound to their point of use, such as `WebRoute` |
| `koala.page` | The frame a page is built on: its head, scripts, ids, mounts and data islands |
| `koala.markdown` | Markdown parsing and rendering |
| `koala.interop` | Calls between markup and the bundle, and the `wire` functions that bind a view to server markup |
| `koala.external` | Declarations of browser and library APIs |
| `koala.utility` | Utilities whose target is not a koala type |
| `koala.dom` | The view runtime: `View`, `ViewScope`, `AppendScope` and the view factories |

UI that needs the browser lives under `koala.dom`, in a package that mirrors its dom-agnostic counterpart: `koala.dom.component`, `koala.dom.layout`, `koala.dom.page` and `koala.dom.markdown`. A browser view imports the `koala.dom` packages with a star import, so it sees the `AppendScope` and `ViewScope` variants and not the `FlowContent` ones. Markup rendered on the server imports the dom-agnostic packages.

| File | Lives in |
|---|---|
| `FooUtility.kt` | The package of its target type, so its extensions need no extra import. A target outside koala puts it in `koala.utility` |
| `FooStyle` object and `FooCss` stylesheet | The dom-agnostic package of the component that references them |
| A model declared for the browser | `commonMain`, unless it needs the browser |

## UI Hierarchy

| Level | Definition | Package |
|---|---|---|
| Component | A single element of UI, even when built from other components | `koala.component` |
| Layout | A composed section of UI, calling components only to lay them out, reusable or not | `koala.layout` |
| View | A layout associated with a model that holds UI state | The app |

A container such as `div`, `column`, `card`, `popover` or `dialog` is a component. A form's parts and a feed row are layouts. A view is named `viewFoo` for the model `Foo`.

A component is imported with `koala.component.*`, and in the browser with `koala.dom.component.*`.

## Launching Coroutines

Start a coroutine with `koala.utility.launch`, not the raw builder.

The browser does not give a coherent call stack for an exception thrown inside a coroutine. `launch` names the coroutine and carries the name in its context as `LaunchTelemetry`. `appExceptionHandler` appends that name to the exception message, along with the path of the view and element the coroutine belongs to, and rethrows the same throwable.

A coroutine launched with the raw builder fails without a name, and its message says nothing about where it came from.

Code that wraps launching builds on `koala.utility.launch` so that the telemetry is kept.

## DropWhileBusy

`DropWhileBusy` launches a block only when its previous launch has finished. A launch requested while one is active is dropped and returns `null`. The previous launch is never cancelled.

An action that must not run twice at once owns one instance. Two actions never share an instance.

## Shaders

A WebGL shader is written in GLSL ES 3.00 as a string in the Kotlin file of the layer that compiles it, marked `// language="GLSL"`. A layer drawn into the map implements `CustomLayerInterface` and is added to the map in `initGeoMap`.

The map's appearance lives in its style, `www/misc/fjord.json`, which the web client and the app both load. A source or layer that needs no code, such as the 3D buildings, is declared there and not added at runtime. `initGeoMap` adds only what must be drawn in code, such as a `CustomLayerInterface` layer.

## Icons

`SvgFile` declares the site's icons, each an SVG file named in kebab case, in alphabetical order. An icon taken from Tabler is declared once by its label with `svgPackOf`, as an `SvgPack` whose variants are files in `www/icon` named `foo-variant.svg`:

| Variant | File | Content |
|---|---|---|
| `small` | `foo-small.svg` | `stroke-width` of 2 |
| `large` | `foo-large.svg` | `stroke-width` of 1 |
| `veryLarge` | `foo-very-large.svg` | `stroke-width` of 0.5 |
| `filled` | `foo-filled.svg` | A filled path, no stroke |
| `animated` | `foo-animated.svg` | An animation |

The stroke widths keep the lines a similar weight at each size. An animated icon is shown with `magicIcon`, which shows the `large` variant and swaps to the `animated` one under `prefers-reduced-motion: no-preference`. A pack declares every variant whether or not its file exists yet. A reference names the variant, as `SvgFile.Foo.large`.

An icon from any other source is a file in `www/svg`, declared with `svgOf`, which takes the whole filename without its extension, as `svgOf("foo-large")`.

An icon from an outside source is listed in `docs/sources` under that source, its variants together as `foo-*.svg`, for attribution.

New and reworked icons are made in `tools/svg`, specified in `tools.svg.md`: the animation rules, the strategies an icon's shape comes from, and the workflow. `tools/svg/notes.md` records each round of work.

## Markers

A marker's body is the indicator that rests on its point, centered on it, such as the thumb of a `ThumbMarker`, the icon of an `IconMarker` or the whole of a `TravelMarker`. The marker's base sits on the point at zero size, with `BodySize` set inline from `bodySize`. `MarkerStyle.Body` shifts its element up and left by half of `BodySize`, so a body of that size, leading the element, is centered on the point, and anything after it, such as a label, trails to its right.

A marker with a `light` shows a light of that color in the light layer beneath it; a marker without one shows none.

A `TravelMarker` draws its icon pointing up and turns to its `bearing`, in degrees clockwise from north. The turn is the `rotate` of the marker's base, set from the inline `MarkerBearing` property and enabled by `MarkerStyle.Bearing`, which a travel marker carries in its `mod` by default. A marker whose icon should stay upright leaves the class out of its `mod`.

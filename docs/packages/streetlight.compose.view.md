# Package streetlight.compose.view

## Introduction

The app's theme, and the composables that take a model or the `Portal`: the view of each route, the feed and its rows, the map, and icons.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.foundation` | Layout, text and lazy layouts |
| `androidx.navigation3` | `NavDisplay` |
| `streetlight.compose.model` | Routes, `Portal`, view models and entity properties |
| `streetlight.compose.modifier` | The theme's styling as modifiers |
| `streetlight.compose.svg` | The renderer of animated icons |
| `org.maplibre.compose` | The map |

## Naming

| Type | Pattern |
|---|---|
| `FooView` | The composable a route renders |
| `foo` on `LazyGridScope`, `LazyStaggeredGridScope` or `LazyListScope` | A part of a lazy layout that adds its own items, such as `feedSection`, named in lower case as `items` is |
| `KoalaFoo` | A group of the theme's values, such as `KoalaColors` |
| `FooMod` | The fixed modifiers of the composable `Foo`, the cousin of the web's `FooStyle` |

## Web Counterparts

| Compose | Web |
|---|---|
| `AppPortal` | `viewPortal` |
| `FooView` | `viewFoo` |
| `AppTheme` | `applyTheme`, and the background of `ThemeCss` |
| `KoalaAppTheme` | `KoalaTheme`, and the stylesheets built from it |
| `KoalaColors` | The color custom properties of `ThemeCss` |
| `KoalaOpacity` | `--opacity-low`, `--opacity-half`, `--opacity-high` |
| `KoalaTypography` | `TypographyCss` and the text classes, such as `TextSmall` |
| `currentTheme.unit` | `--unit`, `KoalaTheme.spacingUnit` |
| `currentTheme.line` | The 2px of `Gap2Px` and `--outline-low` |
| `Column(gap)`, `Row(gap)` | `Column`, `Row` with `Gap(n)` |
| `feedSection` | `feedSection` (`streetlight.web.layout`) |
| `FeedRow`, `FeedMode` | `feedRow`, `FeedMode` (`streetlight.web.layout`) |
| `CellGrid` | `cellGrid` (`streetlight.web.layout`) |
| `Icon` | `icon` (`koala.component`) |
| `ContainImage` | `containImage` (`koala.component`) |
| `AnimatedIcon` | `magicIcon` (`koala.component`), its animated mask |
| `MapWindow` | `geoMapMount` (`koala.component`) and `initGeoMap` |

## Views

`AppPortal` maps each route to its view with one `entry` in its `entryProvider`. Each entry gets its own saved state and its own view model store, so a view model lives while its route is on the back stack.

A view declares its view model as the context parameter `model`, and the `Portal` when it navigates, following `docs/kotlin-style.md`. `App()` provides the `Portal` once, above `AppPortal`. A view reads a lens with `collectAsState()` from `TapUtility.kt`.

```kotlin
entry<FooRoute> {
    context(koinViewModel<FooModel>()) { FooView() }
}
```

## Composables

A view that scrolls is a `LazyGridView`, one lazy grid for the whole view, filled with sections built on `LazyGridScope`. `LazyStaggeredGridView` is its masonry cousin, each column stacking items of their own heights, filled with sections built on `LazyStaggeredGridScope`; a section is written for each scope it serves. On either, `fullItem` spans every column, and `pinnedItem` also stays composed off screen, for content costly to start anew. The home view is a `LazyStaggeredGridView`, its items a unit apart and a unit from its edges in `Grid` mode, so the rounded entries stand clear, and a line apart otherwise. The map and the feed heading stay where they are in either mode.

A composable draws its content and leaves its framing to the caller. It takes a `modifier` and applies it to its root, and the call site gives the corners, shadow, outline and surface, as `CellGrid(top, bottom, FeedRowMod.Cells)`. The composable keeps a styling of its own only where its content depends on it, as the cells of `CellGrid` need a background behind them for their gaps to draw its lines.

A modifier whose arguments do not change in composition is declared once, as a `val` of a `FooMod` object named for the composable that applies it, the cousin of the web's `FooStyle` objects: `FeedRowMod.Base`, `FeedRowMod.Cells`. A val is a whole chain, and a call site extends it, as `FeedRowMod.Base.padding(1)`, or adds it to its own, as `.then(FeedRowMod.Cells)`. A modifier that takes a theme value in composition, as `padding(top = currentTheme.line)`, stays at the call site. The object sits in the composable's file, private while only that file uses it.

## Theme

The theme has two levels: `KoalaAppTheme`, the app's own values, and `PageTheme`, the colors and glows a piece of content brings over them.

`AppTheme` wraps the app below Koin. It shows the `PageTheme` held by `ThemeHost`, draws its three glows behind the content, and provides the `KoalaAppTheme` as `LocalKoalaAppTheme`. A composable reads it through the top-level property `currentTheme`, after Compose's `current` prefix for ambient reads. The theme groups its values as the web's stylesheets do: `currentTheme.unit`, `currentTheme.text.small`, `currentTheme.colors.primary`, `currentTheme.opacity.low`, `currentTheme.lights`. A view whose content carries a theme passes it to `ThemeHost.setTheme`.

A change of theme animates over `KoalaTheme.ThemeTransitionSeconds` with the web's bounce easing. `KoalaLights` holds the glows and the flux hue as states, `drawLights` draws them as the page background, its size the frame of their positions and radii; `KoalaLights.pageSize`, set as the root is laid out, gives a light's radius on the page to a surface drawn elsewhere, and `shineBg()` casts them on a surface. In `Flux` mode the glow colors turn around the hue wheel every 30 seconds, as the web's `hueSpin` does.

Every value that animates is read inside a draw lambda, never in composition, so an animation frame redraws and does not recompose. A composable reads `currentTheme` directly where it uses a value in composition, as `Modifier.padding(top = currentTheme.line)`. An animated color read there recomposes its reader on each frame of a transition, so a color that animates is passed as a lambda where the API takes one. `currentTheme` is read only in composition, so a composable handing it to a lambda captures it first:

```kotlin
val theme = currentTheme
BasicText(text, color = { theme.colors.accent })
```

### Colors

A color derived from the theme, such as `accentFg`, is a getter on `KoalaColors` mixing its sources the way `ThemeCss` does. A `SchemeColor` is resolved with `KoalaColors.colorOf`, as the web's `ThemeColor` is resolved through its custom properties. `Muted` is the ink at the web's `--opacity-high`.

The scheme color of a part of the page is a context, as the web's `--color-scheme`: `LocalSchemeColor` holds it, the ink unless provided, and a view whose content belongs to an entity provides the entity's, as `FeedRow` does. A modifier that paints in it, such as `schemeBg()`, reads it while drawing.

### Typography

`KoalaTypography`, read as `currentTheme.text`, holds the web's text styles, each a multiple of the rem as in `TypographyCss`: `heading1` to `heading6`, `paragraph`, `regular`, `small` and `large`, in Fira Sans. `regular` is the rem itself, the size of text with no style of its own. A style is named for its web class or tag, so `heading5` is `h5` and `small` is `TextSmall`. The web's `Bold` is `FontWeight.SemiBold`. Every style has the web body's 1.4 line height, its leading split evenly above and below the line as CSS does. A use that differs from a style copies it, as a feed row's title copies `regular` with a 1.15 line height.

The fonts are in `composeResources/font`, with their license in `composeResources/files`, and are built into a `FontFamily` by `firaSans()`.

### Rem

`AppTheme` sizes the type and the unit from a rem chosen by the window's width: 16sp, and 14sp below 600dp, as the web's `html` drops to `87.5%` below 600px. It measures the window once, at the root. A layout that adapts to its own width measures that instead.

### Unit Spacing

Spacing is a whole multiple of `currentTheme.unit`, the shortest distance that shows two elements are separate. It is half the rem, as the web's `0.5rem`. Changing it sets the density of the whole app.

The unit builds the hierarchy of grouping. The elements of an item are one unit apart and the item has one unit of padding, so the content of two adjacent items is two units apart. A multiple is written `unit * 2`. A spacing that is not a whole unit is a named value. The one the theme holds is `line`, 2dp, the web's 2px: the gap between rows and the width of an outline, where a unit is too much and a single pixel too sharp. A CSS px and a dp are both reference pixels, and Compose lays out on whole device pixels, so 2px translates directly to 2.dp. A padding in units is `padding(n)` from `streetlight.compose.modifier`. Spacing between children is the parent's `Arrangement`, not a modifier. A gap in units is the `gap` of the `Column` and `Row` overloads in `Lines.kt`, as `Row(1)`, which take the place of foundation's where a gap is given and resolve to it where none is; any other spacing is foundation's `spacedBy`, as `spacedBy(currentTheme.line)`.

## Icons

The app bundles the site's icons. The build syncs `www/icon`, the files the web serves, into the app's resources as `files/icon`, so an `Svg` from the shared code, such as a variant of an `SvgFile` pack, names its bundled file: `/www/icon/foo.svg` is `files/icon/foo.svg`.

`Icon` draws an `Svg` tinted with a color read while drawing, filling the icon's shape as the web's mask does. `rememberSvgPainter` reads an icon's file once, keeps its bytes, and is empty while it loads. `AnimatedIcon` plays an icon's `animated` variant through `streetlight.compose.svg`, at a clock read while drawing.

## Map

`MapWindow` shows MapLibre's map in the site's style, `www/misc/fjord.json`, read from the server by URL as the web reads it, so the two share one style and a change to it reaches the app without a build. The style carries everything that needs no code, as `koala.md` specifies. The window is darkened toward its edges as the web's map overlay is, and `HomeView` frames it as on the web's home, with MapLibre's controls but not its logo.

The desktop window provides the map's presentation host in `main()`, through `ProvideMapPresentationHost`.

A lazy layout disposes an item that scrolls away, which would close the map's native renderer and start it anew each time it returned, so a map is a `pinnedItem`.

## Feed

A feed is shown with `feedSection`, which adds its heading and entries to the `LazyGridView` or `LazyStaggeredGridView` it is in. The heading spans the row, and so does each entry but in `Grid` mode, where an entry takes a column. The section takes the mode as a value, so a change of mode lays the feed out again. The heading sits in a `filigree()` between the type menu, bound to the page model's type, and the `FeedMode` switch, bound to `AppSettings`, both a `MutableTap`.

An entry carries `zenBg()`, and the feed stands its entries `currentTheme.line` apart, so the background behind shows through as the line between them, as the web's `ZenBg` rows and `Gap2Px` column do.

A layout that changes with its width, such as `CellGrid` or the `Row` feed row, decides in its own `Layout`'s measure, from the width it is given. It needs no subcomposition, so it costs nothing per entry.

### Feed Rows

`FeedRow` shows any `Entity`, laid out by `FeedMode`. The layouts are recorded here so their intent survives later changes.

| Mode | Entry |
|---|---|
| `Minimal` | One row with a unit of padding: a square thumbnail of 10 units over a backdrop of the entity's color, then a column of the top details, the title in `regular` on up to two lines, and the bottom details, held to the top of the row, then the flair badge: the entity's `toFlair()` animated, in its color at the web's `--opacity-low`, as tall as the thumbnail. The parts are a unit apart. Details are `small`, on one line each, joined by a muted dot and cut with an ellipsis, leaving out those without text. The top details are in their colors; the bottom details are in the ink at `opacityHigh()`, as a description is |
| `Row` | Padded a unit all around, where the web pads a line at the sides. The main part: the thumbnail, outlined; the title in `heading5` semi-bold on one line over the entity's `body` as markdown in `small` at `opacityHigh()`, both clipped at the row's 10 units with half a unit of inset and fading where they reach the clip; then the flair badge. The `CellGrid` sits below it, or beside it as the row's right half, less half a unit, from 960dp |
| `Grid` | No padding but a unit below. A `ContainImage` across the top in the image's own `aspect`, held between 1:2 and 4:1, or 3:2 when it has none; the title and description as in `Row`, clipped at 10 units with no inset, beside the flair badge; then the `CellGrid`. The parts below the image are inset a unit at the sides and stand a unit apart. The entry's corners are rounded three units |

### Cell Grid

`CellGrid` shows an entity's top and bottom details as cells, on one line when every cell gets at least 128dp, otherwise the top details over the bottom ones. The cells of a line share its width evenly, the pixels left over going to its first cells, so a line never ends in a cell of another width. Each cell is 5 units tall on `cardBg()`, its icon in the detail's color at its start and its text centered in the rest; a detail without text shows its icon alone at `opacityLow()`, in the same place. The cells stand a line apart, so the background draws the grid's lines. A feed row frames it rounded two and a half units, outlined, in the moon's shadow, on `schemeBg()` in the entity's scheme color.

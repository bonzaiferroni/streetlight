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
| `FooScreen` | The composable a route renders: it inflates the route's content and shows the `FooView` |
| `FooView` | The composable that shows a route's content |
| `foo` on `LazyGridScope`, `LazyStaggeredGridScope` or `LazyListScope` | A part of a lazy layout that adds its own items, such as `feedSection`, named in lower case as `items` is |
| `KoalaFoo` | A group of the theme's values, such as `KoalaColors` |
| `FooMod` | The fixed modifiers of the composable `Foo`, the cousin of the web's `FooStyle` |

## Web Counterparts

| Compose | Web |
|---|---|
| `AppPortal` | `viewPortal` |
| `FooScreen` | `viewFooRoute` and its `RouteBlock` |
| `FooView` | `viewFoo`, and the shell it renders |
| `AppTheme` | `applyTheme`, and the background of `ThemeCss` |
| `KoalaAppTheme` | `KoalaTheme`, and the stylesheets built from it |
| `KoalaColors` | The color custom properties of `ThemeCss` |
| `KoalaOpacity` | `--opacity-low`, `--opacity-half`, `--opacity-high` |
| `KoalaMagic` | `MagicStyle` and the motion of `MagicCss`: `--magic-interval`, `--magic-easing`, `--magic-cubic-bounce` |
| `KoalaTypography` | `TypographyCss` and the text classes, such as `TextSmall` |
| `currentTheme.unit` | `--unit`, `KoalaTheme.spacingUnit` |
| `currentTheme.line` | The 2px of `Gap2Px` and `--outline-mid` |
| `Column(gap)`, `Row(gap)` | `Column`, `Row` with `Gap(n)` |
| `feedSection` | `feedSection` (`streetlight.web.layout`) |
| `FeedRow`, `FeedMode` | `feedRow`, `FeedMode` (`streetlight.web.layout`) |
| `CellGrid` | `cellGrid` (`streetlight.web.layout`) |
| `Icon` | `icon` (`koala.component`) |
| `ContainImage` | `containImage` (`koala.component`) |
| `AnimatedIcon` | `magicIcon` (`koala.component`), its animated mask |
| `MapWindow` | `geoMapMount` (`koala.component`) and `initGeoMap` |
| `SmallGalaxyCard` | `smallGalaxyCard` (`streetlight.web.layout`) |
| `ConnectPicker` | `connectPicker` (`streetlight.web.component`) |
| `AppFooter` | `appFooter` (`streetlight.web.page`), without its lottie |
| `EntityHeader` | `entityHeader` and its `pageHeader` card (`streetlight.web.layout`) |
| `Tabs`, `TabScope`, `tab` | `tabs`, its `TabScope` and `tab` (`koala.layout`) |
| `GalaxyView`, its `layoutBlock` | `galaxyShell` and `renderLayout` (`streetlight.web.layout`) |
| `EventView` | `eventShell` and `eventBody` (`streetlight.web.layout`) |
| `AppHeader` | `appHeader` (`streetlight.web.page`), unlinked |
| `TextLogo` | `textLogo` (`streetlight.web.component`) |
| `Button`, `ButtonStyle` | `btn` and `button` (`koala.component`), and the button classes of `ButtonCss`, such as `Zen` |
| `Popover` | `popoverMenu` (`streetlight.web.dom.component`) and its card |
| `TagFilter`, `EventTagMenu` | `tagFilterMenu` (`streetlight.web.component`) and `eventTagMenu` |
| `SearchField` | `searchField` (`koala.component`) |

A button is a `Button` whatever its click does. The web's `btn` exists to dress a link as a button; compose has no such split, so a button that navigates and one that acts are one composable, built on foundation's `clickable` with no Material indication.

A menu opened by a click is a `Popover`, composed inside what opens it while it is open. It places itself below its opener when it fits, above when only that fits, and otherwise on the side with more room, kept inside the window, and it closes on a click outside. It grows from the web's initial scale with the magic bounce out of the corner nearest its opener, fading in, and shrinks back into it as it closes; the `Popup` stays composed until its exit ends.

A floating card blurs the page behind it, as the web's `BlurBackdrop` does, with Haze. `AppTheme` captures the page, lights and all, as the `hazeSource` of `LocalPageHaze`, and the card reads it with `hazeBlur(HazeInput.Sources(…))`, which reaches across the composition root of a `Popup`. The card's tint goes over the blur.

A filter is state on the view model, bound to its control as a `MutableTap`; the model reads the feed again when the state changes. A feed section shows the filters of a type that `isSearched`.

A web link to a page the app has no route for, such as a galaxy card's or the footer's about link, is shown unlinked until the route exists. A link that leaves the site, such as the footer's source link, opens in the browser.

## Views

`AppPortal` maps each route to its screen with one `entry` in its `entryProvider`. Each entry gets its own saved state and its own view model store, so what a screen keeps lives while its route is on the back stack.

A route's `FooScreen` is where the app arrives at it. It inflates the route's content with `Inflated<Content>(route)`, which shows a message while it loads or when a problem keeps it, then builds the view's model from the content in hand and shows the `FooView`. The view and its model never see the inflation: a model is built with its content, and a view shows what its model holds, as the web's `view*Route` hands its content to the shell.

```kotlin
@Composable
fun GalaxyScreen(route: GalaxyRoute) = Inflated<GalaxyContent>(route) { content ->
    context(koinViewModel<GalaxyModel> { parametersOf(content) }) { GalaxyView() }
}
```

A view declares its view model as the context parameter `model`, following `docs/kotlin-style.md`, and reads a lens with `collectAsState()` from `TapUtility.kt`. A view with no state of its own takes its content as an argument, as `EventView(event)`.

The `Portal` is ambient: `App()` provides it as `LocalPortal`, and a part that opens a page reads `currentPortal` and calls it from `pointerClick`, as a feed row opens its entity's `toRoute()` and the logo of `AppHeader` returns `home()`. An entity the app has no page for opens nothing.

## Tabs

`Tabs` is a layout, not a view: it takes no model, and its lambda builds the tabs on a `TabScope` with `tab(label, icon) { content }`, as the web's does. The buttons share the container's width, wrapping below the web's 100px each, and the chosen tab's content slides in a short way from the side its button lies on as the other slides out the other way, never sweeping across the page. A tab remembers which is chosen across recomposition and a return to its page.

## Motion

A change of page fades the old page out over one `magic` interval, then the new one in over the next, as the web's portal does, and a return does the same. The new page's content is read from the start of the fade-out, so it is often ready as the page fades in; content that arrives later fades in within the page, and a loading message is held back for a moment so a quick read shows none.

A part that a page shares with the page it opens moves between them, the container transform: `AppPortal` sets the stage, `LocalSharedTransitionScope`, and both pages mark the part with `sharedPart(route, part)`, keyed by the route of the page they lead to, so the two find each other without passing anything. A `Frame` and a `Title` share their bounds, their contents fading between; an `Image` is one element on both pages. A galaxy card's frame, image and name grow into the galaxy's header, and a feed row's image and title into its page's header. A part without a route shares nothing.

## Composables

A view that scrolls is a `LazyGridView`, one lazy grid for the whole view, filled with sections built on `LazyGridScope`. `LazyStaggeredGridView` is its masonry cousin, each column stacking items of their own heights, filled with sections built on `LazyStaggeredGridScope`; a section is written for each scope it serves. On either, `fullItem` spans every column, and `pinnedItem` also stays composed off screen, for content costly to start anew. The home view is a `LazyStaggeredGridView`, its items a unit apart and a unit from its edges in `Grid` mode, so the rounded entries stand clear, and a line apart otherwise. The map and the feed heading stay where they are in either mode.

A composable draws its content and leaves its framing to the caller. It takes a `modifier` and applies it to its root, and the call site gives the corners, shadow, outline and surface, as `CellGrid(details, FeedRowMod.Cells)`. The composable keeps a styling of its own only where its content depends on it, as the cells of `CellGrid` need a background behind them for their gaps to draw its lines.

A modifier whose arguments do not change in composition is declared once, as a `val` of a `FooMod` object named for the composable that applies it, the cousin of the web's `FooStyle` objects: `FeedRowMod.Base`, `FeedRowMod.Cells`. A val is a whole chain, and a call site extends it, as `FeedRowMod.Base.padding(1)`, or adds it to its own, as `.then(FeedRowMod.Cells)`. A modifier that takes a theme value in composition, as `padding(top = currentTheme.line)`, stays at the call site. The object sits in the composable's file, private while only that file uses it.

## Theme

The theme has two levels: `KoalaAppTheme`, the app's own values, and `PageTheme`, the colors and glows a piece of content brings over them.

`AppTheme` wraps the app below Koin. It shows the `PageTheme` held by `ThemeHost`, draws its three glows behind the content, and provides the `KoalaAppTheme` as `LocalKoalaAppTheme`. A composable reads it through the top-level property `currentTheme`, after Compose's `current` prefix for ambient reads. The theme groups its values as the web's stylesheets do: `currentTheme.unit`, `currentTheme.text.small`, `currentTheme.colors.primary`, `currentTheme.opacity.low`, `currentTheme.lights`, `currentTheme.magic`. A motion takes its timing and curve from `currentTheme.magic`, as `eased()` or `bounced()`. Each view shows its page's theme with `PageThemeEffect`, its content's theme or `null` for the default, so returning to a view restores its own.

A change of theme animates over `KoalaTheme.ThemeTransitionSeconds` with the web's bounce easing. `KoalaLights` holds the glows and the flux hue as states, `drawLights` draws them as the page background, its size the frame of their positions and radii; `KoalaLights.pageSize`, set as the root is laid out, gives a light's radius on the page to a surface drawn elsewhere, and `shineBg()` casts them on a surface. In `Flux` mode the glow colors turn around the hue wheel every 30 seconds, as the web's `hueSpin` does.

Every value that animates is read inside a draw lambda, never in composition, so an animation frame redraws and does not recompose. A composable reads `currentTheme` directly where it uses a value in composition, as `Modifier.padding(top = currentTheme.line)`. An animated color read there recomposes its reader on each frame of a transition, so a color that animates is passed as a lambda where the API takes one. `currentTheme` is read only in composition, so a composable handing it to a lambda captures it first:

```kotlin
val theme = currentTheme
BasicText(text, color = { theme.colors.accent })
```

### Colors

A color derived from the theme, such as `accentFg`, is a getter on `KoalaColors` mixing its sources the way `ThemeCss` does. An `EntityType` is given its color by `KoalaColors.colorOf`, as the web gives it a custom property, and no type the primary color. `muted` is the ink at the web's `--opacity-high`.

The scheme color of a part of the page is a context, as the web's `--color-scheme`: `LocalSchemeColor` holds the resolved color. A view is neutral, so `AppTheme` provides the ink, and a layout that shows an entity provides the entity's color, `colorOf(entity.toEntityType())`, as `FeedRow`, `EntityHeader` and `SmallGalaxyCard` do, a typeless entity taking the primary color. A part that paints in it, such as `schemeBg()` or a tab's button, reads it as given.

### Typography

`KoalaTypography`, read as `currentTheme.text`, holds the web's text styles, each a multiple of the rem as in `TypographyCss`: `heading1` to `heading6`, `paragraph`, `regular`, `small` and `large`, in Fira Sans. `regular` is the rem itself, the size of text with no style of its own. A style is named for its web class or tag, so `heading5` is `h5` and `small` is `TextSmall`. The web's `Bold` is `FontWeight.SemiBold`. Every style has the web body's 1.4 line height, its leading split evenly above and below the line as CSS does. A use that differs from a style copies it, as a feed row's title copies `regular` with a 1.15 line height.

The fonts are in `composeResources/font`, with their license in `composeResources/files`, and are built into a `FontFamily` by `firaSans()`: each weight the web uses, upright and italic, so italic text takes Fira's own italic rather than a slanted upright.

### Rem

`AppTheme` sizes the type and the unit from a rem chosen by the window's width: 16sp, and 14sp below 600dp, as the web's `html` drops to `87.5%` below 600px. It measures the window once, at the root. A layout that adapts to its own width measures that instead.

### Unit Spacing

Spacing is a whole multiple of `currentTheme.unit`, the shortest distance that shows two elements are separate. It is half the rem, as the web's `0.5rem`. Changing it sets the density of the whole app.

The unit builds the hierarchy of grouping. The elements of an item are one unit apart and the item has one unit of padding, so the content of two adjacent items is two units apart. A multiple is written `unit * 2`. A spacing that is not a whole unit is a named value. The one the theme holds is `line`, 2dp, the web's 2px: the gap between rows and the width of an outline, where a unit is too much and a single pixel too sharp. A CSS px and a dp are both reference pixels, and Compose lays out on whole device pixels, so 2px translates directly to 2.dp. A padding in units is `padding(n)` from `streetlight.compose.modifier`. Spacing between children is the parent's `Arrangement`, not a modifier. A gap in units is the `gap` of the `Column` and `Row` overloads in `Lines.kt`, as `Row(1)`, which take the place of foundation's where a gap is given and resolve to it where none is; any other spacing is foundation's `spacedBy`, as `spacedBy(currentTheme.line)`.

## Icons

The app bundles the site's icons and drawings. The build syncs `www/icon` and `www/svg`, the files the web serves, into the app's resources as `files/icon` and `files/svg`, so an `Svg` from the shared code, such as a variant of an `SvgFile` pack or the logo, names its bundled file: `/www/icon/foo.svg` is `files/icon/foo.svg`, and `/www/svg/foo.svg` is `files/svg/foo.svg`.

`Icon` draws an `Svg` tinted with a color read while drawing, filling the icon's shape as the web's mask does. `rememberSvgPainter` reads an icon's file once, keeps its bytes, and is empty while it loads. `AnimatedIcon` plays an icon's `animated` variant through `streetlight.compose.svg`, at a clock read while drawing.

## Map

`MapWindow` shows MapLibre's map in the site's style, `www/misc/fjord.json`, read from the server by URL as the web reads it, so the two share one style and a change to it reaches the app without a build. The style carries everything that needs no code, as `koala.md` specifies. The window is darkened toward its edges as the web's map overlay is, and `HomeView` frames it as on the web's home, with MapLibre's controls but not its logo.

The desktop window provides the map's presentation host in `main()`, through `ProvideMapPresentationHost`.

The app has one map. Starting MapLibre's renderer stalls the UI thread for most of a second, so `MapHost`, around the app in `App()`, composes the map once at launch and keeps it for the life of the app. A page shows it with a `MapSlot` where the map goes, its center and the radius of its corners; the slot holds the place in the page's layout and reports its bounds as the page lays out and scrolls, and the host draws the map there, above the page, in its own frame and outline. The map shows in the newest page's slot, fading out, moving and looking over the slot's center, then fading in, in step with the page. A slot leaves the host as soon as its page starts to leave, so the map fades out with it. A page never composes a map of its own, and a slot needs no pinning, since its leaving the composition costs nothing.

## Feed

A feed is shown with `feedSection`, which adds its heading and entries to the `LazyGridView` or `LazyStaggeredGridView` it is in. The heading spans the row, and so does each entry but in `Grid` mode, where an entry takes a column. It reads its controls' state from the `FeedModel` it takes as a context parameter. A lazy layout's builder cannot read state, so the view reads the page and the mode and passes them as values, and a change of either lays the feed out again. The heading sits in a `filigree()` between the type menu, bound to the model's type, and the `FeedMode` switch, bound to the mode the model takes from `AppSettings`.

An entry carries `zenBg()`, and the feed stands its entries `currentTheme.line` apart, so the background behind shows through as the line between them, as the web's `ZenBg` rows and `Gap2Px` column do.

A layout that changes with its width, such as `CellGrid` or the `Row` feed row, decides in its own `Layout`'s measure, from the width it is given. It needs no subcomposition, so it costs nothing per entry.

### Feed Rows

`FeedRow` shows any `Entity`, laid out by `FeedMode`. The layouts are recorded here so their intent survives later changes.

| Mode | Entry |
|---|---|
| `Minimal` | One row with a unit of padding: a square thumbnail of 8 units over a backdrop of the entity's color, then a column of the priority details, the title in `heading5`, semibold, on one line, and the other details, held to the top of the row, then the flair badge: the entity's `toFlair()` animated, in its color at the web's `--opacity-low`, as tall as the thumbnail. The parts are a unit apart. Details are `small`, on one line each, joined by a muted dot and cut with an ellipsis. The priority details are in their colors; the others are in the ink at `opacityHigh()`, as a description is |
| `Row` | Padded a unit all around, where the web pads a line at the sides. The main part: the thumbnail, outlined; the title in `heading5` semi-bold on one line over the entity's `body` as markdown in `small` at `opacityHigh()`, both clipped at the row's 10 units with half a unit of inset and fading where they reach the clip; then the flair badge. The `CellGrid` sits below it, or beside it as the row's right half, less half a unit, from 960dp |
| `Grid` | No padding but a unit below. A `ContainImage` across the top in the image's own `aspect`, held between 1:2 and 4:1, or 3:2 when it has none; the title and description as in `Row`, clipped at 10 units with no inset, beside the flair badge; then the `CellGrid`. The parts below the image are inset a unit at the sides and stand a unit apart. The entry's corners are rounded three units |

### Cell Grid

`CellGrid` lays out an entity's details as the web's `cellGrid` does: in as many equal columns of at least 128dp as fit, up to 7, wrapping onto more lines, the last cell stretching to the end of its line. The columns share the width evenly, the pixels left over going to the first columns. Each cell is 5 units tall on `cardBg()`, its icon, or else its label, at its start in the detail's color or the scheme color, and its text centered in the rest. A cell whose url leaves the app opens it when clicked; a url within the app waits for routes. The cells stand a line apart, so the background draws the grid's lines. A feed row frames it rounded two and a half units, outlined, in the moon's shadow, on `schemeBg()` in the entity's scheme color.

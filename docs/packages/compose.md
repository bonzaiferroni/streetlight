# Module compose

## Introduction

The Streetlight native app, built with Compose Multiplatform. It runs on desktop. The iOS app is SwiftUI over `model` and shares no UI with this module.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.foundation` | Layout, text and input. Material is not used |
| `androidx.navigation3` | The back stack and `NavDisplay` |
| `androidx.lifecycle` | `ViewModel`, and its store per back stack entry |
| `koin` | Services and view models, through `koin-compose` |
| `io.ktor.client` | `AppTransport`, over the CIO engine |
| `org.maplibre.compose` | The map, through MapLibre Native |
| `model` | Entities, DTOs, `Api`, and `ApiClient` from `streetlight.model.io` |
| `kampfire.model` | `Store` and `Tap` |
| `koala.modifier` | The theme's defaults and types, `Rgb` and `Glow`, through `model` |

## Packages

| Package | Holds |
|---|---|
| `streetlight.compose.model` | Routes, `Portal`, `appModule`, services and view models |
| `streetlight.compose.io` | `AppTransport` |
| `streetlight.compose.view` | Composables that take a model or the `Portal`, including the view of each route |
| `streetlight.compose.markdown` | Markdown rendered for Compose from koala's parsed blocks |
| `streetlight.compose.modifier` | `Modifier` extensions for the theme's styling, the cousins of `koala.modifier` |
| `streetlight.compose.svg` | The renderer of animated icons: an XML reader, the SVG document and its SMIL animations, and their drawing |
| `streetlight.compose.utility` | Utilities whose target is not a compose type, such as `TapUtility.kt` and `ColorUtility.kt` |

## Naming

| Type | Pattern |
|---|---|
| `FooRoute` | A route, a `@Serializable` member of the sealed `AppRoute`, in `AppRoute.kt` |
| `FooView` | The composable a route renders |
| `FooModel` | The view model of `FooView`, such as `HomeModel`. Web names its models by role, since it has no `View` suffix to pair with |
| `FooState` | The state class a view model holds in its `Store` |
| `foo` on `LazyGridScope` or `LazyListScope` | A part of a lazy layout that adds its own items, such as `feedSection`, named in lower case as `items` is |

## Web Counterparts

A part with a counterpart in `web` follows that counterpart's specification, except where this document says otherwise.

| Compose | Web |
|---|---|
| `AppRoute` | `WebRoute` (`koala.model`) |
| `Portal` | `Portal` (`koala.model`) |
| `AppPortal` | `viewPortal` |
| `appModule` | `webModule` |
| `AppTransport` | `WebTransport` |
| `FooView` | `viewFoo` |
| `AppTheme` | `applyTheme`, and the background of `ThemeCss` |
| `KoalaAppTheme` | `KoalaTheme`, and the stylesheets built from it |
| `KoalaColors` | The color custom properties of `ThemeCss` |
| `KoalaOpacity` | `--opacity-low`, `--opacity-half`, `--opacity-high` |
| `streetlight.compose.modifier` | `koala.modifier`, where a styling class has a counterpart worth having |
| `KoalaTypography` | `TypographyCss` and the text classes, such as `TextSmall` |
| `ThemeColor` | `ThemeColor` (`streetlight.web.model`) |
| `FeedRow`, `FeedMode` | `feedRow`, `FeedMode` |
| `feedSection` | `feedSection` |
| `CellGrid` | `cellGrid` |
| `annotatedMarkdownOf` | `renderMarkdownBlocks` (`koala.markdown`), as a feed row's summary styles it |
| `filigree()` | `filigree` (`koala.component`) and its `ParticleRay` |
| `AppSettings` | The root switches, such as `FeedRowStyle.Mode` |
| `Icon` | `icon` (`koala.component`) |
| `MapWindow` | `geoMapMount` (`koala.component`) and `initGeoMap` |
| `AnimatedIcon` | `magicIcon` (`koala.component`), its animated mask |
| `FlairIcon`, `toFlair` | `FlairIcon`, `toFlair` (`streetlight.web.model`) |
| `EntityUtility.kt` | `EntityUtility.kt` (`streetlight.web.model`) |
| `currentTheme.unit` | `--unit`, `KoalaTheme.spacingUnit` |
| `FooModel`, `FooState` | The models of `streetlight.web.model` |

## Navigation

`Portal` owns the back stack, which holds `AppRoute`s and is saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching the back stack.

`AppPortal` maps each route to its view with one `entry` in its `entryProvider`. Each entry gets its own saved state and its own view model store, so a view model lives while its route is on the back stack.

## Services

`appModule` is the Koin module holding the app's services and view models. A setting the viewer keeps across the app, such as the feed mode, is a lens on `AppSettings`. `App()` starts Koin with it through `KoinApplication`, above the `Portal`. A composable reaches a service with `koinInject()`.

A service or view model takes its collaborators through its constructor, and Koin supplies them. `Portal` is not in Koin, since it is Compose saved state.

## API

`AppTransport` is the app's `ApiTransport`, over Ktor. `appModule` registers it with the server's URL from `AppConfig` and builds `ApiClient` over it. A service or view model that calls the API takes `ApiClient` through its constructor.

`AppCookieStorage` keeps the cookies in an app-data file, so a session outlives the app. A cookie's `Max-Age` is saved as an absolute expiry. When the base URL is plain `http`, the storage clears `secure` on the cookies it keeps, since Ktor sends a secure cookie only over `https` and makes no exception for `localhost` as a browser does.

## View Models

A view model extends `ViewModel`, holds one `Store` of its `FooState`, and exposes lenses as `streetlight.web.model` specifies. Its coroutines run in `viewModelScope`.

It is registered in `appModule` with `viewModelOf(::FooModel)`. The route's entry gets it with `koinViewModel()`, which scopes it to the entry, and provides it to the view as a context parameter.

```kotlin
entry<FooRoute> {
    context(koinViewModel<FooModel>()) { FooView() }
}
```

A view reads a lens with `collectAsState()` from `TapUtility.kt`.

```kotlin
val entities by model.entitiesState.collectAsState()
```

## Images

An image is shown with Coil's `AsyncImage`, given the image's `Url`. The image loader `App()` sets resolves a relative `Url` against `AppConfig.apiUrl`.

## Icons

The app bundles the site's icons. The build syncs `www/icon`, the files the web serves, into the app's resources as `files/icon`, so an `Svg` from the shared code, such as a variant of an `SvgFile` pack, names its bundled file: `/www/icon/foo.svg` is `files/icon/foo.svg`.

`Icon` draws an `Svg` tinted with a color read while drawing, filling the icon's shape as the web's mask does. `rememberSvgPainter` reads an icon's file once, keeps its bytes, and is empty while it loads.

`AnimatedIcon` plays an icon's `animated` variant, whose SMIL a browser plays and Skia does not. `streetlight.compose.svg` reads the document once and draws it at the icon's clock, which is read while drawing, so a frame redraws without recomposing. Every shape is drawn in the one color, as the web's mask uses an icon's alpha alone.

The renderer covers what the site's icons use: `g`, `path`, `circle`, `line` and `rect`; `transform` lists; `clipPath` with `clip-rule`, and `fill-rule`; stroke and fill presentation with `pathLength` dashes, which it cuts itself so a dash of no length draws nothing, as in a browser, where Skia's dash effect would draw a dot; and `animate`, `animateTransform` and `animateMotion` with `values`, `keyTimes`, spline `keySplines`, `from`/`to` and `begin`, repeating indefinitely. An animated value interpolates the numbers of its keyframes in place, so path data, transforms and dash arrays share one interpolation. A `mask` is not applied, and a group's `opacity` multiplies into its shapes rather than compositing the group.

## Map

`MapWindow` shows MapLibre's map in the site's style, `www/misc/fjord.json`, read from the server by URL as the web reads it, so the two share one style and a change to it reaches the app without a build. The style carries everything that needs no code, as `koala.md` specifies; what the map draws in code is added through MapLibre Compose's own layers.

The desktop window provides the map's presentation host in `main()`, through `ProvideMapPresentationHost`.

The map is never an item of a lazy list, which disposes an item that scrolls away and so closes the map's native renderer; it would start anew, style and tiles, each time it returned. A view that scrolls a map lays it over a spacer item of its size and moves it with the lazy layout's scroll, read in its `offset`, as `HomeView` does over its grid.

## Theme

The theme has two levels: `KoalaAppTheme`, the app's own values, and `PageTheme`, the colors and glows a piece of content brings over them.

`AppTheme` wraps the app below Koin. It shows the `PageTheme` held by `ThemeHost`, draws its three glows behind the content, and provides the `KoalaAppTheme` as `LocalKoalaAppTheme`. A composable reads it through the top-level property `currentTheme`, after Compose's `current` prefix for ambient reads. The theme groups its values as the web's stylesheets do: `currentTheme.unit`, `currentTheme.text.small`, `currentTheme.colors.primary`. A new group of values is a class of its own, named `KoalaFoo`. A view whose content carries a theme passes it to `ThemeHost.setTheme`.

A change of theme animates over `KoalaTheme.ThemeTransitionSeconds` with the web's bounce easing. In `Flux` mode the glow colors turn around the hue wheel every 30 seconds, as the web's `hueSpin` does.

Every value that animates is read inside a draw lambda, never in composition, so an animation frame redraws and does not recompose. A composable reads `currentTheme` directly where it uses a value in composition, as `Modifier.padding(currentTheme.unit)`. An animated color read there recomposes its reader on each frame of a transition, so a color that animates is passed as a lambda where the API takes one. `currentTheme` is read only in composition, so a composable handing it to a lambda captures it first:

```kotlin
val theme = currentTheme
BasicText(text, color = { theme.colors.accent })
```

A color derived from the theme, such as `accentFg`, is a getter on `KoalaColors` mixing its sources the way `ThemeCss` does.

## Typography

`KoalaTypography`, read as `currentTheme.text`, holds the web's text styles, each a multiple of the rem as in `TypographyCss`: `heading1` to `heading6`, `paragraph`, `regular`, `small` and `large`, in Fira Sans. `regular` is the rem itself, the size of text with no style of its own. A style is named for its web class or tag, so `heading5` is `h5` and `small` is `TextSmall`. The web's `Bold` is `FontWeight.SemiBold`. Every style has the web body's 1.4 line height, its leading split evenly above and below the line as CSS does. A use that differs from a style copies it, as a feed row's title copies `regular` with a 1.15 line height.

The fonts are in `composeResources/font`, with their license in `composeResources/files`, and are built into a `FontFamily` by `firaSans()`.

## Theme Colors

A color that depends on what it shows is a `ThemeColor`, resolved with `KoalaColors.colorOf`, as the web's `ThemeColor` is resolved through its custom properties. `Muted` is the ink at the web's `--opacity-high`.

## Entities

`EntityUtility.kt` holds the properties of an entity that depend on its type, each a `when` over every entity type, as the web's does. `topDetails` and `bottomDetails` are the `EntityDetail`s a feed row shows, each a `ThemeColor`, an icon and a text. An entity type always gives the same details, in the same order; a fact the entity lacks keeps its detail with `null` text, so its cell still shows. A fact of the entity itself takes the entity's color, as an event's cost and tag take the event's; a fact drawn from another entity takes that entity's, as an event's location takes the location's.

## Feed Rows

`FeedRow` shows any `Entity`, laid out by `FeedMode`. It has only `Minimal` so far.

The layout is recorded here so its intent survives later changes.

| Mode | Entry |
|---|---|
| `Minimal` | One row with a unit of padding: a square thumbnail of 10 units over a backdrop of the entity's color, then a column of the top details, the title in `regular` on up to two lines, and the bottom details, held to the top of the row, then the flair badge: the entity's `toFlair()` animated, in its color at the web's `--opacity-low`, as tall as the thumbnail. The parts are a unit apart. Details are `small`, on one line each, joined by a muted dot and cut with an ellipsis, leaving out those without text. The top details are in their colors; the bottom details are in the ink at `opacityHigh()`, as a description is |
| `Row` | Padded a unit all around, where the web pads a line at the sides. The main part: the thumbnail, outlined; the title in `heading5` semi-bold on one line over the entity's `body` as markdown in `small` at `opacityHigh()`, both clipped at the row's 10 units with half a unit of inset and fading where they reach the clip; then the flair badge. The `CellGrid` sits below it, or beside it as the row's right half, less half a unit, from 960dp |
| `Grid` | No padding but a unit below. The image across the top at 3:2, its `medium` variant whole over the backdrop; the title and description as in `Row`, clipped at 10 units with no inset, beside the flair badge; then the `CellGrid`. The parts below the image are inset a unit at the sides and stand a unit apart |

A feed is shown with `feedSection`, which adds its heading and entries to the `LazyVerticalGrid` it is in: columns of at least 300dp, a line apart both ways, as the web's grid. The heading spans the row, and so does each entry but in `Grid` mode, where an entry takes a column. The section takes the mode as a value, so a change of mode lays the feed out again. The heading sits in a `filigree()` between the type menu, bound to the page model's type, and the `FeedMode` switch, bound to `AppSettings`, both a `MutableTap`. A row carries `zenBg()`, and the feed stands its rows `currentTheme.line` apart, so the background behind shows through as the line between them, as the web's `ZenBg` rows and `Gap2Px` column do.

## Modifiers

A styling the web gives with a `koala.modifier` class, such as `BorderRadius2` or `MoonShadow`, is an extension on Compose's `Modifier` in `streetlight.compose.modifier`, named for its web cousin in Compose's camel case: `borderRadius(2)`, `outline()`, `moonShadow()`, `zenBg()`, `cardBg()`, `fadeClip()`, `opacityLow()`, `opacityHigh()`, `filigree()`. A length the web gives in units is given in units. The set holds what the app uses; it is not a copy of `koala.modifier`, and a styling Compose already gives plainly, such as a text color or a padding of `currentTheme.unit`, has none.

Each is a `Modifier.Node` that reads the theme with `currentValueOf(LocalKoalaAppTheme)`, so a call site needs no theme in hand and chains like any modifier. The theme holds the values, such as `KoalaOpacity`, and the node holds only how they are applied. A node that reshapes or fades its content places it with a layer; one that draws around it is a `DrawModifierNode`.

A modifier lives in the file that corresponds to its web cousin's stylesheet: `DisplayModifiers.kt` for `DisplayCss`, as borders, shadows and opacity, `MagicModifiers.kt` for `MagicCss`, as `filigree()` and its rays, and `TextModifiers.kt` for `TextCss` when one is needed. `ModifierUtility.kt` holds what the nodes share, such as their read of the theme.

Compose's `Modifier` is the only one in this module: `koala.modifier.Modifier` is never imported here.

## Cell Grid

`CellGrid` shows an entity's top and bottom details as cells, on one line when every cell gets at least 128dp, otherwise the top details over the bottom ones. The cells of a line share its width evenly, the pixels left over going to its first cells, so a line never ends in a cell of another width. Each cell is 5 units tall on `cardBg()`, its icon in the detail's color at its start and its text centered in the rest; a detail without text shows its icon alone at `opacityLow()`, in the same place. The cells stand a line apart, so the background draws the grid's lines, inside a container rounded two units, outlined, and in the moon's shadow.

A layout that changes with its width, such as `CellGrid` or the `Row` feed row, decides in its own `Layout`'s measure, from the width it is given. It needs no subcomposition, so it costs nothing per row.

## Markdown

`annotatedMarkdownOf` renders markdown, parsed by koala's `markdownBlocksOf`, as one `AnnotatedString` for text read as prose: each block on its own line, headings at the text's size a step heavier, emphasis, strong, code and struck text styled, and links in the color given. Images and rules are left out.

## Unit Spacing

Spacing is a whole multiple of `currentTheme.unit`, the shortest distance that shows two elements are separate. It is half the rem, as the web's `0.5rem`. Changing it sets the density of the whole app.

## Rem

`AppTheme` sizes the type and the unit from a rem chosen by the window's width: 16sp, and 14sp below 600dp, as the web's `html` drops to `87.5%` below 600px. It measures the window once, at the root. A layout that adapts to its own width measures that instead.

The unit builds the hierarchy of grouping. The elements of an item are one unit apart and the item has one unit of padding, so the content of two adjacent items is two units apart. A multiple is written `unit * 2`. A spacing that is not a whole unit is a named value. The one the theme holds is `line`, 2dp, the web's 2px: the gap between rows and the width of an outline, where a unit is too much and a single pixel too sharp. A CSS px and a dp are both reference pixels, and Compose lays out on whole device pixels, so 2px translates directly to 2.dp. Spacing between children is the parent's `Arrangement`, as `spacedBy(currentTheme.line)`, not a modifier.

## Animated Modifiers

A modifier that moves, such as `filigree()`, runs its own frame clock from the node's `coroutineScope`, launched in `onAttach`, and reads the time only while drawing, so a frame redraws its node alone.

A node that both places its content and draws, as `filigree()` does, draws in the space of the content it placed: the origin is the content's corner and `size` is the content's. Drawing beside the content uses the offsets the node measured.

## Context Parameters

A view declares its view model as the context parameter `model`, and the `Portal` when it navigates, following `docs/kotlin-style.md`. `App()` provides the `Portal` once, above `AppPortal`.

## Source Sets

| Source set | Holds |
|---|---|
| `commonMain` | All UI, starting from `App()` |
| `jvmMain` | `main()` and anything that reads the desktop platform, such as the `actual` of `AppFiles.kt` |

## Build

| Rule | Detail |
|---|---|
| Targets | `jvm()` only. An Android target lives in a module of its own |
| Compose libraries | Catalog entries over direct coordinates, as `libs.compose.foundation`. The plugin's `compose.*` aliases are deprecated, except `compose.desktop.currentOs` |
| Material | Excluded from `compose.desktop.currentOs`, which otherwise brings in `org.jetbrains.compose.material` |
| MapLibre | The desktop needs Java 25, the JVM flag `--enable-native-access=ALL-UNNAMED`, and one native runtime: `maplibre-compose-runtime-vulkan-linux-x64` for Linux. Another desktop OS adds its own runtime |
| Resources | `syncComposeResources` assembles `src/commonMain/composeResources` and `www/icon` into a build directory, which `compose.resources.customDirectory` makes `commonMain`'s resources. A resource is added to `src/commonMain/composeResources`, never to the build directory |

## Workflows

| Task | Command |
|---|---|
| Run on desktop | `./gradlew :compose:run` |
| Run with hot reload | `./gradlew :compose:hotRunJvm` |

To add a route: declare `FooRoute` in `AppRoute.kt`, write `FooView`, and add its `entry` to `AppPortal`. A route with a view model registers it in `appModule`.

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
| `FooScreen.kt` | The file of a route's `FooScreen` and its `FooView` |
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
| `MapWindow` | `initGeoMap` |
| `MapMount` | `geoMapMount` (`koala.dom.component`) |
| `MapHost` | `wireGeoMap` and `wireMapContext` (`koala.interop`), with `GeoCameraController` and `GeoRender` |
| `Markers` | `GeoLayerAdapter` (`koala.model`) |
| `Marker` | `toPointElement` and `MarkerStyle.Base` (`koala.model`) |
| `MarkerBody`, `FooMarkerBody` | `configureBody`, `configureFooMarker` (`koala.model`), `configureEventMarkerBody` |
| `MarkerThumb`, `MarkerLabel` and the other `MarkerFoo` parts | `markerThumb`, `markerLabel` and the other `markerFoo` parts (`koala.model`) |
| `SmallGalaxyCard` | `smallGalaxyCard` (`streetlight.web.layout`) |
| `ConnectPicker` | `connectPicker` (`streetlight.web.component`) |
| `AppFooter` | `appFooter` (`streetlight.web.page`), without its lottie |
| `EntityHeader` | `entityHeader` and its `pageHeader` card (`streetlight.web.layout`) |
| `Tabs`, `TabScope`, `tab` | `tabs`, its `TabScope` and `tab` (`koala.layout`) |
| `GalaxyView`, its `layoutBlock` | `galaxyShell` and `renderLayout` (`streetlight.web.layout`) |
| `EventView` | `eventShell` and `eventBody` (`streetlight.web.layout`) |
| `LocationView` | `locationShell` (`streetlight.web.shell`) |
| `EarthView` | `viewEarth` (`streetlight.web.view`) |
| `EarthHeader` | `earthControls` (`streetlight.web.view`); the title of `earthHeader` is the earth's `TopBar.Title` |
| `GalaxyMapView` | `viewEarth` (`streetlight.web.view`) on a galaxy's map |
| `MapFocus` | `earthFocus` (`streetlight.web.view`) |
| `renderLayout`, `renderBlock`, `RenderFoo` | `renderLayout`, `renderBlock`, `renderFoo` (`streetlight.web.layout`) |
| `AppHeader` | `appHeader` (`streetlight.web.page`), unlinked |
| `RouteDock` | `viewRouteDock`, its main routes |
| `HelmBar` | `helmBar` (`streetlight.web.page`) |
| `HelmMenu` | `siteHelmPopover` and `siteMenuItems` (`streetlight.web.page`) |
| `StarMenu` | `starHelmPopover` and `wireStarHelm`, with the sign-in form of `signInForm` |
| `TextLogo` | `textLogo` (`streetlight.web.component`) |
| `Button`, `ButtonStyle` | `btn` and `button` (`koala.component`), and the button classes of `ButtonCss`, such as `Zen` |
| `Popover` | `popoverMenu` (`streetlight.web.dom.component`) and its card |
| `TagFilter`, `EventTagMenu` | `tagFilterMenu` (`streetlight.web.component`) and `eventTagMenu` |
| `SearchField` | `searchField` (`koala.component`) |
| `TextField` | `textField` (`koala.component`) |
| `StarGate`, `SignInToContinue` | `starGate`, `signInToContinue` (`streetlight.web.view`) |
| `SignInForm` | `signInForm` (`streetlight.web.view`) |
| `ConfigBody` | `configBody` (`streetlight.web.view`) |
| `StarDashView` | `viewStarDash` (`streetlight.web.view`) |
| `EventScoutView`, `LocationScoutView`, `MediaForgeView` | `viewEventScout`, `viewLocationScout`, `viewMediaForge` (`streetlight.web.view`) |
| `CreatePostMenu` | `createPostMenu` (`streetlight.web.component`) and `wireCreatePost` |
| `SelectorContainer` | `selectorContainer` (`koala.dom.layout`) |
| `StageHeader` | `onboardStage` (`streetlight.web.view`), without its strikethrough |
| `LocationScoutView` | `viewLocationScout` and `locationFinder` (`streetlight.web.view`) |
| `StarConfigView` | `viewStarConfig` (`streetlight.web.view`), its account tab, with `registerAccountForm`, `changeEmailForm` and `changePasswordForm` |
| `ColumnRow` | `BodyStyle.ColumnRow` (`streetlight.web.layout`) |
| `FormCard` | `formCard` (`streetlight.web.dom.layout`) |
| `MarkdownEditor` | `markdownEditor` (`koala.dom.markdown`) |
| `ImageField` | `imageFormSection` and `imageDrop` (`streetlight.web.view`), with FileKit's picker |
| `LocationEditForm` | `locationEditFormBody` (`streetlight.web.view`) |
| `CheckBox` | `checkBox` (`koala.dom.component`) |
| `FormSection`, `FormHeading`, `FormBullets`, `FormSubmit` | `formSection`, `formHeading`, `formBullets`, `formSubmit` (`streetlight.web.dom.layout`) |
| `GuestRegistrationForm`, `FullRegistrationForm` | `guestRegistrationForm`, `fullRegistrationForm` (`streetlight.web.view`) |
| `RawDialogMount`, `DialogMount`, `DialogCard` | `dialog` with `rawDialogContent` or `dialogContent`, and `dialogCard` (`koala.dom.component`) |

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

## Route Dock

`HelmBar` and `RouteDock` are `AppTheme`'s `overlay`. The helm bar's menus open within the overlay, not in a `Popup`, so a button's icon is one shared element with its menu's. `RouteDock` floats over the bottom of the window, over the page and the map, and stays across pages; a page makes no room for it. It is `AppTheme`'s `overlay`, the page's sibling, since a Haze blur cannot draw a source it lies within.

A tray of the dock holds icons for utilities, never routes.

`AppPortal` decorates the entries of each stack on their own, so a hidden stack's pages keep their saved state and view models.

## Top Bar

A view declares its top bar with a `TopBarMount`, handing over a `TopBar` value, not a composable, and how far its content has scrolled under the bar. `TopBarHost`, in the overlay, draws the latest mount's bar in one look for every view, so the bar can blur the page, and so two views with the same bar share it unchanged. A view with a bar keeps `TopBarUnits` of room above its content. A bar known from the route is mounted by the screen, outside `Inflated`, so it stays while the content is read. A part that knows more, such as a `StarGate`'s content knowing the star, may mount a bar of its own within the screen's; the latest mount is shown, so the screen's returns when the part leaves, as the dash's `TopBar.TwoTone` gives way to the logo on sign-out. The helm and star stay reachable above the bar, whatever its fade.

## Star Gate

A screen of the star's own puts a `StarGate` ahead of `Inflated`, so its content is read only while a star is signed in, and the star is in context for what it shows. Signed out, it shows its fallback, by default the `SignInForm` on a card. Signing out leaves the star where they are; each gate turns to its fallback, so nothing of the star's stays on screen. What a gated screen shows may depend on the star's account type, as the star configuration shows a guest the form to register and a registered star their email and password; it changes with the star, so an upgrade shows the registered form at once.

## Forms

A form is laid out in `ColumnRow`s: columns side by side while each keeps 400dp, wrapping below that, none wider than 540dp, as the web's `ColumnRow`. A `FormSection` is such a column, and a `FormCard` holds a form's rows. A field asks for the keyboard a touch screen should offer, such as `KeyboardType.Uri` for a link or word capitals for a name, as a phone's own forms do.

## Markdown Editor

`MarkdownEditor` is a text field over the markdown as written, styled by a `VisualTransformation` that maps every offset to itself, so the caret, selection, undo and keyboard stay the platform's. Its styles come from `MarkdownStyler`. Every style is a span style: a paragraph style, such as a centered heading, lays its range out as a paragraph of its own, and the newline after it then adds a line that the text does not have, so headings stay at the start of their line until the editor maps offsets. Each line takes its own height. A style that would hide or move text, such as hiding the syntax, needs an offset mapping of its own.

## Dialogs

A dialog is declared with a `RawDialogMount`, its content from scratch, or a `DialogMount`, its content on a `DialogCard`, composed beside what opens it and open while its state is `true`. Unlike a top bar, it hands the `DialogHost` a composable, since a dialog's content is its own. The host, last in the overlay, shows one dialog at a time over the blurred app; opening another closes the first, and a click on the veil, Escape, or a change of route closes it.

## Page Layouts

A page with a design, such as a galaxy's or a location's, lays out its `PageLayout` with `renderLayout`, which adds each block to the view's lazy grid with `renderBlock`. The family keeps the web's names: the functions on the lazy scope are lower case, and the composable of each block is `RenderFoo`, after the web's `renderFoo`. A feed block is a `feedSection` of the grid; every other block is a full item showing `RenderBlock`.

A feed is available only at the top level of a layout. Within a tab, whose blocks are a `RenderColumn`, a feed block adds nothing. A block the app does not yet lay out shows `RenderPlaceholder`.

## Tabs

`Tabs` is a layout, not a view: it takes no model, and its lambda builds the tabs on a `TabScope` with `tab(label, icon) { content }`, as the web's does. The buttons share the container's width, wrapping below the web's 100px each, and the chosen tab's content slides in a short way from the side its button lies on as the other slides out the other way, never sweeping across the page. A tab remembers which is chosen across recomposition and a return to its page. A route that names a tab, as `StarDashRoute(tab)` does with its `StarDashTab`, opens its view on that tab through `initialTab`, so a place in the app can lead straight to it.

## Motion

A change of page fades the old page out over one `magic` interval, then the new one in over the next, as the web's portal does, and a return does the same. The new page's content is read from the start of the fade-out, so it is often ready as the page fades in; content that arrives later fades in within the page, and a loading message is held back for a moment so a quick read shows none.

A part that a page shares with the page it opens moves between them, the container transform: `AppPortal` sets the stage, `LocalSharedTransitionScope`, and both pages mark the part with `sharedPart(route, part)`, keyed by the route of the page they lead to, so the two find each other without passing anything. A `Frame` and a `Title` share their bounds, their contents fading between; an `Image` is one element on both pages. A galaxy card's frame, image and name grow into the galaxy's header, and a feed row's image and title into its page's header. A part without a route shares nothing.

## Composables

A view that scrolls is a `LazyGridView`, one lazy grid for the whole view, filled with sections built on `LazyGridScope`. `LazyStaggeredGridView` is its masonry cousin, each column stacking items of their own heights, filled with sections built on `LazyStaggeredGridScope`; a section is written for each scope it serves. On either, `fullItem` spans every column, and `pinnedItem` also stays composed off screen, for content costly to start anew. A `LazyStaggeredGridView` takes the feed's mode and spaces itself by it: its items a unit apart and a unit from its edges in `Grid` mode, so the rounded entries stand clear, and a line apart otherwise, each full item padded by the mode's `sideUnits`. It keeps the top bar's room at its top. The map and the feed heading stay where they are in either mode.

A composable draws its content and leaves its framing to the caller. It takes a `modifier` and applies it to its root, and the call site gives the corners, shadow, outline and surface, as `CellGrid(details, FeedRowMod.Cells)`. The composable keeps a styling of its own only where its content depends on it, as the cells of `CellGrid` need a background behind them for their gaps to draw its lines. A composable that clips to a shape of its own, such as `Button` or `SearchField`, takes a `backdrop` modifier, drawn within its shape beneath its background, so a caller can blur what lies behind it, as the earth's controls blur the map.

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

A color derived from the theme, such as `accentFg`, is a getter on `KoalaColors` mixing its sources the way `ThemeCss` does. An `EntityType` is given its color by `KoalaColors.colorOf`, as the web gives it a custom property, and no type the primary color. `muted` is the ink at the web's `--opacity-high`. A background that is a gradient, as `--primary-bg` and `--accent-bg` are, is a function of the surface's size on `KoalaColors`, such as `primaryBg(size)`, drawn by its modifier.

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

`Icon` draws an `Svg` tinted with a color read while drawing, filling the icon's shape as the web's mask does. It and `AnimatedIcon` draw in a square, the icon held to the aspect of its `viewBox`. `rememberSvgPainter` reads an icon's file once, keeps its bytes, and is empty while it loads. `AnimatedIcon` plays an icon's `animated` variant through `streetlight.compose.svg`, at a clock read while drawing.

## Map

`MapWindow` shows MapLibre's map in the site's style, `www/misc/fjord.json`, read from the server by URL as the web reads it, so the two share one style and a change to it reaches the app without a build. The style carries everything that needs no code, as `koala.md` specifies. The window is darkened toward its edges as the web's map overlay is, and `HomeView` frames it as on the web's home, with MapLibre's controls but not its logo.

The desktop window provides the map's presentation host in `main()`, through `ProvideMapPresentationHost`.

The app has one map. Starting MapLibre's renderer stalls the UI thread for most of a second, so `MapHost`, around the app in `App()`, composes the map once at launch and keeps it for the life of the app. A page shows it with a `MapMount` where the map goes, its center or an area to frame, and the radius of its corners; the mount holds the place in the page's layout and reports its bounds as the page lays out and scrolls, and the host draws the map there, above the page, in its own frame and outline. The map shows in the newest page's mount. When the mount it leaves and the new one both lie within the window, its frame grows or shrinks from one to the other over the pages' two fades, its corners following and its camera gliding to the new center, and the new mount's markers take the old ones' place as it starts. Otherwise it fades out, moves and looks over the mount's center, then fades in, in step with the page. A mount leaves the host as soon as its page starts to leave, so the map fades out with it. An area is fit once the map has the mount's frame, so a page frames its arrival through its mount, not a camera pan, which the mount's arrival would overwrite. A page never composes a map of its own, and a mount needs no pinning, since its leaving the composition costs nothing.

The map is drawn above the page, so whatever a page shows over its map, such as the earth's controls, is the mount's `overlay`, which the host draws in the map's frame above the markers; it fades and changes frame with the map. The overlay is drawn beside the map, not within MapLibre's own overlay, so the map and its markers can be the `LocalMapHaze` source a part of the overlay blurs, as `MapFocus` does. A page never lays out its own parts over a mount.

`MapHost` is the link between the map and `GeoMap`. As a mount becomes the one shown, the host hands its `MarkerMap`, or `null`, to `GeoMap`, so a page's markers show only while its map is in sight. The host applies `GeoCamera`'s pans to the map and reports the camera back into it.

A marker is a composable in the map's overlay, placed at its point with MapLibre's `placedAt` in the layout phase, so a camera move places it again without recomposing. A marker is drawn in two stages, as on the web. `Marker` is the base: it places the marker so its body is centered on its point, fades it in, grows it on focus, takes its click, and provides its type's color as the scheme color. The body is its content, chosen by `MarkerBody` from the marker's type, since a `MapMarker` is data and carries no composable. A body, such as `EventMarkerBody`, is built from the `MarkerFoo` parts: `MarkerBodyRow`, `MarkerThumb`, `MarkerIcon`, `MarkerLabelColumn`, `MarkerLabel` and `MarkerSublabel`, which read the scheme color as given. A new kind of marker adds a `MapMarker` type, its body, and a branch of `MarkerBody`. `Markers` composes one `Marker` per marker, keyed by its id. A click focuses it, and a focused marker grows by half; a click on the map itself clears the focus of the `MarkerMap` shown. A marker fades in as it is composed, whether newly set or newly revealed.

A crowded map stays readable by revealing markers by zoom, as the web's `GeoLayerAdapter` does. When the markers change, each is given a reveal zoom: stepping in from zoom 0 by half a zoom, each step reveals, in list order, the markers farther than 160dp on screen from every marker already revealed. The list's order is its priority. A marker is composed only from its reveal zoom, or while it is focused, read through `derivedStateOf` so a zoom recomposes only when the revealed set changes.

Every marker has a light, revealed or not, so a hidden marker shows as its light alone. The lights are one `Spacer` beneath the markers, drawn in its draw lambda: each light projected to the screen and drawn as a dot in its type's color near white, twinkling on a phase steady for its id, against a frame clock read only while drawing. It is the cousin of the web's `LightLayer` shader, and like it, redraws every frame while the map shows and never recomposes. Markers outside the view are not placed or drawn, but are composed; culling their composition to the camera waits for a map with many.

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
| `Grid` | No padding but a unit below. A `ContainImage` across the top in the image's own `aspect`, held between 3:4 and 4:1 and no taller than 48 units, contained within that height, or 3:2 when it has none; the title and description as in `Row`, clipped at 10 units with no inset, beside the flair badge; then the `CellGrid`. The parts below the image are inset a unit at the sides and stand a unit apart. The entry's corners are rounded three units |

A featured row, `isFeatured`, takes the `Grid` layout in any mode, with square corners and its text clipped at 24 units, as the web's `FeedRowStyle.Featured`. A map's focus features its first entity.

### Cell Grid

`CellGrid` lays out an entity's details as the web's `cellGrid` does: in as many equal columns of at least 128dp as fit, up to 7, wrapping onto more lines, the last cell stretching to the end of its line. The columns share the width evenly, the pixels left over going to the first columns. Each cell is 5 units tall on `cardBg()`, its icon, or else its label, at its start in the detail's color or the scheme color, and its text centered in the rest. A cell with a route opens the app's page of it, by `toAppRoute()`; a cell whose url leaves the app opens it. The cells stand a line apart, so the background draws the grid's lines. A feed row frames it rounded two and a half units, outlined, in the moon's shadow, on `schemeBg()` in the entity's scheme color.

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
| `model` | Entities, DTOs, `Api`, and `ApiClient` from `streetlight.model.io` |
| `kampfire.model` | `Store` and `Tap` |
| `koala.modifier` | The theme's defaults and types, `Rgb` and `Glow`, through `model` |

## Packages

| Package | Holds |
|---|---|
| `streetlight.compose.model` | Routes, `Portal`, `appModule`, services and view models |
| `streetlight.compose.io` | `AppTransport` |
| `streetlight.compose.view` | Composables that take a model or the `Portal`, including the view of each route |
| `streetlight.compose.utility` | Utilities whose target is not a compose type, such as `TapUtility.kt` and `ColorUtility.kt` |

## Naming

| Type | Pattern |
|---|---|
| `FooRoute` | A route, a `@Serializable` member of the sealed `AppRoute`, in `AppRoute.kt` |
| `FooView` | The composable a route renders |
| `FooModel` | The view model of `FooView`, such as `HomeModel`. Web names its models by role, since it has no `View` suffix to pair with |
| `FooState` | The state class a view model holds in its `Store` |

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
| `KoalaTypography` | `TypographyCss` and the text classes, such as `TextSmall` |
| `ThemeColor` | `ThemeColor` (`streetlight.web.model`) |
| `FeedRow`, `FeedMode` | `feedRow`, `FeedMode` |
| `Icon` | `icon` (`koala.component`) |
| `EntityUtility.kt` | `EntityUtility.kt` (`streetlight.web.model`) |
| `currentTheme.unit` | `--unit`, `KoalaTheme.spacingUnit` |
| `FooModel`, `FooState` | The models of `streetlight.web.model` |

## Navigation

`Portal` owns the back stack, which holds `AppRoute`s and is saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching the back stack.

`AppPortal` maps each route to its view with one `entry` in its `entryProvider`. Each entry gets its own saved state and its own view model store, so a view model lives while its route is on the back stack.

## Services

`appModule` is the Koin module holding the app's services and view models. `App()` starts Koin with it through `KoinApplication`, above the `Portal`. A composable reaches a service with `koinInject()`.

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

`Icon` draws an `Svg` tinted with a color read while drawing, filling the icon's shape as the web's mask does. `rememberSvgPainter` reads an icon's file once, keeps its bytes, and is empty while it loads. An animated variant draws its first frame.

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

`KoalaTypography`, read as `currentTheme.text`, holds the web's text styles, each a multiple of the rem as in `TypographyCss`: `heading1` to `heading6`, `paragraph`, `regular`, `small` and `large`, in Fira Sans. `regular` is the rem itself, the size of text with no style of its own. A style is named for its web class or tag, so `heading5` is `h5` and `small` is `TextSmall`. The web's `Bold` is `FontWeight.SemiBold`. Every style has the web body's 1.4 line height, its leading split evenly above and below the line as CSS does. A use that differs from a style copies it, as a feed row's title copies `regular` with the semi-bold weight and a 1.15 line height.

The fonts are in `composeResources/font`, with their license in `composeResources/files`, and are built into a `FontFamily` by `firaSans()`.

## Theme Colors

A color that depends on what it shows is a `ThemeColor`, resolved with `KoalaColors.colorOf`, as the web's `ThemeColor` is resolved through its custom properties. `Muted` is the ink at the web's `--opacity-high`. `accentFg` mixes the accent half and half with the ink, lighter than the web's `--accent-fg` at 75%.

## Entities

`EntityUtility.kt` holds the properties of an entity that depend on its type, each a `when` over every entity type, as the web's does. `topDetails` and `bottomDetails` are the `EntityDetail`s a feed row shows above and below its title, each a `ThemeColor` and a text.

## Feed Rows

`FeedRow` shows any `Entity`, laid out by `FeedMode`. It has only `Minimal` so far.

The layout is recorded here so its intent survives later changes.

| Mode | Entry |
|---|---|
| `Minimal` | One row with a unit of padding: a square thumbnail of 10 units over a backdrop of the entity's color, then a column of the top details, the title in `regular` semi-bold on up to two lines, and the bottom details, held to the top of the row. The thumbnail and column are a unit apart. Details are `small`, on one line each, joined by a muted dot and cut with an ellipsis |

A feed separates its rows with a 1dp line in `outlineLow`.

## Unit Spacing

Spacing is a whole multiple of `currentTheme.unit`, the shortest distance that shows two elements are separate. It is half the rem, as the web's `0.5rem`. Changing it sets the density of the whole app.

## Rem

`AppTheme` sizes the type and the unit from a rem chosen by the window's width: 16sp, and 14sp below 600dp, as the web's `html` drops to `87.5%` below 600px. It measures the window once, at the root. A layout that adapts to its own width measures that instead.

The unit builds the hierarchy of grouping. The elements of an item are one unit apart and the item has one unit of padding, so the content of two adjacent items is two units apart. A multiple is written `unit * 2`. A spacing that is not a whole unit is a named value.

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
| Resources | `syncComposeResources` assembles `src/commonMain/composeResources` and `www/icon` into a build directory, which `compose.resources.customDirectory` makes `commonMain`'s resources. A resource is added to `src/commonMain/composeResources`, never to the build directory |

## Workflows

| Task | Command |
|---|---|
| Run on desktop | `./gradlew :compose:run` |
| Run with hot reload | `./gradlew :compose:hotRunJvm` |

To add a route: declare `FooRoute` in `AppRoute.kt`, write `FooView`, and add its `entry` to `AppPortal`. A route with a view model registers it in `appModule`.

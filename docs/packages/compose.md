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
| `KoalaAppTheme` | `KoalaTheme`, and the custom properties `ThemeCss` builds from it |
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

## Theme

The theme has two levels: `KoalaAppTheme`, the app's own values, and `PageTheme`, the colors and glows a piece of content brings over them.

`AppTheme` wraps the app below Koin. It shows the `PageTheme` held by `ThemeHost`, draws its three glows behind the content, and provides the `KoalaAppTheme` as `LocalKoalaAppTheme`. A composable reads it through the top-level property `currentTheme`, after Compose's `current` prefix for ambient reads, flat, as `currentTheme.unit` and `currentTheme.primary`. A view whose content carries a theme passes it to `ThemeHost.setTheme`.

A change of theme animates over `KoalaTheme.ThemeTransitionSeconds` with the web's bounce easing. In `Flux` mode the glow colors turn around the hue wheel every 30 seconds, as the web's `hueSpin` does.

Every value that animates is read inside a draw lambda, never in composition, so an animation frame redraws and does not recompose. A composable that passes a color as a lambda, where the API takes one, first holds the theme in a local, since `currentTheme` is read only in composition:

```kotlin
val theme = currentTheme
BasicText(text, color = { theme.ink })
```

A color derived from the theme, such as `accentFg`, is a getter on `KoalaAppTheme` mixing its sources the way `ThemeCss` does.

## Unit Spacing

Spacing is a whole multiple of `currentTheme.unit`, the shortest distance that shows two elements are separate. It is `8.dp`, the web's `0.5rem`. Changing it sets the density of the whole app.

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

## Workflows

| Task | Command |
|---|---|
| Run on desktop | `./gradlew :compose:run` |
| Run with hot reload | `./gradlew :compose:hotRunJvm` |

To add a route: declare `FooRoute` in `AppRoute.kt`, write `FooView`, and add its `entry` to `AppPortal`. A route with a view model registers it in `appModule`.

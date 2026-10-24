# Module compose

## Introduction

The Streetlight native app, built with Compose Multiplatform. It runs on desktop. The iOS app is SwiftUI over `model` and shares no UI with this module.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.foundation` | Layout, text and input. Material is not used |
| `androidx.navigation3` | The back stack and `NavDisplay` |
| `androidx.lifecycle` | `ViewModel`, and its store per back stack entry |
| `model` | Entities, DTOs and `Api` |
| `kampfire.model` | `Store` and `Tap` |

## Packages

| Package | Holds |
|---|---|
| `streetlight.compose.model` | Routes, `Portal` and view models |
| `streetlight.compose.view` | Composables that take a model or the `Portal`, including the view of each route |
| `streetlight.compose.utility` | Utilities whose target is not a compose type, such as `TapUtility.kt` |

## Naming

| Type | Pattern |
|---|---|
| `FooRoute` | A route, a `@Serializable` member of the sealed `AppRoute`, in `AppRoute.kt` |
| `FooView` | The composable a route renders |
| `Foo` | A view model, named for its role as in `streetlight.web.model`, such as `EventFeed` |
| `FooState` | The state class a view model holds in its `Store` |

## Navigation

`Portal` owns the back stack, which holds `AppRoute`s and is saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching the back stack.

`AppPortal` maps each route to its view with one `entry` in its `entryProvider`. Each entry gets its own saved state and its own view model store, so a view model lives while its route is on the back stack.

## View Models

A view model extends `ViewModel`, holds one `Store` of its `FooState`, and exposes lenses as `streetlight.web.model` specifies. Its coroutines run in `viewModelScope`.

The route's entry builds it with `viewModel { }` and provides it to the view as a context parameter.

```kotlin
entry<FooRoute> {
    context(viewModel { Foo() }) { FooView() }
}
```

A view reads a lens with `collectAsState()` from `TapUtility.kt`.

```kotlin
val events by feed.eventsState.collectAsState()
```

## Context Parameters

A view declares the `Portal` and its view model as context parameters, following `docs/kotlin-style.md`. `App()` provides the `Portal` once, above `AppPortal`.

## Source Sets

| Source set | Holds |
|---|---|
| `commonMain` | All UI, starting from `App()` |
| `jvmMain` | `main()` and anything that reads the desktop platform |

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

To add a route: declare `FooRoute` in `AppRoute.kt`, write `FooView`, and add its `entry` to `AppPortal`.

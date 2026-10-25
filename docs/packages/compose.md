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
| `koala` | The theme's defaults and types, the icon packs, and the markdown parser, through `model` |

## Packages

| Package | Holds |
|---|---|
| `streetlight.compose.model` | Routes, `Portal`, `appModule`, services, view models, and the properties of entities |
| `streetlight.compose.io` | `AppTransport`, its cookie storage, app files, and the image loader |
| `streetlight.compose.view` | The theme, and composables that take a model or the `Portal`, including the view of each route |
| `streetlight.compose.modifier` | `Modifier` extensions for the theme's styling, the cousins of `koala.modifier` |
| `streetlight.compose.markdown` | Markdown rendered for Compose from koala's parsed blocks |
| `streetlight.compose.svg` | The renderer of animated icons |
| `streetlight.compose.utility` | Utilities whose target is not a compose type, such as `TapUtility.kt` and `ColorUtility.kt` |

## Web Counterparts

Most of the module has a cousin in `web` or `koala`, and each package document lists its own. A part with a counterpart follows that counterpart's specification, except where its document says otherwise, and its document names the counterpart where it is introduced.

Compose's `Modifier` is the only one in this module: `koala.modifier.Modifier` is never imported here.

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

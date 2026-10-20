# Package streetlight.web.subApp

## Introduction

The browser half of each sub-app: its start, its portal, its Koin module and its content fetcher. `web.md` under Sub-Apps states where the other parts of a sub-app live.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.web.shell` | The sub-app's shells |
| `streetlight.web.interop` | The `wire` functions and the global functions a sub-app takes from the app |
| `streetlight.web.io` | `ApiClient` |
| `streetlight.model.ui` | The sub-app's screens and routes |
| `koala.dom`, `koala.model` | Views, `Portal`, `RouteInflator` |

## Files

| File | Holds |
|---|---|
| `viewFooApp.kt` | `viewFooApp()`: Koin with `fooAppModule`, the root view on `KoalaBody.PortalMount`, the portal, and the `wire` and global functions the sub-app uses |
| `viewFooPortal.kt` | `viewFooPortal()`: a `flowBlock` on `Portal.screenState` with a `when` over the screen, and the view of each route |
| `fooAppModule.kt` | The services of the sub-app. A service is added only when a view of the sub-app reads it |
| `FooAppFetcher.kt` | The `ContentFetcher` of the sub-app's routes |

## Rules

The portal sets `RouteView.activeScope` as `viewPortal` does, since the feed functions read it.

A sub-app has no sign-in. It reads no user and wires nothing that needs one.

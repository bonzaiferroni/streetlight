# Package streetlight.web.model

## Introduction

View models. Each holds the state behind one view and the calls that change it.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | DTOs and routes |
| `streetlight.web.io` | `ApiClient` |
| `kampfire.model` | `Store`, `Tap`, `Messenger` and `Outcome` |
| `koala.model` | `Portal` and `GeoCamera` |
| `kotlinx.coroutines` | View model work |

## Construction

A view model takes its collaborators through its constructor. The view builds it, so the model never reaches into the container itself.

```kotlin
val model = FeedbackHub(contentScope, api, toaster)
```

A model needing more than a handful of services gets a factory extension on `AppContainer`, which supplies the container's half and takes the view's half as parameters.

```kotlin
fun AppContainer.getGalaxyEditor(galaxy: GalaxyEdit, scope: CoroutineScope) =
    GalaxyEditor(galaxy, scope, koin.get(), koin.get(), koin.get(), koin.get())
```

`RouteDock` is an app-wide model, registered in `appModule` and documented in `streetlight.web.ui.md`.

## Scope

A view model takes the view's `contentScope`. Its coroutines then end when the content is replaced, so a request outlives neither the view that wanted it nor the page it was drawn on.

## State

A model holds one `Store` of a single state class, and exposes lenses onto it.

| Exposure | Built with | For |
|---|---|---|
| A read | `tapOf` | Content a view renders |
| A write | `mutableTapOf` | A field a control edits |

A control binds to the narrowest lens that covers it, so editing one field leaves the rest of the state untouched.

## Messages

A method that reports to the user takes a `Messenger` parameter. The view decides where the message appears, the model decides what it says. `streetlight.web.ui` states the conventions the messages follow.

## Editing

A model editing a record holds an edit DTO in its state. On a successful send it clears the fields the sender filled and keeps the choices they made, so a second send starts from the same settings.

## Viewer Settings

A setting the viewer keeps across sessions is a field of `SiteConfigState`, exposed as a lens on `SiteConfig`. A model that follows the setting takes `SiteConfig` from the container and declares the lens as its own property, such as `postAndResetState`, so its view binds to the model.

## Posting

A scout's `galaxy` is optional. Without one, `post()` saves the record and creates no post row, and the record is found through its city.

A scout reports a successful post to the `Toaster`, since the view it posted from may be gone. With `postAndResetState` set, it calls `reset()` and returns to its first stage. Otherwise it sets `isPosted`, which the view follows to the galaxy, or to `HomeRoute` when there is no galaxy.

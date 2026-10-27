# Package streetlight.compose.model

## Introduction

The app's routes and back stack, its services and view models, and the properties of entities that depend on their type.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.navigation3` | `NavKey` and `NavBackStack` |
| `androidx.lifecycle` | `ViewModel` |
| `koin` | `appModule` |
| `kampfire.model` | `Store`, `Tap` and `MutableTap` |
| `streetlight.model.io` | `ApiClient` |
| `koala` | `SvgFile` packs, for details and flair |

## Naming

| Type | Pattern |
|---|---|
| `FooRoute` | A route, a `@Serializable` member of the sealed `AppRoute`, in `AppRoute.kt` |
| `FooModel` | The view model of `FooView`, such as `HomeModel`. The web names its models by role, since it has no `View` suffix to pair with |
| `FooState` | The state class a model holds in its `Store` |

## Web Counterparts

| Compose | Web |
|---|---|
| `AppRoute` | `WebRoute` (`koala.model`) |
| `Portal` | `Portal` (`koala.model`) |
| `appModule` | `webModule` |
| `AppSettings` | The root switches, such as `FeedRowStyle.Mode` |
| `FooModel`, `FooState` | The models of `streetlight.web.model` |
| `EntityUtility.kt` | `EntityUtility.kt` (`streetlight.web.model`) |
| `FlairIcon`, `toFlair` | `FlairIcon`, `toFlair` (`streetlight.web.model`) |
| `FeedUtility.kt` | `heading` and `toSvg` beside `feedSection` (`streetlight.web.layout`) |
| `GeoMap`, `GeoCamera` | `GeoMap`, `GeoCamera` (`koala.model`) |
| `MarkerMap` | `MarkerMap` (`streetlight.web.model`) |
| `MapMarker`, `ThumbMarker`, `IconMarker`, `EventMarker`, `toMarker` | The markers of `StreetMarker.kt` and `createMarker` (`streetlight.web.model`) |

## Submodels

State and logic that several view models share is a submodel: a plain class, not a `ViewModel`, that a view model builds with its `viewModelScope` and the collaborators it needs, and holds as a property. `FeedModel` is one: a feed shown in a view, starting from the page its view's content holds, its type, the mode it is laid out in, and the tag and search that filter it, read again in the scope it was given. A view model holds one per feed, as `HomeModel.feed`, and a view passes it to the parts that show it as a context parameter.

`MarkerMap` is another: the markers a view shows on the map and the one in focus, built with `GeoMap`. A view model sets its markers, as from its feed's entities, and never clears them; the map shows them only while the view's map is in sight. Focus belongs to the `MarkerMap`. Culling to the camera is the renderer's, not the view model's.

A marker is a `MapMarker`, a plain description of what to draw, built from an entity by `toMarker()`.

## Inflation

`Inflator` reads the content of each route, one branch of a `when` over the sealed `AppRoute`, so a new route cannot be added without saying how its content arrives. `InflationModel` holds a route's `Inflation` while the route is on the back stack, so a return shows its content without reading it again. A route whose content arrives another way, as the earth's does with the camera, gives `Ok(Unit)` there, and its screen shows its view without inflating. Only a screen touches either; a view model is built with its content, as `GalaxyModel(content, …)`.

## Navigation

`Portal` owns the back stack, which holds `AppRoute`s and is saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching the back stack: `open` a route, go `back`, or return `home` to the first. It is ambient, provided as `LocalPortal` and read as `currentPortal`. An entity's page is its `toRoute()`, `null` for a type the app has no page for. A web route from the shared model, such as a detail's `route`, becomes the app's with `toAppRoute()`, `null` for a page the app does not have. It is built by `rememberPortal`, not by Koin, since the back stack is Compose saved state.

## Services

`appModule` is the Koin module holding the app's services and view models. A service or view model takes its collaborators through its constructor, and Koin supplies them.

| Service | Holds |
|---|---|
| `AppConfig` | The settings of this build, such as the server's URL |
| `AppSettings` | The viewer's settings that hold across the app, each a lens, such as the feed mode |
| `ThemeHost` | The `PageTheme` the app shows |
| `Inflator` | The read of each route's content |
| `GeoMap` | The app's one map: its `GeoCamera` and the `MarkerMap` it shows |
| `GeoCamera` | The requests to pan the map, and where it looks as the map reports it |

## View Models

A view model extends `ViewModel`, holds one `Store` of its `FooState`, and exposes lenses as `streetlight.web.model` specifies. Its coroutines run in `viewModelScope`. A view model that calls the API takes `ApiClient`.

It is registered in `appModule` with `viewModelOf(::FooModel)`. The route's entry gets it with `koinViewModel()`, which scopes it to the entry, as `streetlight.compose.view.md` shows.

## Entities

`EntityUtility.kt` holds the properties of an entity that depend on its type, each a `when` over every entity type, as the web's does.

An entity's details and its `EntityType` come from `streetlight.model.data`, shared with the web, as `streetlight.model.md` specifies. The theme in `streetlight.compose.view` gives a type its color.

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
| `DockRoute`, `StableDock` | `RouteDock` and `RouteDockState` (`streetlight.web.model`), the main routes |
| `appModule` | `webModule` |
| `AppSettings` | The root switches, such as `FeedRowStyle.Mode` |
| `FooModel`, `FooState` | The models of `streetlight.web.model` |
| `EntityUtility.kt` | `EntityUtility.kt` (`streetlight.web.model`) |
| `FlairIcon`, `toFlair` | `FlairIcon`, `toFlair` (`streetlight.web.model`) |
| `FeedUtility.kt` | `heading` and `toSvg` beside `feedSection` (`streetlight.web.layout`) |
| `GeoMap`, `GeoCamera` | `GeoMap`, `GeoCamera` (`koala.model`) |
| `MarkerMap` | `MarkerMap` (`streetlight.web.model`) |
| `StarSession` | `SessionClient` (`streetlight.web.model`) |
| `ImageEditor` | `ImageEditor` (`streetlight.web.model`), holding the picked bytes where the web holds a blob |
| `LocationEditor` | `LocationEditor` (`streetlight.web.model`), as a submodel |
| `LocationScout` | `LocationScout` (`streetlight.web.model`) |
| `AccountEditor` | `AccountEditor` (`streetlight.web.model`), with the web's `EmailEditor` and `PasswordEditor` folded in |
| `UserCreator` | `UserCreator` (`streetlight.web.model`), with the web's `EmailEditor` and `PasswordEditor` folded in |
| `EarthModel`, `EarthCache` | `Earth`, `EarthCache` (`streetlight.web.model`) |
| `MapMarker`, `ThumbMarker`, `IconMarker`, `EventMarker`, `toMarker` | The markers of `StreetMarker.kt` and `createMarker` (`streetlight.web.model`) |

## Submodels

State and logic that several view models share is a submodel: a plain class, not a `ViewModel`, that a view model builds with its `viewModelScope` and the collaborators it needs, and holds as a property. `FeedModel` is one: a feed shown in a view, starting from the page its view's content holds, its type, the mode it is laid out in, and the tag and search that filter it, read again in the scope it was given. A view model holds one per feed, as `HomeModel.feed`, and a view passes it to the parts that show it as a context parameter.

`LocationEditor` is another: a location's edit, its fields and its saving, with an `ImageEditor` that holds a picked image until the save uploads it, held by any view model that edits a location, as `LocationScout` holds one for a new location.

`MarkerMap` is another: the markers a view shows on the map and the one in focus, built with `GeoMap`. A view model sets its markers, as from its feed's entities, and never clears them; the map shows them only while the view's map is in sight. Focus belongs to the `MarkerMap`. Culling to the camera is the renderer's, not the view model's. `GeoCamera` reports whichever map is shown, so a submodel that reads the camera, such as `EarthCache`, acts only while `GeoMap` shows its own `MarkerMap`.

The web's earth routes change the address bar; the app has none, so a route is a place the viewer goes, and a refinement of what it shows, such as a tag or a search, is state on its model. Each earth route is its own entry with its own model.

A staged form's model holds the focus of the star's work, such as `LocationScoutFocus`, and its stage follows from the focus, an enum whose order is the stages' order. A stage is left by changing the focus, so going back to an earlier stage is a move like any other.

A model of a part that belongs to no route, such as a dialog's form, is a plain class built where the part is shown, with that composition's scope, as the create-account dialog builds `UserCreator`.

A marker is a `MapMarker`, a plain description of what to draw, built from an entity by `toMarker()`, and carrying that entity, so a focus can show it or, for an `EventGroup`, read the location's events.

## Inflation

`Inflator` reads the content of each route, one branch of a `when` over the sealed `AppRoute`, so a new route cannot be added without saying how its content arrives. `InflationModel` holds a route's `Inflation` while the route is on the back stack, so a return shows its content without reading it again. A route whose content arrives another way, as the earth's does with the camera, gives `Ok(Unit)` there, and its screen shows its view without inflating. Only a screen touches either; a view model is built with its content, as `GalaxyModel(content, …)`. A screen that creates a post reads the galaxy its route names, if any, as a `PostGalaxy`, so a route with a galaxy's slug posts to that galaxy and one without posts to none. Content that differs by who reads it, such as the star's own account, is read and modeled under a key of the reader, so a star who signs in after another reads their own.

## Navigation

`Portal` owns a back stack for each route of the dock, each starting at its route, as a tab bar keeps a stack per tab. It shows the stack of its `dockRoute` above the stack of its `start`, the first dock route, so back from the first route of another stack returns to the start's. The stacks hold `AppRoute`s and are saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching a stack: `open` a route on the shown stack, go `back`, `dock` a dock route to show its stack, or to return to its first route when it is shown, `swap` the route on top for another, or return `home` to the start. It is ambient, provided as `LocalPortal` and read as `currentPortal`. An entity's page is its `toRoute()`, `null` for a type the app has no page for. A web route from the shared model, such as a detail's `route`, becomes the app's with `toAppRoute()`, `null` for a page the app does not have. It is built by `rememberPortal` from the routes of a dock, `StableDock` by default, not by Koin, since the stacks are Compose saved state.

A route with a focus gives its `FocusDock` with `toFocusDock()`, built from the route alone. A focus dock's routes `swap` with each other rather than stacking.

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
| `StarSession` | The star signed in on this device |

## Sign In

The app signs in as a browser does: the server's session cookie is kept in the app's cookie file, created readable by the user alone, and goes with every request. A session always stays signed in, as the device is taken to be one person's, and lasts until the star signs out. One star is signed in at a time. `StarSession` reads the star of the saved session at launch.

## View Models

A view model extends `ViewModel`, holds one `Store` of its `FooState`, and exposes lenses as `streetlight.web.model` specifies. Its coroutines run in `viewModelScope`. A view model that calls the API takes `ApiClient`.

It is registered in `appModule` with `viewModelOf(::FooModel)`. The route's entry gets it with `koinViewModel()`, which scopes it to the entry, as `streetlight.compose.view.md` shows.

## Entities

`EntityUtility.kt` holds the properties of an entity that depend on its type, each a `when` over every entity type, as the web's does.

An entity's details and its `EntityType` come from `streetlight.model.data`, shared with the web, as `streetlight.model.md` specifies. The theme in `streetlight.compose.view` gives a type its color.

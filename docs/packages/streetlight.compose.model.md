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

## Submodels

State and logic that several view models share is a submodel: a plain class, not a `ViewModel`, that a view model builds with its `viewModelScope` and the collaborators it needs, and holds as a property. `FeedModel` is one: a feed shown in a view, its type, the mode it is laid out in, and the tag and search that filter it, read again in the scope it was given. A view model holds one per feed, as `HomeModel.feed`, and a view passes it to the parts that show it as a context parameter.

## Navigation

`Portal` owns the back stack, which holds `AppRoute`s and is saved with `AppRoute.serializer()`. A view navigates through `Portal`, never by touching the back stack. It is built by `rememberPortal`, not by Koin, since the back stack is Compose saved state.

## Services

`appModule` is the Koin module holding the app's services and view models. A service or view model takes its collaborators through its constructor, and Koin supplies them.

| Service | Holds |
|---|---|
| `AppConfig` | The settings of this build, such as the server's URL |
| `AppSettings` | The viewer's settings that hold across the app, each a lens, such as the feed mode |
| `ThemeHost` | The `PageTheme` the app shows |

## View Models

A view model extends `ViewModel`, holds one `Store` of its `FooState`, and exposes lenses as `streetlight.web.model` specifies. Its coroutines run in `viewModelScope`. A view model that calls the API takes `ApiClient`.

It is registered in `appModule` with `viewModelOf(::FooModel)`. The route's entry gets it with `koinViewModel()`, which scopes it to the entry, as `streetlight.compose.view.md` shows.

## Entities

`EntityUtility.kt` holds the properties of an entity that depend on its type, each a `when` over every entity type, as the web's does.

An entity's details and its `EntityType` come from `streetlight.model.data`, shared with the web, as `streetlight.model.md` specifies. The theme in `streetlight.compose.view` gives a type its color.

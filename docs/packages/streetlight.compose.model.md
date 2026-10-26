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
| `SchemeColor` | `ThemeColor` (`streetlight.web.model`) |
| `FlairIcon`, `toFlair` | `FlairIcon`, `toFlair` (`streetlight.web.model`) |
| `FeedUtility.kt` | `heading` and `toSvg` beside `feedSection` (`streetlight.web.layout`) |

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

`topDetails` and `bottomDetails` are the `EntityDetail`s a feed row shows, each a `SchemeColor`, an icon and a text. An entity type always gives the same details, in the same order; a fact the entity lacks keeps its detail with `null` text, so its cell still shows. A fact of the entity itself takes the entity's color, as an event's cost and tag take the event's; a fact drawn from another entity takes that entity's, as an event's location takes the location's.

A color that depends on what it shows is a `SchemeColor`, resolved by the theme in `streetlight.compose.view`.

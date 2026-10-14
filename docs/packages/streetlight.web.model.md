# Package streetlight.web.model

## Introduction

View models, each holding the state behind one view and the calls that change it, and the app's services, in `jsMain`. Models the server also renders from, such as the entity properties, `FlairIcon` and `ThemeColor`, live in `commonMain`.

## Dependencies

| Package | Provides |
|---|---|
| `streetlight.model` | DTOs and routes |
| `streetlight.web.io` | `ApiClient` |
| `kampfire.model` | `Store`, `Tap`, `Messenger` and `Outcome` |
| `koala.model` | `Portal` and `GeoCamera` |
| `kotlinx.coroutines` | View model work |

## Services

`appModule` is the Koin module holding every browser-side service. `AppContainer` wraps the resulting Koin instance and is reached through `ViewScope.app`.

A view model with per-view state is not registered in the module. It is built by a factory extension on `AppContainer` that takes the scope and any starting value, so each view gets its own.

## Entity Properties

A property that depends on the entity type is an `Entity` extension function in `EntityUtility.kt`, resolved by a `when` over every entity type. A new `Entity` type adds a branch to each.

| Form | Used when | Example |
|---|---|---|
| `toFoo()` extension function | The entity alone decides the value | `Entity.toCells()` |
| `fooOf(entity, …)` function | The value also depends on where it is shown | |

A property returns `null` for content the entity does not have, and a parameter that receives it is nullable.

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

`RouteDock` is an app-wide model, registered in `appModule` and documented in `streetlight.web.view.md`.

## Scope

A view model takes the view's `contentScope`. Its coroutines then end when the content is replaced, so a request outlives neither the view that wanted it nor the page it was drawn on.

## State

A model holds one `Store` of a single state class, and exposes lenses onto it.

| Exposure | Built with | For |
|---|---|---|
| A read | `tapOf` | Content a view renders |
| A write | `mutableTapOf` | A field a control edits |

A control binds to the narrowest lens that covers it, so editing one field leaves the rest of the state untouched.

### Route Filters

A filter a route carries, such as the tag of `EventsMapRoute`, is read from the route as a lens on `Portal.routeState`. A control that sets it calls a model method, which navigates with the new value. A filter change replaces the current route rather than appending to the backstack.

A filter edited faster than the route, such as search text behind a debounce, keeps its own `Store`, seeded from the current route. The model navigates when the debounced value differs from the route's. It copies a route's value into the store only when the route is not the one it last issued, as on a back navigation, so a route that lands while the viewer types does not overwrite the field.

## Messages

A method that reports to the user takes a `Messenger` parameter. The view decides where the message appears, the model decides what it says. Without an obvious place in the view for the message, the model reports through its injected `Toaster` instead. `streetlight.web.view` states the conventions the messages follow.

## Editing

A model editing a record holds an edit DTO in its state. On a successful send it clears the fields the sender filled and keeps the choices they made, so a second send starts from the same settings.

## Submodels

Functionality two models share lives in a submodel named with the suffix `Editor`, such as `EmailEditor` and `LeadEditor`. A model holds its submodel as a property, built in its own constructor, and the view reaches it through the model. The submodel holds its own state, publishes it, and makes its own API call, so the base models stay clean and the work is written once. What differs between its users, such as a `LeadEditor`'s `LeadType`, is passed through its constructor.

## Images

An `ImageEditor` takes the `ImageTarget` of the record it edits, and uploads at that target's sizes. An image a model uploads without one, such as a layout block's, names its target in the `uploadImage` call.

## Viewer Settings

A setting the viewer keeps across sessions is a field of `SiteConfigState`, exposed as a lens on `SiteConfig`. A model that follows the setting takes `SiteConfig` from the container and declares the lens as its own property, such as `postModeState`, so its view binds to the model.

## Posting

A scout's `galaxy` is optional. Without one, `post()` saves the record and creates no post row, and the record is found through its city.

A scout reports a successful post to the `Toaster`, since the view it posted from may be gone. Its post mode, `LocationPostMode` or `EventPostMode`, decides what follows.

| Mode | After the post |
|---|---|
| `ResetLocation` | `reset()`, back to the location search |
| `ResetEvent` | `resetEvent()`, back to the event search at the same location |
| `Return` | Sets `isPosted`, which the view follows to the galaxy, or to `HomeRoute` when there is no galaxy |

## Models

### Rendered by the Server

| Model | Role |
|---|---|
| [`toCells`, `toButtons`…](../../web/src/commonMain/kotlin/streetlight/web/model/EntityUtility.kt) | The entity properties, as specified under Entity Properties |
| [`FlairIcon`](../../web/src/commonMain/kotlin/streetlight/web/model/FlairIcon.kt) | The icon badge of an entity type |
| [`ThemeColor`](../../web/src/commonMain/kotlin/streetlight/web/model/ThemeColor.kt) | The color of an entity type |
| [`SiteDoc`](../../web/src/commonMain/kotlin/streetlight/web/model/SiteDoc.kt) | The site's own docs, such as About and Privacy |

### Services

| Model | Role |
|---|---|
| [`appModule`](../../web/src/jsMain/kotlin/streetlight/web/model/appModule.kt) | The app's services, and the factories of per-view models |
| [`AppFacade.api, AppFacade.portal…`](../../web/src/jsMain/kotlin/streetlight/web/model/StreetlightFacade.kt) | Services reached from any `AppFacade` |
| [`SessionClient`](../../web/src/jsMain/kotlin/streetlight/web/model/SessionClient.kt) | The signed-in star, and sign-in and sign-out |
| [`AppContentFetcher`](../../web/src/jsMain/kotlin/streetlight/web/model/AppContentFetcher.kt) | The content of each route |
| [`Toaster`](../../web/src/jsMain/kotlin/streetlight/web/model/Toaster.kt) | Messages shown as toasts |
| [`RouteDock`](../../web/src/jsMain/kotlin/streetlight/web/model/RouteDock.kt) | The state of the route dock |
| [`SiteConfig`](../../web/src/jsMain/kotlin/streetlight/web/model/SiteConfig.kt) | The viewer's site settings |
| [`DataCache`](../../web/src/jsMain/kotlin/streetlight/web/model/DataCache.kt) | Data loaded once per session |
| [`StarCache`](../../web/src/jsMain/kotlin/streetlight/web/model/StarCache.kt) | The items a star has lit |
| [`Fleet`](../../web/src/jsMain/kotlin/streetlight/web/model/Fleet.kt) | A list loaded on first request |
| [`readDeviceAgent`](../../web/src/jsMain/kotlin/streetlight/web/model/DeviceAgent.kt) | The browser described for a bug report |

### Editors

| Model | Role |
|---|---|
| [`AccountEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/AccountEditor.kt) | Edits the signed-in account |
| [`ProfileEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ProfileEditor.kt) | Edits the star's profile and its design |
| [`UserCreator`](../../web/src/jsMain/kotlin/streetlight/web/model/UserCreator.kt) | Creates an account and signs in |
| [`CredentialStore`](../../web/src/jsMain/kotlin/streetlight/web/model/CredentialStore.kt) | The sign-in form |
| [`EmailEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/EmailEditor.kt) | An email field |
| [`PasswordEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/PasswordEditor.kt) | A password field with its confirmation |
| [`AppConnector`](../../web/src/jsMain/kotlin/streetlight/web/model/AppConnector.kt) | The payment handles a star links to their profile |
| [`SellerEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/SellerEditor.kt) | The star's seller account and its onboarding |
| [`CityEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/CityEditor.kt) | Edits a city |
| [`EventEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/EventEditor.kt) | Creates or updates an event |
| [`LocationEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LocationEditor.kt) | Creates or updates a location |
| [`MediaEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/MediaEditor.kt) | Creates or updates media |
| [`GalaxyEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/GalaxyEditor.kt) | Creates or updates a galaxy |
| [`ImageEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ImageEditor.kt) | Uploads a picked image |
| [`DesignEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/DesignEditor.kt) | Edits the layout and theme of a page |
| [`LayoutEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LayoutEditor.kt) | Edits a page layout as a tree of blocks and containers |
| [`ContainerEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ContainerEditor.kt) | One container of a layout |
| [`BlockEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/BlockEditor.kt) | One block of a layout |
| [`buildBlock`](../../web/src/jsMain/kotlin/streetlight/web/model/buildBlock.kt) | Rebuilds a block from its edited containers |
| [`ThemeEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/ThemeEditor.kt) | Edits a page theme |
| [`LeadEditor`](../../web/src/jsMain/kotlin/streetlight/web/model/LeadEditor.kt) | A url sent for the crawler to read |

### Screens

| Model | Role |
|---|---|
| [`EventScout`](../../web/src/jsMain/kotlin/streetlight/web/model/EventScout.kt) | Posts an event in stages |
| [`LocationScout`](../../web/src/jsMain/kotlin/streetlight/web/model/LocationScout.kt) | Finds or creates a location and posts it |
| [`FeedbackHub`](../../web/src/jsMain/kotlin/streetlight/web/model/FeedbackHub.kt) | Lists and sends feedback |
| [`BugReporter`](../../web/src/jsMain/kotlin/streetlight/web/model/BugReporter.kt) | Sends a bug report |
| [`Inbox`](../../web/src/jsMain/kotlin/streetlight/web/model/Inbox.kt) | A star's chats and messages |
| [`ChatRoom`](../../web/src/jsMain/kotlin/streetlight/web/model/ChatRoom.kt) | A live chat over a socket |
| [`MusicianHub`](../../web/src/jsMain/kotlin/streetlight/web/model/MusicianHub.kt) | Lists and adds songs |
| [`SongProfile`](../../web/src/jsMain/kotlin/streetlight/web/model/SongProfile.kt) | Shows and edits a song |
| [`SiteMonitor`](../../web/src/jsMain/kotlin/streetlight/web/model/SiteMonitor.kt) | Charts the site's status |
| [`Sandbox`](../../web/src/jsMain/kotlin/streetlight/web/model/Sandbox.kt) | The state of the sandbox |

### Map

| Model | Role |
|---|---|
| [`Earth`](../../web/src/jsMain/kotlin/streetlight/web/model/Earth.kt) | The model of the map screens |
| [`EarthCache`](../../web/src/jsMain/kotlin/streetlight/web/model/EarthCache.kt) | The events of each settled map view |
| [`MarkerMap`](../../web/src/jsMain/kotlin/streetlight/web/model/MarkerMap.kt) | The entity markers on the map and its focus |
| [`MarkerLayerConfig`](../../web/src/jsMain/kotlin/streetlight/web/model/MarkerLayerConfig.kt) | The map layers the site draws on |
| [`StreetMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/StreetMarker.kt) | The markers of entities |
| [`InflateMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/InflateMarker.kt) | The markers of event groups |
| [`StarMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/StarMarker.kt) | The viewer's own location marker |
| [`PrototypeMarker`](../../web/src/jsMain/kotlin/streetlight/web/model/PrototypeMarker.kt) | The marker id of a spirit |
| [`MapTypeIcon`](../../web/src/jsMain/kotlin/streetlight/web/model/MapTypeIcon.kt) | The icon of a location marker by its map type |
| [`StarLocator`](../../web/src/jsMain/kotlin/streetlight/web/model/StarLocator.kt) | The viewer's location |
| [`SpiritMap`](../../web/src/jsMain/kotlin/streetlight/web/model/SpiritMap.kt) | The map's position shared with other viewers |
| [`TransitMap`](../../web/src/jsMain/kotlin/streetlight/web/model/TransitMap.kt) | Transit routes and vehicle positions |

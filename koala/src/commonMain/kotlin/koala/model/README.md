# Package koala.model

## Introduction

Classes and interfaces not bound to their point of use: routes, menus, documents, charts, and the shared map and its markers.

## Dependencies

| Package | Provides |
|---|---|
| `kampfire.model` | `Tap`, `Store` and `Messenger` |
| `koala.modifier` | Modifiers that menu entries and markers carry |
| `kotlinx.coroutines` | Flows and scopes |
| `web` (Kotlin wrappers) | Browser APIs, in `jsMain` |

## Route Labels

An `WebRoute` carries no label. A link that shows a route as text takes a `LabeledRoute`, which pairs the route with the label of that link and defaults it to the route's `title`. Two links to one route may label it differently while the route stays equal to the one parsed from the path.

## History

`Portal.go` replaces the current entry, in its backstack and in the browser's history, when the new route has the current route's `screenKey`, and adds one otherwise.

`Portal` intercepts a click on a same-host link only when the link parses to a route of its screens. Any other link is left to the browser, which loads it from the server.

Only `Portal` writes the browser's history. Code that sets a query parameter of the current route dispatches a `QUERY_PARAM_CHANGE` event on `window`, and `Portal` goes to the route parsed from the address with that parameter set. A parameter the route does not parse leaves it unchanged.

## Models

### Routes

| Model | Role |
|---|---|
| [`WebRoute`, `AppScreen`, `RouteParse`](WebRoute.kt) | A place in the app, the screen that renders it, and how its path is parsed |
| [`WebRouteUtility`](WebRouteUtility.kt) | Relative paths built from route parts |
| [`SubRoute`](SubRoute.kt) | A route among several one screen shows, beneath its parent |
| [`LabeledRoute`](LabeledRoute.kt) | A route with the label that links to it |
| [`RouteContent`](RouteContent.kt) | The content a `ContentFetcher` returns for a route |
| [`Portal`](../../../../jsMain/kotlin/koala/model/Portal.kt) | The current route, kept in step with the address bar |
| [`RouteInflator`, `ContentFetcher`](../../../../jsMain/kotlin/koala/model/RouteInflator.kt) | Fetches the content of each new route |

### Menus and Messages

| Model | Role |
|---|---|
| [`MenuOption`](MenuAction.kt) | An entry in a menu: a label, a route, an action or a value |
| [`IconButton`](IconButton.kt) | An icon that navigates or acts |
| [`MessageStore`](MessageStore.kt) | A `Messenger` holding one message for a `messageBox` |
| [`Toaster`, `Toast`](Toaster.kt) | A `Messenger` showing each message as a toast that fades, held and dismissed by the view |

### Data

| Model | Role |
|---|---|
| [`Doc`](Doc.kt) | A document of the site |
| [`DocTree`](DocTree.kt) | The docs of a site as nodes |
| [`KeyedField`](KeyedField.kt) | A value with a key that stays with it while its list changes |
| [`ChartSeries`](ChartSeries.kt) | A named series of a chart |
| [`LineChartAdapter`](../../../../jsMain/kotlin/koala/model/LineChartAdapter.kt) | Draws chart data as an ECharts line chart |
| [`LocalStorageDelegate`](../../../../jsMain/kotlin/koala/model/LocalStorageDelegate.kt) | A property backed by `localStorage` |
| [`ScriptLoader`](../../../../jsMain/kotlin/koala/model/ScriptLoader.kt) | Loads external scripts once each |

### Map

A `MarkerMap` is a submodel: a model holds one for the map its view shows, and the view passes it to its map mount, which has `GeoMap` show it while the mount is in sight. `GeoRender` clears the map when another `MarkerMap` is shown and draws that one's layers. Focus belongs to the `MarkerMap`, and a click on a marker sets it there without moving the camera.

| Model | Role |
|---|---|
| [`GeoMap`](GeoMap.kt) | The app's one map and the `MarkerMap` it shows |
| [`MarkerMap`](MarkerMap.kt) | The layers of markers a view shows and the one in focus |
| [`MarkerLayerConfig`](MarkerLayerConfig.kt) | The map layers the site draws on |
| [`GeoCamera`](GeoCamera.kt) | The camera over the map |
| [`GeoFocus`](GeoFocus.kt) | The focus of the map: a marker or a cluster |
| [`GeoLayer`](GeoLayer.kt) | A layer of markers |
| [`GeoMarker`](GeoMarker.kt) | Something drawn on the map |
| [`MarkerStyle`](MarkerStyle.kt) | The stylesheet of markers |
| [`configureThumbMarker`…](configureMarker.kt) | The markup of each kind of marker |
| [`GeoRender`](../../../../jsMain/kotlin/koala/model/GeoRender.kt) | Draws the layers of the shown `MarkerMap` and marks its focused marker |
| [`GeoCameraController`](../../../../jsMain/kotlin/koala/model/GeoCameraController.kt) | Connects the MapLibre map to its camera |
| [`GeoLayerAdapter`](../../../../jsMain/kotlin/koala/model/GeoLayerAdapter.kt) | Gives each point the zoom where it is revealed |
| [`LightLayer`](../../../../jsMain/kotlin/koala/model/LightLayer.kt) | Draws each marker's light beneath the markers |
| [`LineRender`](../../../../jsMain/kotlin/koala/model/LineRender.kt) | A line marker as a MapLibre source and layer |
| [`PointMarkerElement`](../../../../jsMain/kotlin/koala/model/PointMarkerElement.kt) | The element of a point marker |
| [`MapLibreUtility`](../../../../jsMain/kotlin/koala/model/MapLibreUtility.kt) | Conversions between map points and MapLibre types |
| [`CurrentLocation`](../../../../jsMain/kotlin/koala/model/CurrentLocation.kt) | The device's location as a stream of fixes |
| [`CompassHeading`](../../../../jsMain/kotlin/koala/model/CompassHeading.kt) | The device's heading as a stream |

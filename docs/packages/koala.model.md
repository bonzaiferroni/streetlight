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

An `AppRoute` carries no label. A link that shows a route as text takes a `LabeledRoute`, which pairs the route with the label of that link and defaults it to the route's `title`. Two links to one route may label it differently while the route stays equal to the one parsed from the path.

## Models

### Routes

| Model | Role |
|---|---|
| [`AppRoute`, `AppScreen`, `RouteParse`](../../koala/src/commonMain/kotlin/koala/model/AppRoute.kt) | A place in the app, the screen that renders it, and how its path is parsed |
| [`AppRouteUtility`](../../koala/src/commonMain/kotlin/koala/model/AppRouteUtility.kt) | Relative paths built from route parts |
| [`SubRoute`](../../koala/src/commonMain/kotlin/koala/model/SubRoute.kt) | A route among several one screen shows, beneath its parent |
| [`LabeledRoute`](../../koala/src/commonMain/kotlin/koala/model/LabeledRoute.kt) | A route with the label that links to it |
| [`RouteContent`](../../koala/src/commonMain/kotlin/koala/model/RouteContent.kt) | The content a `ContentFetcher` returns for a route |
| [`Portal`](../../koala/src/jsMain/kotlin/koala/model/Portal.kt) | The current route, kept in step with the address bar |
| [`RouteInflator`, `ContentFetcher`](../../koala/src/jsMain/kotlin/koala/model/RouteInflator.kt) | Fetches the content of each new route |

### Menus and Messages

| Model | Role |
|---|---|
| [`MenuOption`](../../koala/src/commonMain/kotlin/koala/model/MenuAction.kt) | An entry in a menu: a label, a route, an action or a value |
| [`IconButton`](../../koala/src/commonMain/kotlin/koala/model/IconButton.kt) | An icon that navigates or acts |
| [`MessageStore`](../../koala/src/commonMain/kotlin/koala/model/MessageStore.kt) | A `Messenger` holding one message for a `messageBox` |

### Data

| Model | Role |
|---|---|
| [`Doc`](../../koala/src/commonMain/kotlin/koala/model/Doc.kt) | A document of the site |
| [`DocTree`](../../koala/src/commonMain/kotlin/koala/model/DocTree.kt) | The docs of a site as nodes |
| [`KeyedField`](../../koala/src/commonMain/kotlin/koala/model/KeyedField.kt) | A value with a key that stays with it while its list changes |
| [`ChartSeries`](../../koala/src/commonMain/kotlin/koala/model/ChartSeries.kt) | A named series of a chart |
| [`LineChartAdapter`](../../koala/src/jsMain/kotlin/koala/model/LineChartAdapter.kt) | Draws chart data as an ECharts line chart |
| [`LocalStorageDelegate`](../../koala/src/jsMain/kotlin/koala/model/LocalStorageDelegate.kt) | A property backed by `localStorage` |
| [`ScriptLoader`](../../koala/src/jsMain/kotlin/koala/model/ScriptLoader.kt) | Loads external scripts once each |

### Map

| Model | Role |
|---|---|
| [`GeoMap`](../../koala/src/commonMain/kotlin/koala/model/GeoMap.kt) | The map's layers and focused marker, shared by every view that shows the map |
| [`GeoCamera`](../../koala/src/commonMain/kotlin/koala/model/GeoCamera.kt) | The camera over the map |
| [`GeoFocus`](../../koala/src/commonMain/kotlin/koala/model/GeoFocus.kt) | The focus of the map: a marker or a cluster |
| [`GeoLayer`](../../koala/src/commonMain/kotlin/koala/model/GeoLayer.kt) | A layer of markers |
| [`GeoMarker`](../../koala/src/commonMain/kotlin/koala/model/GeoMarker.kt) | Something drawn on the map |
| [`MarkerStyle`](../../koala/src/commonMain/kotlin/koala/model/MarkerStyle.kt) | The stylesheet of markers |
| [`configureThumbMarker`…](../../koala/src/commonMain/kotlin/koala/model/configureMarker.kt) | The markup of each kind of marker |
| [`GeoRender`](../../koala/src/jsMain/kotlin/koala/model/GeoRender.kt) | Draws the map's layers and marks the focused marker |
| [`GeoCameraController`](../../koala/src/jsMain/kotlin/koala/model/GeoCameraController.kt) | Connects the MapLibre map to its camera |
| [`GeoLayerAdapter`](../../koala/src/jsMain/kotlin/koala/model/GeoLayerAdapter.kt) | Gives each point the zoom where it is revealed |
| [`LightLayer`](../../koala/src/jsMain/kotlin/koala/model/LightLayer.kt) | Draws each marker's light beneath the markers |
| [`LineRender`](../../koala/src/jsMain/kotlin/koala/model/LineRender.kt) | A line marker as a MapLibre source and layer |
| [`PointMarkerElement`](../../koala/src/jsMain/kotlin/koala/model/PointMarkerElement.kt) | The element of a point marker |
| [`MapLibreUtility`](../../koala/src/jsMain/kotlin/koala/model/MapLibreUtility.kt) | Conversions between map points and MapLibre types |
| [`CurrentLocation`](../../koala/src/jsMain/kotlin/koala/model/CurrentLocation.kt) | The device's location as a stream of fixes |
| [`CompassHeading`](../../koala/src/jsMain/kotlin/koala/model/CompassHeading.kt) | The device's heading as a stream |

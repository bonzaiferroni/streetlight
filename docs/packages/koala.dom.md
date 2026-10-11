# Package koala.dom

## Introduction

The view runtime: views, the scopes view functions are built in, route blocks, and the utilities that reach and change live elements. The dom-aware UI built on it lives in `koala.dom.component`, `koala.dom.layout`, `koala.dom.page` and `koala.dom.markdown`.

## Dependencies

| Package | Provides |
|---|---|
| `kotlinx.html` | The HTML DSL the builders extend |
| `koala.modifier` | Modifiers applied to elements |
| `koala.page` | `Id` and `Queryable` |
| `koala.model` | `Portal`, `RouteInflator` and the route types |
| `kampfire.model` | `Tap`, `Store` and `Messenger` |
| `web` (Kotlin wrappers) | DOM types |
| `kotlinx.coroutines` | View lifecycles |

## Route Blocks

`routeBlock` builds a view from the content of its route. It reads the island the shell carried when the route is the initial one, and otherwise takes the content from `RouteInflator`.

`RouteInflator` fetches the content of each new route through a `ContentFetcher`, which maps a route to its endpoint and is implemented by the application. A route on a screen with `hasShell` skips the fetch when it is the initial route. The island is read whenever the route is the initial one, whether or not `hasShell` is set, so a shell screen without the flag works and fetches content it does not use.

## Runtime

| Part | Role |
|---|---|
| [`View`](../../koala/src/jsMain/kotlin/koala/dom/View.kt) | A unit of UI: its elements, the coroutines behind them, and its child views |
| [`ViewScope`, `launchEffect`](../../koala/src/jsMain/kotlin/koala/dom/ViewScope.kt) | The receiver of every view function, and the effects launched in it |
| [`AppendScope`](../../koala/src/jsMain/kotlin/koala/dom/AppendScope.kt) | A builder that appends elements to a live parent |
| [`EffectScope`](../../koala/src/jsMain/kotlin/koala/dom/EffectScope.kt) | The receiver of a view effect |
| [`mountRootView`, `mountChildView`](../../koala/src/jsMain/kotlin/koala/dom/viewFactories.kt) | Builds a root or child view into an element |
| [`RouteScope`, `routeBlock`](../../koala/src/jsMain/kotlin/koala/dom/RouteScope.kt) | The receiver of a route's view, and the block that builds it from the route's content |
| [`AppContainer`](../../koala/src/jsMain/kotlin/koala/dom/AppContainer.kt) | The app's service container, which a view reaches with `get` |

## Elements

| Part | Role |
|---|---|
| [`onEvent`, `onClick`](../../koala/src/jsMain/kotlin/koala/dom/ElementEvent.kt) | Event handlers on an element |
| [`flowModifier`, `flowIsDisplayed`…](../../koala/src/jsMain/kotlin/koala/dom/FlowModifier.kt) | A modifier, display or validity applied while a flow says so |
| [`ElementUtility`](../../koala/src/jsMain/kotlin/koala/dom/ElementUtility.kt) | Queries by `Queryable`, and the nearest matching ancestor |
| [`DocumentUtility`](../../koala/src/jsMain/kotlin/koala/dom/DocumentUtility.kt) | Elements by id, view transitions and open popovers |
| [`W3CUtility`](../../koala/src/jsMain/kotlin/koala/dom/W3CUtility.kt) | Casts between the element types of kotlinx.html and the Kotlin wrappers |

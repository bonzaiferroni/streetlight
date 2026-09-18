# streetlight.web.model

The view models. One class per screen or per coherent piece of a screen; views in `streetlight.web.ui` read from these and never hold state of their own.

## Model Structure

`Foo` and its `FooState` live in the same file, along with any enums only they use.

```kotlin
class Foo(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(FooState())
    val stateNow get() = state.now
    val stateFlow = state.flow
}

data class FooState(
    val items: List<Item> = emptyList(),
)
```

Every property of `FooState` has a default, so `FooState()` is the opening position.

## Store and Taps

`storeOf` creates a `Store`, the root of all state. A `Store` is a `MutableTap`.

A `Tap` is a lens over one property of its source, which is a store or another tap. Taps are how a view binds to one field without knowing the whole state.

| Factory | Use |
|---|---|
| `state.mutableTapOf({ it.x }) { copy(x = it) }` | The view reads and writes the property |
| `state.tapOf { it.x }` | The view only reads |
| `stateFlow.dedup { it.x }` | Only a flow is needed and the initial value does not matter |

`dedup` is also where a state is transformed into something a component consumes, so the mapping runs once per change rather than once per collector.

## Reacting to State

Side effects that follow a state change are wired in `init` with `reactIn(scope)`:

```kotlin
init {
    timeFrameState.reactIn(scope) { refreshData() }
}
```

## Server Requests

`scope.launch(::functionName)` — the function reference names the failure in the log. Results unwrap with `toDataOr`, handing the failure to a `Toaster` or a `MessageStore` and returning from the launch:

```kotlin
val feed = api.feedSiteStatusFeed(resolution).toDataOr(toaster) { return@launch }
state.set { copy(points = feed.points) }
```

A long-lived poll keeps its `Job` on the model so the next trigger can cancel it.

`MessageStore` carries a message bound to one control — a search field, a submit button — where a `Toaster` would be too broad.

## Adapters

Most views are declarative and rebuild from a tap. A few wrap an imperative library — echarts, maplibre — that owns its own DOM and must be told what changed. Those get a class holding the widget, exposing methods a flow can be collected into. The suffix is `Adapter`.

| Rule | Reason |
|---|---|
| The adapter takes projected data | Mapping a domain type to the adapter's coordinates belongs in the view model, as in `pointSeriesOf` and `SiteMonitor.chartFlow` |
| The adapter is generic-free | The type parameter lives on the factory function that does the projecting, so the adapter does not gain a parameter per kind of data it accepts |
| Multiple input kinds are a sealed interface | `ChartSeries` resolves to `PointSeries` or `MarkSeries`, and the adapter branches on it |

An adapter that takes incremental updates aligns them against the data it already holds. Where the sealed shapes are mixed, only the relevant ones participate: `LineChartAdapter.addPoint` counts the `PointSeries` before matching a slice against them.

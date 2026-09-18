# streetlight.web.model

The view models. One class per screen or per coherent piece of a screen; views in
`streetlight.web.ui` read from these and never hold state of their own.

## Shape of a model

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

## Store and taps

`storeOf` creates a `Store`, the grandaddy of all our state. A `Store` is a `MutableTap`.

A `Tap` is a lens: it reads and writes one property of its source, which is a store or
another tap. Taps are how a view binds to one field without knowing the whole state.

```kotlin
val timeFrameState = state.mutableTapOf({ it.timeFrame }) { copy(timeFrame = it) }
val locationsState = state.tapOf { it.locations }
```

The getter projects out of the state; the setter is a `copy` back into it. Use `tapOf`
when the view only reads.

When only a flow is wanted and the initial value doesn't matter, `dedup` derives one
straight off `stateFlow` — no tap in between:

```kotlin
val pointsFlow = stateFlow.dedup { it.points }
```

`dedup` is also where a state is transformed into something a component consumes, so the
mapping runs once per change rather than once per collector.

## Reacting

Side effects that follow a state change are wired in `init` with `reactIn(scope)`:

```kotlin
init {
    timeFrameState.reactIn(scope) { refreshData() }
}
```

## Talking to the server

`scope.launch(::functionName)` — the function reference gives the failure a name in the
log. Results unwrap with `toDataOr`, handing the failure to a `Toaster` or a
`MessageStore` and returning from the launch:

```kotlin
val feed = api.feedSiteStatusFeed(resolution).toDataOr(toaster) { return@launch }
state.set { copy(points = feed.points) }
```

A long-lived poll keeps its `Job` on the model so the next trigger can cancel it.

`MessageStore` carries a message bound to one control — a search field, a submit button —
where a `Toaster` would be too loud.

## Adapters

Most views are declarative and rebuild from a tap. A few wrap something imperative —
echarts, maplibre — that owns its own DOM and must be *told* what changed. Those get a
plain class holding the widget, exposing methods a flow can be collected into.

The suffix is `Adapter`: it stands between a data source and a view that can't rebuild
itself, which is the role exactly. `Controller` says only that something is in charge.

> `GeoCameraController` predates this and is still to be renamed.

An adapter takes already-projected data. The mapping from a domain type to coordinates
belongs in the view model, not in the adapter — see `seriesOf` and `SiteMonitor.dataFlow`
for the pattern: the generic parameter lives on the factory function, and the adapter
itself is generic-free.

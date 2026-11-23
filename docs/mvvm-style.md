# MVVM Style

At the core of every Streetlight user interface is an interaction model. It reads data from various sources and binds user actions and events, at every moment holding a snapshot of UI state. It handles interactions and publishes the state, and after that its job is done.

## Store

For a very simple UI, the interaction model may just be a store.

```kotlin
fun ViewScope.helloView() {
    val nameState = storeOf("Someone")
    textField(nameState)
    flowBlock(nameState) { name ->
        textBlock("Hello $name!")
    }
}
```

* [`storeOf`](../kampfire/src/commonMain/kotlin/kampfire/model/Store.kt) is a factory that creates a `Store<T>`, a humble wrapper for `StateFlow<T>` that implements `MutableTap<T>`. It is the grandaddy of all our UI state.
* [`textField`](../koala/src/jsMain/kotlin/koala/dom/component/textField.kt) is a wrapper for an `<input>` element. It takes a `MutableTap<String>` and operates as a two-way binder for its value.
* [`flowBlock`](../koala/src/jsMain/kotlin/koala/dom/layout/flowBlock.kt) is our heavy lifter for rendering UI state. It is a wrapper for `<div>` that renders its content with the state value.

## ViewModel

For any section of UI complex enough to justify it, the interaction model is a class.

```kotlin
// fooView.kt
context(model: Foo)
fun ViewScope.fooView() {
    column {
        row {
            textField(model.nameState)
            button("add", model::addItem).flowisBusy(model.isBusyState, contentScope)
        }
        flowBlock(model.itemsState) { items ->
            items.forEach { fooItem(it) }
        }
    }
}

// Foo.kt
class Foo(
    scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val scope = ModelScope(scope, this::class)
    private val state = storeOf(FooState())

    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
    val itemsState = state.tapOf { it.items }
    val isBusyState = state.mutableTapOf({ it.isBusy }) { copy(isBusy = it) }

    init {
        scope.launch {
            val items = api.foo.readItems().toDataOr(toaster) { return@launch }
            state.set { copy(items = items) }
        }
    }

    fun addItem() {
        val name = state.now.name.takeIf { it.isNotBlank() } ?: run {
            toaster.deliver(Problem("A name is required."))
            return
        }
        scope.launch(isBusyState, toaster) {
            val item = api.foo.addItem(state.name.now).toDataOr(toaster) { return@launch }
            state.set { copy(items = items + item) }
        }
    }
}

data class FooState(
    val name: String = "",
    val items: List<FooItem> = emptyList(),
    val isBusy: Boolean = false,
)
```

The above example demonstrates the basic anatomy of our ViewModel. The state is declared first as a store, followed by the state properties. A `Tap` operates as a lens over a single property or aspect of UI state. We use `mutableTapOf` to enable a two-way binding of distinct values. Public functions are invoked directly by the UI.

Our finest ViewModels do not try to be clever, they take a regular shape whenever possible.

## UI Messages

The UI wants to communicate about important state and events. A button press that invokes a background process can change its presentation and become temporarily disabled:

```kotlin
button("add", model::addItem).flowisBusy(model.isBusyState)
```

For richer communication involving text content, we use a Messenger. The globally available `Toaster` can be used in a pinch, which presents messages temporarily in a single location. A `MessageStore` and `messageBox` is preferable when there is a specific place in the UI that is ideal for a message, like next to a submit button. The `formSubmit` utility bundles the messageBox and button.

```kotlin
val messenger = MessageStore()
formSubmit("Send", { model.sendFeedback(messenger) }, messenger)
```

Although everyone is doing their best, sometimes problems arise and the user needs to know. The comments below explain more.

```kotlin
fun addItem() {
    val name = state.now.name.takeIf { it.isNotBlank() } ?: run {
        // invalid input is caught and communicated
        toaster.deliver(Problem("A name is required."))
        return
    }
    // isBusyState is true during launch and exceptions raise a message
    launch(isBusyState, toaster) {
        // http errors or problems delivered by the server raise a message
        val item = api.foo.addItem(state.name.now).toDataOr(toaster) { return@launch }
        state.set { copy(items = items + item) }
    }
}
```

## Different front ends, same approach

Streetlight targets both the browser and Compose Multiplatform, and their ViewModels are strikingly similar. One might even wish to use the same model, but each platform wants the freedom to be different to make the most of its features. However, a model built for the web is a great starting point for writing its cousin in the compose module, and vise versa.

Likewise, views also often take direct inspiration across platforms and use familiar language. 

## Patterns

### Submodel

Role: Holds the state and functions for a section of UI that is reusable across different viewmodels.

```kotlin
class FooModel(
    private val scope: CoroutineScope,
    api: ApiClient,
) {
    val barEditor = BarEditor(scope, api, BarType.Foo)
}
```

### Messages

Role: Reports the outcome of a model's work to the user.

```kotlin
fun submit(messenger: Messenger) {
    scope.launch {
        messenger.deliverSending()
        api.foo.submit(state.now.edit).toDataOr(messenger, "Saved") { return@launch }
    }
}
```

A method that reports to the user takes a `Messenger` parameter. The view decides where the message appears, and the model decides what it says. Without an obvious place in the view for the message, the model reports through its injected `Toaster` instead. `streetlight.web.view` states the conventions the messages follow.

## Antipatterns

### Avoid publishing upstream state

```kotlin
private val layoutState = mix.playbackSecondsPerTickState.combine(studio.isLyricsShownState) { perTick, isLyricsShown ->
    perTick to isLyricsShown
}
val lanesState = editor.sheetState.combine(layoutState) { sheet, (perTick, isLyricsShown) ->
    lanesOf(sheet, perTick, isLyricsShown)
}
```

Recommendations:
- Pass a service or state holder as a context parameter to the same UI function and consume directly

### Avoid overreliance on other ViewModels as dependencies

Indicators:
- Several ViewModels passed as arguments, each with a minor role

```kotlin
class LaneEditor(
    private val scope: CoroutineScope,
    private val studio: MusicStudio,
    private val editor: MusicEditor,
    private val mix: MixEditor,
    private val player: TrackPlayer,
    private val tones: TonePlayer,
)
```

Considerations:
- Is a shared service or state holder wanted?
- Does the UI want to be simpler?

### Avoid handling presentation concerns within a ViewModel

```kotlin
fun clipLabelOf(trackId: TrackId, notationId: NotationId?): Tap<String?> = state.tapOf { editor ->
    val clip = editor.clipOf(trackId, notationId) ?: return@tapOf null
    val track = editor.sheet.trackOf(trackId) ?: return@tapOf null
    if (clip.fits(editor.sheet.music, track, notationId)) clip.name else "${clip.name} (stale)"
}
```

Recommendations:
- Hold the fact as state, as `isClipStale`, and let the view decide how it shows, such as a red label

### Avoid complicated state resolution

Indicators:
- Reliance on an elaborate factory to resolve state.

```kotlin
private fun stateOf(track: Track): TrackEditorState {
    fun namedOf(notationId: NotationId) = namedNotationOf(track, notationId, music)
    return TrackEditorState(
        track = track,
        notations = track.notations.map { namedOf(it.notationId) },
        arrangements = TrackArrangements(
            arrangements = track.arrangements.map { arrangement ->
                NamedArrangement(namedOf(arrangement.notationId), arrangement.notations.map { namedOf(it) })
            },
            choices = (track.notations.map { it.notationId } + track.arrangements.map { it.notationId }).map { namedOf(it) },
        ),
        warnings = sectionWarningsOf(track, music),
        sectionCells = cellsOf(music),
        cells = cellsOf(music, track),
    )
}
```

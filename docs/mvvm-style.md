# MVVM Style

## Patterns

### ViewModel

Role: Holds state and defines functions for a section of UI.

```kotlin
class FooModel(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(FooState())
    
    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
    val itemsState = state.tapOf { it.items }
    
    init {
        scope.launch {
            val items = api.foo.readItems().toDataOr(toaster) { return@launch }
            state.set { copy(items = items) }
        }
    }
    
    fun addItem(item: FooItem) {
        scope.launch {
            val item = api.foo.addItem(item).toDataOr(toaster) { return@launch }
            state.set { copy(items = items + item) }
        }
    }
}
```

### ViewModel State

Role: Represents the state for a section of UI.

```kotlin
data class FooState(
    val name: String = "",
    val items: List<FooItem> = emptyList()
)
```

### View

Role: Renders the state of its viewmodel and calls its functions.

```kotlin
context(model: FooModel)
fun ViewScope.fooView() {
    textField(model.nameState)
    flowBlock(model.itemsState) { items ->
        items.forEach { fooItem(it) }
    }
}
```

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

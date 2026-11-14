## Antipatterns

### Avoid publishing upstream state

```kt
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

```kt
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
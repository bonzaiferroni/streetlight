package streetlight.web.view

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.markdown.textEditor
import koala.model.HeadingLevel
import koala.modifier.*
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.text.NotationError
import streetlight.web.dom.layout.actionCard
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicEditorFocus
import streetlight.web.model.MusicHost
import streetlight.web.model.NotationEditor
import streetlight.web.model.NotationFocus
import streetlight.web.model.NotationView

/**
 * Renders the notation of [focus] alone in a card under its name and its track's icon, with an up arrow that hands
 * [setFocus] its track as it now stands: its text and its notes.
 */
context(config: MusicConfig, host: MusicHost)
fun ViewScope.notationEditor(focus: NotationFocus, setFocus: (MusicEditorFocus?) -> Unit) {
    val track = host.readTrack(focus.notationId)
    val model = NotationEditor(contentScope, config.notationStyleState, focus.notationId, host)
    actionCard(
        label = model.nameState.now,
        level = HeadingLevel.H3,
        mod = modify(track.trackType.color.bg, OutlineLow),
        svg = track.trackType.svg.large,
        key = focus.notationId.toString(),
        onBack = { setFocus(null) },
    ) {
        card(modify(PaperBg, Outline, BorderRadius1)) {
            row(modify(AlignItemsCenter)) {
                switch("study", model.isActiveStudy)
            }
            notationPanes(track.notationLabel, model.notationTextEditor.textState, model.errorsState, config.notationViewState) {
                flowBlock(model.musicSourceState) { source ->
                    source?.tracks?.firstOrNull()?.let { notationStrips(source, it) }
                }
            }
        }
    }
}

/**
 * Renders the text in [textState], then [output], what the text reads as, then its [errorsState], each part as
 * [viewState] shows it. The errors go with the text. Showing both, a wide pane puts the notes beside the text.
 */
private fun ViewScope.notationPanes(
    label: String,
    textState: MutableTap<String>,
    errorsState: Tap<List<NotationError>>,
    viewState: Tap<NotationView> = storeOf(NotationView.Both),
    output: ViewScope.() -> Unit,
) {
    flowBlock(viewState, StudioStyle.Panes) { view ->
        // a lone part keeps the whole width, so only both parts split
        val split = StudioStyle.SplitPanes.takeIf { view == NotationView.Both }
        div(modify(StudioStyle.PanesGrid, split)) {
            if (view != NotationView.NotesOnly) {
                textEditor(textState, label, modify(MonoFont, StudioStyle.TextPane), spellcheck = false)
            }
            if (view != NotationView.TextOnly) div(StudioStyle.NotesPane) { output() }
            if (view != NotationView.NotesOnly) {
                flowBlock(errorsState, StudioStyle.ErrorsPane) { errors ->
                    errors.forEach { textBlock("Line ${it.line}: ${it.message}", modify(TextSmall, InkDimFg)) }
                }
            }
        }
    }
}

/** The name of the text the track's notations are written in. */
private val Track.notationLabel get() = when (this) {
    is ChordTrack -> "chords"
    is MelodyTrack -> "jianpu"
    is LyricTrack -> "lyrics"
    is DrumTrack -> "drums"
}

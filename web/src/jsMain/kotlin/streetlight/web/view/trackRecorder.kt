package streetlight.web.view

import kampfire.model.tapOf
import koala.dom.ViewScope
import koala.dom.flowModifier
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.Flex
import streetlight.model.music.Clip
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import streetlight.web.dom.layout.actionMenu
import streetlight.web.model.ClipEditor
import streetlight.web.model.ClipMic
import streetlight.web.model.ClipMicPhase
import streetlight.web.model.MusicEditor
import streetlight.model.studio.StudioDb
import streetlight.model.music.nameOf
import streetlight.web.model.MusicHost
import streetlight.web.model.getClipMic
import kotlin.uuid.Uuid

/**
 * The actions that open the recorder of clips of the notation [notationId] of the track [trackId], or of the whole
 * track when `null`, and the clips that fit it.
 */
context(host: MusicHost, db: StudioDb)
fun ViewScope.clipActions(trackId: TrackId, notationId: NotationId?): List<MenuAction> {
    val recordId = Id(Uuid.random().toString())
    val clipsId = Id(Uuid.random().toString())
    val clipEditor = ClipEditor(contentScope, host.sheetState, db, trackId, notationId)
    recordPopover(recordId, trackId, notationId, clipEditor)
    clipsPopover(clipsId, clipEditor)
    return listOf(
        MenuAction("Record", config = { setPopoverTarget(recordId, true) }) { },
        MenuAction("Clip", config = { setPopoverTarget(clipsId, true) }) { },
    )
}

/**
 * A popover with [id] holding the recorder of clips of the notation [notationId] of the track [trackId], or of the
 * whole track when `null`, under the names of the track and notation and the clip [clipEditor] holds, red when it
 * no longer fits.
 */
context(host: MusicHost)
private fun ViewScope.recordPopover(id: Id, trackId: TrackId, notationId: NotationId?, clipEditor: ClipEditor) {
    popover(id) {
        val mic = app.getClipMic(contentScope, host.sheetState, trackId, notationId)
        column(Padding(2)) {
            flowBlock(host.trackStateOf(trackId)) { track ->
                if (track == null) return@flowBlock
                val notationName = notationId?.let { track.nameOf(it) }
                textBlock(listOfNotNull(track.name, notationName).joinToString(" · "))
            }
            flowBlock(clipEditor.clipState) { clip ->
                textBlock(clip?.name ?: "Tones play this.", modify(TextSmall, InkDimFg))
                    .flowModifier(clipEditor.isClipStaleState, ErrorFg, contentScope)
            }
            clipRecorder(mic)
        }
    }
}

/** A popover with [id] holding the clips [clipEditor] may bind, where a click binds one, or none. */
private fun ViewScope.clipsPopover(id: Id, clipEditor: ClipEditor) {
    popover(id, clipEditor.isPopoverOpenState) { popover ->
        fun bindClip(clip: Clip?) {
            popover.close()
            clipEditor.setClip(clip)
        }
        flowBlock(clipEditor.clipsState) { choices ->
            actionMenu(listOf(MenuAction(NO_CLIP) { bindClip(null) }) + choices.map { clip ->
                MenuAction(clip.name) { bindClip(clip) }
            })
        }
    }
}

/**
 * Records clips through [mic]: the lead-in's progress beside the recording's, then the name the clip is kept under and
 * the button that starts it, or stops it early and discards it.
 */
private fun ViewScope.clipRecorder(mic: ClipMic) {
    column(modify(PaddingY(1))) {
        row(AlignItemsCenter) {
            progressBar(mic.leadProgressState, modify(Flex1, Height(2), ZenBg), isSmooth = false)
            progressBar(mic.clipProgressState, modify(Css.Flex.of(Flex(4)), Height(2), ZenBg), isSmooth = false)
        }
        row(AlignItemsCenter) {
            textField(mic.nameState, "name", Flex1)
            flowBlock(mic.phaseState) { phase ->
                val label = when (phase) {
                    ClipMicPhase.Idle -> "Record"
                    ClipMicPhase.LeadIn, ClipMicPhase.Recording -> "Stop"
                    ClipMicPhase.Saving -> "Saving"
                }
                button(label, { mic.toggle() })
            }
        }
        switch("Click", mic.isClickOnState)
        flowBlock(mic.isCalibratedState) { isCalibrated ->
            if (isCalibrated) return@flowBlock
            row(AlignItemsCenter) {
                textBlock(
                    "A calibration measures how late sound reaches the mic, so clips line up with the beat.",
                    modify(Flex1, TextSmall, InkDimFg),
                )
                flowBlock(mic.isCalibratingState) { isCalibrating ->
                    button(if (isCalibrating) "Calibrating" else "Calibrate", { mic.calibrate() })
                }
            }
        }
    }
}

/** The choice that binds no clip, so tones play. */
private const val NO_CLIP = "(none)"

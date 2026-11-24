package streetlight.web.view

import kampfire.model.Messenger
import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.dom.layout.itemsBlock
import koala.model.IconAction
import koala.modifier.*
import koala.page.Id
import streetlight.model.music.MusicStudy
import streetlight.model.music.Study
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.web.dom.layout.actionRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicPlayer
import streetlight.web.model.NotationFeedback
import streetlight.web.model.StudioAudio
import streetlight.web.model.StudySession
import streetlight.web.model.WaveShelf
import streetlight.web.model.portal
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

context(config: MusicConfig, player: MusicPlayer, waveShelf: WaveShelf)
fun ViewScope.studyFocus(study: MusicStudy) {
    val session = StudySession(contentScope, study, player, waveShelf, config, app.get<StudioAudio>(), app.get<Messenger>())
    // playback left running when the study closes would keep sounding
    onDispose { player.stop() }
    val settingsId = Id(Uuid.random().toString())
    popover(settingsId) {
        column(Padding(2)) { musicConfigMenu() }
    }
    topSection(
        label = study.label,
        onBack = { portal.go(MusicStudioIndexRoute) },
        headingMod = studyLabelTransition(study.study),
        icon = IconAction(SvgFile.Gear.large, "Settings", config = { setPopoverTarget(settingsId, true) }) { },
    ) {
        studyControls(session)
        flowBlock(session.layoutsState) { layouts ->
            layouts.forEach { notationStrips(it) }
        }
        studyFeedback(session)
        studyClips(session)
    }
}

context(config: MusicConfig)
private fun ViewScope.studyControls(session: StudySession) = row(modify(AlignSelfCenter, AlignItemsCenter)) {
    button(SvgFile.PlayerPlay.filled, { session.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { session.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    button(SvgFile.PlayerRecord.filled, { session.record() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Record")))
    switch("lyrics", config.isLyricsShownState)
    flowBlock(session.isCalibratedState) { isCalibrated ->
        if (!isCalibrated) flowBlock(session.isCalibratingState) { isCalibrating ->
            button(if (isCalibrating) "Calibrating" else "Calibrate", { session.calibrate() })
        }
    }
}

/** Renders a summary of the feedback on the latest take. */
private fun ViewScope.studyFeedback(session: StudySession) {
    flowBlock(session.clipIdsState) { clipIds ->
        val feedback = clipIds.lastOrNull()?.let { session.feedbacks[it] } ?: return@flowBlock
        textBlock(feedbackSummaryOf(feedback, session.secondsPerTick), modify(TextAlignCenter, TextSmall))
    }
}

/** How many notes of [feedback] were sung, how far their pitch strayed and where they sat against the beat. */
private fun feedbackSummaryOf(feedback: List<NotationFeedback>, secondsPerTick: Double): String {
    val notes = feedback.flatMap { it.measures }.flatMap { it.notes }
    val sung = notes.filterNotNull()
    if (sung.isEmpty()) return "No notes heard in the last take."
    val pitch = sung.map { abs(it.pitch) }.median()
    val offsets = sung.map { it.time * secondsPerTick * 1000 }
    val offset = offsets.median()
    val spread = offsets.map { abs(it - offset) }.median()
    val side = if (offset < 0) "ahead of" else "behind"
    return "Sung ${sung.size} of ${notes.size} notes · pitch off by ${pitch.roundToInt()} cents · " +
        "${abs(offset).roundToInt()} ms $side the beat, ± ${spread.roundToInt()} ms"
}

private fun List<Double>.median() = sorted()[size / 2]

private fun ViewScope.studyClips(session: StudySession) {
    actionSection(label = "Clips") {
        itemsBlock(session.clipIdsState, gapRems = null) { clipId ->
            val clip = session.clips[clipId] ?: return@itemsBlock
            actionRow(
                label = clip.name,
                icons = listOf(
                    IconAction(SvgFile.PlayerPlay.small, "Play") { session.playClip(clipId) },
                    IconAction(SvgFile.Trash.small, "Remove") { session.removeClip(clipId) },
                ),
            )
        }
    }
}

fun studyLabelTransition(study: Study) = transition("study-${study.trackId}-${study.notationId ?: "track"}")

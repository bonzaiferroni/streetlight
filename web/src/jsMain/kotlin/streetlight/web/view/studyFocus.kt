package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.modifier.*
import streetlight.model.music.MusicStudy
import streetlight.model.music.Study
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicPlayer
import streetlight.web.model.StudySession
import streetlight.web.model.WaveShelf
import streetlight.web.model.portal

context(config: MusicConfig, player: MusicPlayer, waveShelf: WaveShelf)
fun ViewScope.studyFocus(study: MusicStudy) {
    val session = StudySession(contentScope, study, player, waveShelf)
    // playback left running when the study closes would keep sounding
    onDispose { player.stop() }
    topSection(
        label = study.label,
        onBack = { portal.go(MusicStudioIndexRoute) },
        headingMod = studyLabelTransition(study.study),
    ) {
        studyControls(session)
        flowBlock(session.layoutsState) { layouts ->
            layouts.forEach { notationStrips(it) }
        }
    }
}

context(config: MusicConfig)
private fun ViewScope.studyControls(session: StudySession) = row(modify(AlignSelfCenter, AlignItemsCenter)) {
    button(SvgFile.PlayerPlay.filled, { session.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { session.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    switch("lyrics", config.isLyricsShownState)
}

fun studyLabelTransition(study: Study) = transition("study-${study.trackId}-${study.notationId ?: "track"}")

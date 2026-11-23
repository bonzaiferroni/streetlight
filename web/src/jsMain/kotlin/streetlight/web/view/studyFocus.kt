package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.IconAction
import koala.modifier.*
import koala.page.Id
import streetlight.model.music.MusicStudy
import streetlight.model.music.Study
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicPlayer
import streetlight.web.model.StudySession
import streetlight.web.model.WaveShelf
import streetlight.web.model.portal
import kotlin.uuid.Uuid

context(config: MusicConfig, player: MusicPlayer, waveShelf: WaveShelf)
fun ViewScope.studyFocus(study: MusicStudy) {
    val session = StudySession(contentScope, study, player, waveShelf, config)
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
    }
}

context(config: MusicConfig)
private fun ViewScope.studyControls(session: StudySession) = row(modify(AlignSelfCenter, AlignItemsCenter)) {
    button(SvgFile.PlayerPlay.filled, { session.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { session.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    switch("lyrics", config.isLyricsShownState)
}

fun studyLabelTransition(study: Study) = transition("study-${study.trackId}-${study.notationId ?: "track"}")

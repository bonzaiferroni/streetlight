package streetlight.web.view

import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.layout.lazyTransTabs
import koala.model.IconAction
import koala.modifier.setPopoverTarget
import koala.modifier.transition
import koala.page.Id
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.studio.StudioDb
import streetlight.model.ui.MusicEditorRoute
import streetlight.model.ui.MusicStudioIndexRoute
import streetlight.model.ui.MusicStudioTab
import streetlight.web.dom.layout.topSection
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicHost
import streetlight.web.model.MusicPlayer
import streetlight.web.model.WaveShelf
import streetlight.web.model.portal
import kotlin.uuid.Uuid


context(player: MusicPlayer, config: MusicConfig, db: StudioDb, waveShelf: WaveShelf)
fun ViewScope.musicFocus(initialSheet: MusicSheet) {
    val host = MusicHost(contentScope, initialSheet, db)
    // playback left running when the music closes would keep sounding
    onDispose { player.stop() }
    val settingsId = Id(Uuid.random().toString())
//    songSettingsPopover(settingsId, editor)
    context(host) {
        topSection(
            label = host.musicState.now.name,
            onBack = { portal.go(MusicStudioIndexRoute) },
            headingMod = musicNameTransition(host.musicState.now.musicId),
            icon = IconAction(SvgFile.Gear.large, "Settings", config = { setPopoverTarget(settingsId, true) }) { },
        ) {
            lazyTransTabs(paramKey = MusicEditorRoute.tabParam.key) {
                MusicStudioTab.entries.forEach { studioTab ->
                    tab(studioTab.label, studioTab.svg) {
                        when (studioTab) {
                            MusicStudioTab.Compose -> musicEditor()
                            MusicStudioTab.Timeline -> laneEditor()
                        }
                    }
                }
            }
        }
    }
}

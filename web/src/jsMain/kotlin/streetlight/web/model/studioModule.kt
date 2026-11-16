package streetlight.web.model

import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import kampfire.model.Tap
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import streetlight.model.music.MusicId
import streetlight.model.studio.StudioDb

/** The services of the Studio section, one of each, loaded into the app's Koin by [loadStudioModule]. */
val studioModule = module {
    single { StudioAudio(get()) }
    single { ToneSynth() }
    single { TrackPlayer(get(), get()) }
    single { TonePlayer(get(), get()) }
    single<StudioDb> { StudioIdbDb() }
    single { ClipLibrary(get(), get()) }
    single { WaveShelf(get(), get()) }
}

/** Loads [studioModule] into the app's Koin, unless it is loaded. */
fun AppContainer.loadStudioModule() {
    if (isStudioModuleLoaded) return
    koin.loadModules(listOf(studioModule))
    isStudioModuleLoaded = true
}

fun AppContainer.getStudioEditor(scope: CoroutineScope) = StudioEditor(scope, koin.get())

/** The capture of clips of the notation [notationId] of the track [trackId] in [sheetState], or the whole track when `null`. */
fun AppContainer.getClipMic(scope: CoroutineScope, sheetState: Tap<MusicSheet>, trackId: TrackId, notationId: NotationId?) =
    ClipMic(
        scope, sheetState, trackId, notationId,
        koin.get(), koin.get(), koin.get(), koin.get(), koin.get(),
    )

fun AppContainer.getMusicStudio(scope: CoroutineScope, initialId: MusicId?) =
    MusicStudio(scope, initialId, koin.get(), koin.get())

private var isStudioModuleLoaded = false

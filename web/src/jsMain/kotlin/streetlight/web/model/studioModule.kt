package streetlight.web.model

import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import streetlight.model.music.TrackSectionId
import streetlight.model.music.MusicId
import streetlight.model.studio.StudioDb

/** The services of the Studio section, one of each, loaded into the app's Koin by [loadStudioModule]. */
val studioModule = module {
    single { StudioAudio(get()) }
    single { ToneSynth() }
    single { TrackPlayer(get(), get()) }
    single { TonePlayer(get(), get()) }
    single<StudioDb> { StudioIdbDb() }
    single { ClipLibrary(get(), get<StudioDb>().clipDao()) }
    single { TakeShelf(get()) }
}

/** Loads [studioModule] into the app's Koin, unless it is loaded. */
fun AppContainer.loadStudioModule() {
    if (isStudioModuleLoaded) return
    koin.loadModules(listOf(studioModule))
    isStudioModuleLoaded = true
}

fun AppContainer.getStudioEditor(scope: CoroutineScope) = StudioEditor(scope, koin.get())

/** The capture of takes of the track's section [trackSectionId] in [editor]'s open mix. */
fun AppContainer.getSectionMic(scope: CoroutineScope, editor: MixEditor, trackSectionId: TrackSectionId) =
    SectionMic(
        scope, editor, trackSectionId,
        koin.get(), koin.get(), koin.get(), koin.get(),
    )

fun AppContainer.getMusicStudio(scope: CoroutineScope, initialId: MusicId?) =
    MusicStudio(scope, initialId, koin.get(), koin.get(), koin.get())

private var isStudioModuleLoaded = false

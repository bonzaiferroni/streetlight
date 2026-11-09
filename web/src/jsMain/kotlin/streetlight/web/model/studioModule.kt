package streetlight.web.model

import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import streetlight.model.io.MixDb
import streetlight.model.music.SectionId
import streetlight.model.music.TrackId

/** The services of the Studio section, one of each, loaded into the app's Koin by [loadStudioModule]. */
val studioModule = module {
    single { StudioAudio(get()) }
    single { ToneSynth() }
    single { TrackPlayer(get(), get()) }
    single { TonePlayer(get(), get()) }
    single { SongDb(get()) }
    single { localDbOf() }
    single { ClipLibrary(get(), get()) }
    single<MixDb> { LocalMixDb(get()) }
    single { TakeShelf(get()) }
}

/** Loads [studioModule] into the app's Koin, unless it is loaded. */
fun AppContainer.loadStudioModule() {
    if (isStudioModuleLoaded) return
    koin.loadModules(listOf(studioModule))
    isStudioModuleLoaded = true
}

fun AppContainer.getStudioEditor(scope: CoroutineScope) = StudioEditor(scope, koin.get())

/** The capture of takes of the section [sectionId] of the track [trackId] in [editor]'s open mix. */
fun AppContainer.getSectionMic(scope: CoroutineScope, editor: MixEditor, trackId: TrackId, sectionId: SectionId) =
    SectionMic(
        scope, editor, trackId, sectionId,
        koin.get(), koin.get(), koin.get(), koin.get(),
    )

/** The editor of the clip bound to the section [sectionId] of the track [trackId] in [editor]'s open mix. */
fun AppContainer.getSectionClipEditor(scope: CoroutineScope, editor: MixEditor, trackId: TrackId, sectionId: SectionId) =
    SectionClipEditor(scope, editor, trackId, sectionId, koin.get())

fun AppContainer.getMusicStudio(scope: CoroutineScope) = MusicStudio(scope, koin.get(), koin.get(), koin.get(), koin.get(), koin.get())

private var isStudioModuleLoaded = false

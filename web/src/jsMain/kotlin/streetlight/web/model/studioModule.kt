package streetlight.web.model

import koala.dom.AppContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import streetlight.model.io.MixDb

/** The services of the Studio section, one of each, loaded into the app's Koin by [loadStudioModule]. */
val studioModule = module {
    single { StudioAudio() }
    single { AudioRecorder(get()) }
    single { AudioPlayer(get()) }
    single { SongDb(get()) }
    single { TrackPlayer(get()) }
    single { ClipLibrary() }
    single { localDbOf() }
    single { ClipDb(get()) }
    single<MixDb> { LocalMixDb(get()) }
}

/** Loads [studioModule] into the app's Koin, unless it is loaded. */
fun AppContainer.loadStudioModule() {
    if (isStudioModuleLoaded) return
    koin.loadModules(listOf(studioModule))
    isStudioModuleLoaded = true
}

fun AppContainer.getStudioEditor(scope: CoroutineScope) = StudioEditor(scope, koin.get(), koin.get(), koin.get())

fun AppContainer.getMusicStudio(scope: CoroutineScope) = MusicStudio(scope, koin.get(), koin.get(), koin.get())

private var isStudioModuleLoaded = false

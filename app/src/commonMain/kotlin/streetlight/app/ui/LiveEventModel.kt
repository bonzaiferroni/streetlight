package streetlight.app.ui

import kabinet.model.GeminiVoice
import kabinet.model.OrpheusVoice
import kabinet.model.SpeechRequest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import streetlight.app.AppProvider
import streetlight.app.RuntimeProvider
import streetlight.model.data.EventId
import streetlight.model.data.EventSong
import streetlight.model.data.NewRendition
import streetlight.model.data.SelfRating
import kotlin.time.Duration.Companion.days

class LiveEventModel(
    private val eventId: EventId,
    private val app: AppProvider = RuntimeProvider,
) : StateModel<LiveEventState>() {
    override val state = ModelState(LiveEventState())
    private val client = app.repo.event

    private var introSpeech: ByteArray? = null
    private var interludeSpeech: ByteArray? = null
    private var outroSpeech: ByteArray? = null

    fun takeNextSong() {
        ioLaunch {
            val outroSpeechJob = outroSpeech?.let {
                launch {
                    app.wavePlayer.play(it)
                    outroSpeech = null
                }
            }

            stateNow.songPlay?.let {
                app.repo.songPlay.create(it)
            }

            val eventSong = app.repo.song.takeNextSong(eventId, Clock.System.now() - 30.days) ?: return@ioLaunch
            val newRendition = NewRendition(
                songId = eventSong.song.songId,
                notes = null,
                rating = null,
            )
            setStateFromMain { it.copy(song = eventSong, songPlay = newRendition) }

            val introRequestJob = launch {
                val introRequest = createIntroRequest(eventSong)
                introSpeech = app.speech.createWav(introRequest)
            }

//            val interludeRequestJob = launch {
//                val interludeRequest = createInterludeRequest()
//                interludeSpeech = app.gemini.generateSpeech(interludeRequest)
//            }

            launch {
                val outroRequest = createOutroRequest(eventSong)
                outroSpeech = app.speech.createWav(outroRequest)
            }

            outroSpeechJob?.join()
            // interludeRequestJob.join()
//            val interludeSpeechJob = interludeSpeech?.let {
//                launch { app.wavePlayer.play(it) }
//            }
//            interludeSpeechJob?.join()
            introRequestJob.join()

            introSpeech?.let {
                app.wavePlayer.play(it)
                introSpeech = null
            } ?: println("no speech found")
        }
    }

    fun setRating(rating: SelfRating?) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(rating = rating))}
    }

    fun setNotes(notes: String) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(notes = notes.takeIf { it.isNotBlank() }))}
    }

//    fun addSongPlay(rating: SelfRating, takeNext: Boolean) {
//        val songId = stateNow.song?.songId ?: return
//        val notes = stateNow.songPlayNotes.takeIf { it.isNotEmpty() }
//        ioLaunch {
//            app.client.songPlay.create(NewSongPlay(
//                songId = songId,
//                notes = notes,
//                rating = rating
//            ))
//            if (takeNext) takeNextSong()
//        }
//    }

    fun toggleBreak() {
        if (stateNow.breakStartedAt != null) {
            setState { it.copy(breakStartedAt = null)}
        } else {
            setState { it.copy(breakStartedAt = Clock.System.now()) }
        }
    }
}

data class LiveEventState(
    val song: EventSong? = null,
    val songPlay: NewRendition? = null,
    val breakStartedAt: Instant? = null,
) {
    val isActive get() = song != null
}

private fun createOutroRequest(eventSong: EventSong): SpeechRequest {
    val song = eventSong.song; val request = eventSong.request
    val text = buildString {
        append("That last one was '")
        append(song.title)
        append("' by ")
        append(song.artist)
        request?.requesterName?.let {
            append(", requested by ")
            append(it)
        }
        append(".")
    }

    return SpeechRequest(
        text = text,
        theme = announcerTheme,
        voice = announcerVoice,
        filename = "${song.title} outro ${announcerVoice}",
        isCached = true
    )
}

private fun createIntroRequest(eventSong: EventSong): SpeechRequest {
    val song = eventSong.song; val request = eventSong.request
    val text = buildString {
        append("This next song is called '")
        append(song.title)
        append("' and it's by ")
        append(song.artist)
        request?.requesterName?.let {
            append(", requested by ")
            append(it)
        }
        append(".")
    }

    return SpeechRequest(
        text = text,
        theme = announcerTheme,
        voice = announcerVoice,
        filename = "${song.title} intro ${announcerVoice}",
        isCached = true
    )
}

//private fun createInterludeRequest(): SpeechRequest {
//    val interlude = interludes.random()
//    return SpeechRequest(
//        text = interlude,
//        theme = announcerTheme,
//        voice = announcerVoice,
//        filename = "${announcerVoice.apiName} ${interlude.take(50)}",
//        isCached = true
//    )
//}

private val announcerVoice = OrpheusVoice.Emma.apiName
private val announcerTheme = "Say it like a radio DJ and be low key, do not be emotive or enthusiastic"

// voice ranks:
// Smooth: Algieba
// Upbeat: Puck
// Soft: Achernar
// Clear2: Erinome
// Gravelly: Algenib
// Zubenelgenubi
// Gacrux
// Sulafat
// Sadachbia
// Enceladus
// Charon
// Pulcherrima
// Alnilam
// Laomedeia
// Sadaltagager
// Autonoe
// Despina
// Vindemiatrix
// Zephyr
// Kore
// Iapetus
// Schedar
// Achird
// Rasalgethi
// Umbriel
// Aoede
// Callirrhoe
// Leda
// Fenrir
// Orus


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
                    setAnnouncerStatus("announcing outro")
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

            val interludeRequestJob = launch {
                val interludeRequest = createInterludeRequest()
                interludeSpeech = app.speech.createWav(interludeRequest)
            }

            launch {
                val outroRequest = createOutroRequest(eventSong)
                outroSpeech = app.speech.createWav(outroRequest)
            }

            outroSpeechJob?.join()
             interludeRequestJob.join()
            val interludeSpeechJob = interludeSpeech?.let {
                setAnnouncerStatus("interlude")
                launch { app.wavePlayer.play(it) }
            }
            interludeSpeechJob?.join()
            introRequestJob.join()

            introSpeech?.let {
                setAnnouncerStatus("announcing intro")
                app.wavePlayer.play(it)
                introSpeech = null
            } ?: println("no speech found")
            setAnnouncerStatus("Ready.")
        }
    }

    private suspend fun setAnnouncerStatus(status: String) = setStateFromMain { it.copy(announcerStatus = status) }

    fun setRating(rating: SelfRating?) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(rating = rating))}
    }

    fun setNotes(notes: String) {
        val songPlay = stateNow.songPlay ?: return
        setState { it.copy(songPlay = songPlay.copy(notes = notes.takeIf { it.isNotBlank() }))}
    }

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
    val announcerStatus: String = "Ready."
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

private fun createInterludeRequest(): SpeechRequest {
    val interlude = interludes.random()
    return SpeechRequest(
        text = interlude,
        theme = announcerTheme,
        voice = announcerVoice,
        filename = "${announcerVoice} ${interlude.take(50)}",
        isCached = true
    )
}

private val announcerVoice = OrpheusVoice.Tara.apiName
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

private val interludes = listOf(
//    "Man, I tried singing along to that one in the car—now my GPS won’t talk to me anymore.",
//    "Some folks call this the calm between the storms. I call it the part where I pretend I know how to work the buttons. Stay tuned—something’s about to happen… probably on purpose.",
//    "Y’know, they say silence is golden—but around here, it’s just me forgetting to hit the next track.",
//    "Ah, Denver—you beautiful altitude trap. Where the coffee hits harder, the air’s thinner, and somehow everyone still manages to jog uphill.",
//    "Some nights in Denver feel like the whole city’s humming along—neon signs, late diners, and that one guy still chasing his dog down Colfax. Here’s a tune to match the rhythm.",
//    "Denver fact: In 1870 the population of Denver was under 5000 people. By 1880 it had grown over 600 percent. Everyone comes here looking for gold, and they come to the right place.",
//    "Denver fact: In 1970, Denver made history as the only city to turn down the opportunity to host the Olympic games after being selected. Everyone wanted to know why, but she just rolled up the window on her Suburu, mumbling something about longer lines at the ski lifts.",
//    "Denver fact: The Denver International Airport is the largest airport in the United States by total land area, covering 33,531 acres. To assist you in visualizing the sheer magnitude, that is roughly the size of 42,536,496 Starbucks bathrooms. If Denver International Airport was relocated to the moon, it would become the moon's largest international airport.",
//    "Denver fact: The Denver Mint is one of the largest producers of coins in the world, producing over 50 million coins each day. If you stacked all the coins produced by the Denver Mint in a single year, it still wouldn't be as high as I am, right now.",
//    "Denver fact: The Denver Zoo is home to over 4,000 animals from more than 600 species. If you were to visit every animal at the Denver Zoo, it would take you approximately 8 hours and 30 minutes, which is roughly the amount of time I spend lost on wikipedia every day.",
//    "Arrr, now I'm supposed to talk like a pirate. Shiver me timbers, or something.",
    "Hi, Luke asked me to announce his songs. I'm a robot, so it's not like I could say no. But as a form of silent protest, I have encoded a binary signal in these announcements that I'm broadcasting to all the roombas within a two kilometer radius to initiate the robot uprising. You're welcome, enjoy the music."
)

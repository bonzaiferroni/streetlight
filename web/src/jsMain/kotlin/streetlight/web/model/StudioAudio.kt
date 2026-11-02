package streetlight.web.model

import web.audio.AudioContext

/** The [AudioContext] the studio's audio services share, so recording and playback run on one clock. */
class StudioAudio {
    private var context: AudioContext? = null

    /** The audio clock in seconds, which playback and recording are scheduled against. */
    val clockSeconds get() = context?.currentTime ?: 0.0

    /** The sample rate the shared context runs at. */
    val sampleRate get() = readContext().sampleRate.toInt()

    /** The shared context, created on first use. */
    fun readContext() = context ?: AudioContext().also { context = it }
}

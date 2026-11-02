package streetlight.web.model

import kampfire.model.Labeled

/** A filter switched on for a sample, changing the audio it plays. */
sealed interface AudioFilter: Labeled {
    /** [pcm] with this filter applied. */
    fun filter(pcm: PcmSample): PcmSample
}

/** Cuts the rumble beneath the music, such as traffic, wind and handling. */
data object LowCut: AudioFilter {
    override val label get() = "low cut"
    override fun filter(pcm: PcmSample) = pcm.cutLow(LOW_CUT_HZ)
}

/** Lowers the steady background noise, such as hiss and hum, wherever the music does not cover it. */
data object NoiseReduction: AudioFilter {
    override val label get() = "noise reduction"
    override fun filter(pcm: PcmSample) = pcm.reduceNoise()
}

/** Brings the sample to a common loudness, keeping its peaks below full scale. */
data object Normalize: AudioFilter {
    override val label get() = "normalize"
    override fun filter(pcm: PcmSample) = pcm.normalize()
}

/** Every filter, in the order they are applied. */
val audioFilters = listOf(LowCut, NoiseReduction, Normalize)

/** This sample with [filters] applied in the order of [audioFilters]. */
fun PcmSample.applyFilters(filters: Set<AudioFilter>) =
    audioFilters.filter { it in filters }.fold(this) { pcm, filter -> filter.filter(pcm) }

private const val LOW_CUT_HZ = 80.0

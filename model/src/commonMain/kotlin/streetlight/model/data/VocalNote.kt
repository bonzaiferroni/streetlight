package streetlight.model.data

import kotlinx.serialization.Serializable

/** A sung note held for a duration. A `null` pitch is a rest. */
@Serializable
data class VocalNote(
    val utterance: String?,
    val duration: Int?,
    val pitch: Int?,
    val isPhraseEnd: Boolean = false,
) {
    /** The note and its duration written in [style], relative to [rootPitch]. */
    fun toNotation(
        rootPitch: Int = 60,
        style: NotationStyle = NotationStyle.Degrees
    ): String {
        val notation = pitch?.let { notationOf(it, rootPitch, style) }
        return buildString {
            append(utterance ?: "~")
            duration?.let {
                append('_')
                append(it)
            }
            notation?.let {
                append('.')
                append(it)
            }
        }
    }
}

/** Reads a vocal note written by [VocalNote.toNotation], or returns `null`. */
fun parseVocalNote(text: String, style: NotationStyle, isPhraseEnd: Boolean): VocalNote? {
    var utterance = text
    var pitch: Int? = null
    val duration = if (text.contains('_')) {
        val array = text.split('_')
        if (array.size != 2) return null
        var durationText = array[1]
        utterance = array[0]
        if (durationText.contains('.')) {
            val subArray = durationText.split('.')
            if (subArray.size != 2) return null
            pitch = pitchOf(subArray[1], style)
            durationText = subArray[0]
        }
        durationText.toIntOrNull() ?: return null
    } else null
    return VocalNote(
        utterance = utterance.takeIf { it != "~" },
        duration = duration,
        pitch = pitch,
        isPhraseEnd = isPhraseEnd
    )
}
package streetlight.model.music

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** The notes of a part of a track, [length] long, shown as [name] and written [measuresPerLine] measures to a line. */
@Serializable
sealed interface Notation: Labeled {
    val notationId: NotationId
    val name: String?
    val length: Beats
    val measuresPerLine: Int
    val trackType: TrackType

    override val label get() = name ?: trackType.label
}

/** The identity of a notation or an arrangement of a track. */
@JvmInline @Serializable
value class NotationId(val value: Uuid) {
    override fun toString() = value.toString()
    companion object { fun random() = NotationId(Uuid.random()) }
}

/** The [notations] played in order as [notationId], shown as [name], each a notation or another arrangement. */
@Serializable
data class Arrangement(
    val notationId: NotationId,
    val name: String,
    val notations: List<NotationId>,
)
package streetlight.model.music

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * The notes of a part of a track, [length] long from its first [downbeat], shown as [name] and written
 * [measuresPerLine] measures to a line.
 */
@Serializable
sealed interface Notation: Labeled {
    val notationId: NotationId
    val name: String?
    val downbeat: Beats
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

/**
 * The [notations] played in order as [notationId], shown as [name], each a notation or another arrangement, played
 * [repeats] times after [wait] measures of rest, once and at once when they are `null`.
 */
@Serializable
data class Arrangement(
    val notationId: NotationId,
    val name: String,
    val notations: List<NotationId>,
    val repeats: Int? = null,
    val wait: Int? = null,
)

/** The number of notes of [noteTicks], each of its ticks, that start before [downbeat], which make up a pickup. */
fun pickupCountOf(noteTicks: List<Int>, downbeat: Beats): Int {
    var tick = 0
    return noteTicks.takeWhile { ticks -> (tick < downbeat.ticks).also { tick += ticks } }.size
}
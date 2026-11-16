package streetlight.model.music

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Study(
    val studyId: StudyId,
    val trackId: TrackId,
    val notationId: NotationId?,
)

@JvmInline @Serializable
value class StudyId(val value: Uuid) {
    override fun toString() = value.toString()
}
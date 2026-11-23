package streetlight.model.music

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

@Serializable
data class Study(
    val trackId: TrackId,
    val notationId: NotationId?,
    val isActive: Boolean,
)

data class MusicStudy(
    val study: Study,
    val sheet: MusicSheet,
): Labeled {
    override val label get() = "${sheet.music.name}: $targetLabel"

    private val targetLabel get() = study.notationId?.let { sheet.notationOrNullOf(it)?.label }
        ?: sheet.trackOf(study.trackId).label
}

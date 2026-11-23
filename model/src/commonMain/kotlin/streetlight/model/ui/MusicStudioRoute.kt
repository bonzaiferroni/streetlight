package streetlight.model.ui

import kampfire.api.ordinalParamOf
import kampfire.api.paramOf
import kampfire.api.read
import koala.model.PathParse
import koala.model.buildRelativePath
import streetlight.model.music.MusicId
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import kotlin.uuid.Uuid

sealed interface MusicStudioRoute: StreetlightRoute {
    override val screen get() = Screen.MusicStudio
    // a change of route is a change within the view
    override val screenKey: Any get() = Screen.MusicStudio
}

data object MusicStudioIndexRoute: MusicStudioRoute {
    override val title get() = "Songs"
}

data class MusicEditorRoute(
    val musicId: MusicId,
    val tab: MusicStudioTab? = null,
): MusicStudioRoute {
    companion object {
        val tabParam = ordinalParamOf<MusicStudioTab>("tab")
    }

    override val title get() = "Song"
    override fun toRelativePath() = buildRelativePath(listOf(MUSIC_NODE, musicId.toString())) {
        writeParam(tabParam, tab)
    }
}

data class MusicStudyRoute(
    val trackId: TrackId,
    val notationId: NotationId? = null,
): MusicStudioRoute {
    companion object {
        val notationParam = paramOf(
            key = "notation",
            toValue = { text -> Uuid.parseOrNull(text)?.let { NotationId(it) } },
            toString = { it?.toString() ?: error("notation param not found") },
        )
    }

    override val title get() = "Study"
    override fun toRelativePath() = buildRelativePath(listOf(STUDY_NODE, trackId.toString())) {
        writeParam(notationParam, notationId)
    }
}

val parseMusicStudioRoute = PathParse(listOf(MUSIC_NODE, STUDY_NODE)) { nodes ->
    val id = nodes.takeSegment(2)?.let { Uuid.parseOrNull(it) }
    when {
        id == null -> MusicStudioIndexRoute
        nodes[1] == MUSIC_NODE -> MusicEditorRoute(MusicId(id), read(MusicEditorRoute.tabParam))
        nodes[1] == STUDY_NODE -> MusicStudyRoute(TrackId(id), read(MusicStudyRoute.notationParam))
        else -> MusicStudioIndexRoute
    }
}

private const val MUSIC_NODE = "music"
private const val STUDY_NODE = "study"

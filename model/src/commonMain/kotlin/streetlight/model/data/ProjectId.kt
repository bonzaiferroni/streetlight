package streetlight.model.data

import kabinet.db.TableId

sealed interface ProjectId: TableId<String>

inline fun <reified T: ProjectId> String.toProjectId(): T = when (T::class) {
    AreaId::class -> AreaId(this) as T
    ContactId::class -> ContactId(this) as T
    EventId::class -> EventId(this) as T
    LocationId::class -> LocationId(this) as T
    RequestId::class -> RequestId(this) as T
    SongId::class -> SongId(this) as T
    RenditionId::class -> RenditionId(this) as T
    SparkId::class -> SparkId(this) as T
    else -> error("invalid projectId type: ${T::class.simpleName}")
}
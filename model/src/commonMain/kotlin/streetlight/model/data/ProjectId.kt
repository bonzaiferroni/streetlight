package streetlight.model.data

import kampfire.api.TableId
import kotlin.uuid.Uuid

sealed interface ProjectId: TableId<Uuid>

inline fun <reified T> Uuid.toProjectId(): T = when (T::class) {
    StarId::class -> StarId(this) as T
    GalaxyId::class -> GalaxyId(this) as T
    ContactId::class -> ContactId(this) as T
    EventId::class -> EventId(this) as T
    LocationId::class -> LocationId(this) as T
    RequestId::class -> RequestId(this) as T
    SongId::class -> SongId(this) as T
    RenditionId::class -> RenditionId(this) as T
    PerformerId::class -> PerformerId(this) as T
    GuestId::class -> GuestId(this) as T
    UploadFileId::class -> UploadFileId(this) as T
    TalentId::class -> TalentId(this) as T
    PostId::class -> PostId(this) as T
    CommentId::class -> CommentId(this) as T
    else -> error("invalid projectId type: ${T::class.simpleName}")
}

inline fun <reified T> String.toProjectId() = Uuid.parse(this).toProjectId<T>()
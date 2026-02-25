package streetlight.model.data

import kampfire.api.TableId

sealed interface ProjectId: TableId<String>

inline fun <reified T: ProjectId> String.toProjectId(): T = when (T::class) {
    CommunityId::class -> CommunityId(this) as T
    ContactId::class -> ContactId(this) as T
    EventId::class -> EventId(this) as T
    LocationId::class -> LocationId(this) as T
    RequestId::class -> RequestId(this) as T
    SongId::class -> SongId(this) as T
    RenditionId::class -> RenditionId(this) as T
    PerformerId::class -> PerformerId(this) as T
    GuestId::class -> GuestId(this) as T
    PostId::class -> PostId(this) as T
    UploadFileId::class -> UploadFileId(this) as T
    TalentId::class -> TalentId(this) as T
    else -> error("invalid projectId type: ${T::class.simpleName}")
}
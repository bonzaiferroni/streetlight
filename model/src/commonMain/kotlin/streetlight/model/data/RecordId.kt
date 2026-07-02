package streetlight.model.data

import kampfire.api.TableId
import kotlin.uuid.Uuid

sealed interface RecordId: TableId<Uuid> {
    val string get() = value.toString()
}

inline fun <reified T> Uuid.toRecordId(): T = when (T::class) {
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
    EditLogId::class -> EditLogId(this) as T
    TaskId::class -> TaskId(this) as T
    MediumId::class -> MediumId(this) as T
    QuorumId::class -> QuorumId(this) as T
    PolicyId::class -> PolicyId(this) as T
    FlagId::class -> FlagId(this) as T
    else -> error("invalid recordId type: ${T::class.simpleName}")
}

inline fun <reified T> String.toRecordId() = Uuid.parse(this).toRecordId<T>()
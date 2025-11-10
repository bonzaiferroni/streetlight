package streetlight.server.utils

import kabinet.model.UserId
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.data.ProjectId
import streetlight.model.data.toProjectId
import java.util.UUID

inline fun <reified T: ProjectId> ResultRow.toProjectId(column: Column<EntityID<UUID>>): T =
    this[column].value.toStringId().toProjectId()

inline fun <reified T: ProjectId> ResultRow.toProjectIdOrNull(column: Column<EntityID<UUID>?>): T? =
    this.getOrNull(column)?.value?.toStringId()?.toProjectId()

inline fun <reified T: ProjectId> EntityID<UUID>.toProjectId(): T = value.toStringId().toProjectId()

fun ResultRow.toUserId(column: Column<EntityID<UUID>>) = UserId(this[column].value.toStringId())

fun ResultRow.toUserIdOrNull(column: Column<EntityID<UUID>?>) = this[column]?.value?.toStringId()?.let { UserId(it) }
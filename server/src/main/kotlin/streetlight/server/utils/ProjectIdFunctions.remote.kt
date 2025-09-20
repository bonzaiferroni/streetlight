package streetlight.server.utils

import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.data.AreaId
import streetlight.model.data.ContactId
import streetlight.model.data.EventId
import streetlight.model.data.LocationId
import streetlight.model.data.ProjectId
import streetlight.model.data.RequestId
import streetlight.model.data.SongId
import streetlight.model.data.toProjectId
import java.util.UUID

inline fun <reified T: ProjectId> ResultRow.toProjectId(column: Column<EntityID<UUID>>): T =
    this[column].value.toStringId().toProjectId()

inline fun <reified T: ProjectId> ResultRow.toProjectIdOrNull(column: Column<EntityID<UUID>>): T? =
    this.getOrNull(column)?.value?.toStringId()?.toProjectId()

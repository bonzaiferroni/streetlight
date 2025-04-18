package streetlight.server.db.tables

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.core.Area

internal object AreaTable : IntIdTable() {
    val name = text("name")
}

internal fun ResultRow.toArea() = Area(
    id = this[AreaTable.id].value,
    name = this[AreaTable.name]
)
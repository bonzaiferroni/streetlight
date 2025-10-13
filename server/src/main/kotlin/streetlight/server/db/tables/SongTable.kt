package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toUUID
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Song
import streetlight.model.data.SongNotation
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object SongTable : UUIDTable() {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = text("title")
    val artist = text("artist")
    val notation = jsonb<SongNotation>("notation", tableJsonDefault).nullable()
    val tempo = integer("tempo").nullable()
    val capo = integer("capo").nullable()
    val inRotation = bool("in_rotation").default(true)
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
}

fun ResultRow.toSong() = Song(
    songId = toProjectId(SongTable.id),
    userId = toUserId(SongTable.userId),
    title = this[SongTable.name],
    artist = this[SongTable.artist],
    notation = this[SongTable.notation],
    tempo = this[SongTable.tempo],
    capo = this[SongTable.capo],
    inRotation = this[SongTable.inRotation],
    updatedAt = this[SongTable.updatedAt].toInstantFromUtc(),
    createdAt = this[SongTable.createdAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(song: Song) {
    this[SongTable.id] = song.songId.toUUID()
    this[SongTable.userId] = song.userId.toUUID()
    this[SongTable.createdAt] = song.createdAt.toLocalDateTimeUtc()
    writeUpdate(song)
}

fun UpdateBuilder<*>.writeUpdate(song: Song) {
    this[SongTable.name] = song.title
    this[SongTable.artist] = song.artist
    this[SongTable.notation] = song.notation
    this[SongTable.tempo] = song.tempo
    this[SongTable.capo] = song.capo
    this[SongTable.inRotation] = song.inRotation
    this[SongTable.updatedAt] = song.updatedAt.toLocalDateTimeUtc()
}

val tableJsonDefault = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}
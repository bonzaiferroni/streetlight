package streetlight.app.io

import kampfire.api.write
import kotlin.time.Instant
import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewRendition
import streetlight.model.data.Rendition
import streetlight.model.data.SongId
import streetlight.model.data.RenditionId

interface RenditionRepository {
    suspend fun readById(renditionId: RenditionId): Rendition?
    suspend fun readBySongId(songId: SongId): List<Rendition>?
    suspend fun readAllSince(since: Instant): List<Rendition>?
    suspend fun create(newPlay: NewRendition): RenditionId?
    suspend fun update(play: Rendition): Boolean?
    suspend fun delete(renditionId: RenditionId): Boolean?
}

class RenditionApiClient(
    private val client: NeoApiClient
): RenditionRepository {
    override suspend fun readById(renditionId: RenditionId) = client.getById(Api.RenditionFeed, renditionId)
    override suspend fun readBySongId(songId: SongId) = client.getById(Api.RenditionFeed.BySong, songId)
    override suspend fun readAllSince(since: Instant) = client.request(Api.RenditionFeed.ReadAllSince) {
        write(it.since, since)
    }
    override suspend fun create(newPlay: NewRendition) = client.request(Api.RenditionFeed.Create, newPlay)
    override suspend fun update(play: Rendition) = client.request(Api.RenditionFeed.Update, play)
    override suspend fun delete(renditionId: RenditionId) = client.request(Api.RenditionFeed.Delete, renditionId)
}

class RenditionMockClient: RenditionRepository {
    override suspend fun readById(renditionId: RenditionId): Rendition? = null
    override suspend fun readBySongId(songId: SongId): List<Rendition>? = emptyList()
    override suspend fun readAllSince(since: Instant): List<Rendition>? = emptyList()
    override suspend fun create(newPlay: NewRendition): RenditionId? = TODO("Not yet implemented")
    override suspend fun update(play: Rendition): Boolean? = TODO("Not yet implemented")
    override suspend fun delete(renditionId: RenditionId): Boolean? = TODO("Not yet implemented")
}
